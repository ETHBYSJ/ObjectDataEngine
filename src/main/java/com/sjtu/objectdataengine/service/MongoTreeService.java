package com.sjtu.objectdataengine.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sjtu.objectdataengine.dao.MongoRootDAO;
import com.sjtu.objectdataengine.dao.MongoTreeDAO;
import com.sjtu.objectdataengine.model.KnowledgeTreeNode;
import com.sjtu.objectdataengine.utils.MongoCondition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class MongoTreeService {

    private static ObjectMapper MAPPER = new ObjectMapper();

    @Resource
    MongoTreeDAO mongoTreeDAO;

    @Resource
    MongoRootDAO mongoRootDAO;

    /**
     * 创建结点
     * @param request 请求json
     * @return true表示成功，false反之
     */
    public boolean createTreeNode(String request) {
        //解析
        JSONObject jsonObject = JSON.parseObject(request);
        //id必须要有
        String id = jsonObject.getString("id");
        if(id == null) return false;
        String name = jsonObject.getString("name");
        if (name == null) name = "";
        String template = jsonObject.getString("template");
        if (template == null) template = "";
        JSONArray parentsArray = jsonObject.getJSONArray("parents");
        JSONArray childrenArray = jsonObject.getJSONArray("children");
        JSONArray objectsArray = jsonObject.getJSONArray("objects");
        //要判断是否有空的
        List<String> parents = parentsArray==null ? new ArrayList<>() : JSONObject.parseArray(parentsArray.toJSONString(), String.class);
        List<String> children =childrenArray==null ? new ArrayList<>() : JSONObject.parseArray(childrenArray.toJSONString(), String.class);
        List<String> objects = objectsArray==null ? new ArrayList<>() : JSONObject.parseArray(objectsArray.toJSONString(), String.class);
        KnowledgeTreeNode knowledgeTreeNode = new KnowledgeTreeNode(id, name, template, parents, children, objects);


        if(mongoTreeDAO.create(knowledgeTreeNode)) {
            if(parents.get(0) == "root") {
                mongoRootDAO.addNewRoot(id , name);
            }
            //为其父结点添加关系
            opParents(id, parents, true);
            //为其子结点添加关系
            opChildren(id, children, true);
            return true;
        } else{
            return false;
        }
    }

    /**
     * 为指定结点列表增加/减少指定子结点，父结点->子结点的关系添加/删除
     * @param child 子结点id
     * @param parents 父结点id列表
     * @param flag true表示增加，false表示减少
     */
    private boolean opParents(String child, List<String> parents, boolean flag) {
        MongoCondition mongoCondition = new MongoCondition();
        try {
            for (String parent : parents) {
                List<String> childrenList = mongoTreeDAO.findByKey(parent).getChildren();
                if (flag) {
                    childrenList.add(child);
                } else {
                    childrenList.remove(child);
                }
                mongoCondition.addQuery("id", parent);
                mongoCondition.addUpdate("children", childrenList);
                mongoTreeDAO.update(mongoCondition);
                mongoCondition.clearQuery();
                mongoCondition.clearUpdate();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 对应opChildren, 增加/删除父结点所有子结点中parents的该父结点，即子结点->父结点的关系
     * @param parent 父结点id
     * @param children 子结点id列表
     * @param flag true表示增加，false表示删除
     * @return true表示成功，false表示失败
     */
    public boolean opChildren(String parent, List<String> children, boolean flag) {
        MongoCondition mongoCondition = new MongoCondition();
        try {
            for (String child : children) {
                List<String> parentsList = mongoTreeDAO.findByKey(child).getParents();
                if (flag) {
                    parentsList.add(parent);
                } else {
                    parentsList.remove(parent);
                }
                mongoCondition.addQuery("id", child);
                mongoCondition.addUpdate("parents", parentsList);
                mongoTreeDAO.update(mongoCondition);
                mongoCondition.clearQuery();
                mongoCondition.clearUpdate();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 为指定子节点更新父结点列表，只更新列表不更新关系
     * @param child 子结点id
     * @param parents 父结点id列表
     * @return true表示成功，false反之
     */
    private boolean updateParentsList(String child, List<String> parents) {
        MongoCondition mongoCondition = new MongoCondition();
        mongoCondition.addQuery("id", child);
        mongoCondition.addUpdate("parents", parents);
        return mongoTreeDAO.update(mongoCondition);
    }

    /**
     * 为指定父节点更新子结点列表，只更新列表不更新关系
     * @param children 子结点id列表
     * @param parent 父结点id
     * @return true表示成功，false反之
     */
    private boolean updateChildrenList(String parent, List<String> children) {
        MongoCondition mongoCondition = new MongoCondition();
        mongoCondition.addQuery("id", parent);
        mongoCondition.addUpdate("parents", children);
        return mongoTreeDAO.update(mongoCondition);
    }

    /**
     * 删除父结点->子结点有向边，首先删除父结点children中的子结点id，然后删除子结点中的parents列表
     * 注意这里只删除边，子结点依旧存在，子结点的删除在deleteNode中
     * @param child 子结点id
     * @param parents 父结点id列表
     */
    private boolean deleteParentsEdge(String child, List<String> parents) {
        //opParents false操作
        if (opParents(child, parents,false)) {
            List<String> newParents = new ArrayList<>();
            return updateParentsList(child, newParents);
        } else {
            return false;
        }
    }

    /**
     * 删除子结点-->父结点的隐形有向边，首先删除父结点children，然后删除子结点中的parents列表对应的父结点id
     * 注意这里只删除边，父子结点依旧存在，父结点的删除在deleteNode中
     * @param children 子结点id列表
     * @param parent 父结点id
     */
    private boolean deleteChildrenEdge(String parent, List<String> children) {
        //opChildren false操作
        if (opChildren(parent, children,false)) {
            List<String> newChildren = new ArrayList<>();
            return updateChildrenList(parent, newChildren);
        } else {
            return false;
        }
    }

    /**
     * 删除指定Key的结点，只删除结点，未对其父子做出操作
     * @param key 节点key：id
     * @return true表示成功，false反之
     */
    private boolean deleteNodeByKey(String key) {
        return mongoTreeDAO.deleteByKey(key);
    }

    /**
     * 查看指定key的节点
     * @param key 节点key：id
     * @return KnowledgeTreeNode对象
     */
    public KnowledgeTreeNode findNodeByKey(String key) {
        return mongoTreeDAO.findByKey(key);
    }

    /**
     * 完全删除指定key的节点，删除其所有边和本身，保留其孩子和父亲
     * @param key 节点key:id
     * @return true表示成功，false反之
     */
    public boolean deleteWholeNodeByKey(String key) {
        try{
            KnowledgeTreeNode knowledgeTreeNode = findNodeByKey(key);
            List<String> parents = knowledgeTreeNode.getParents();
            List<String> children = knowledgeTreeNode.getChildren();
            deleteChildrenEdge(key, children);
            deleteParentsEdge(key, parents);
            deleteNodeByKey(key);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 修改指定key的结点的属性
     * @param query 结点key+修改
     * @return true表示成功，false反之
     */
    public boolean updateNodeByKey(String query) {
        MongoCondition mongoCondition = new MongoCondition();
        JSONObject queryObject = JSON.parseObject(query);
        //读id
        String id = queryObject.getString("id");
        if(id==null) return false;
        mongoCondition.addQuery("id", id);
        //读update体
        JSONObject update = queryObject.getJSONObject("update");
        //改名字
        String name = update.getString("name");
        if (name!=null) mongoCondition.addUpdate("name", name);
        //改模板
        String template = update.getString("template");
        if (template!=null) mongoCondition.addUpdate("template", template);
        //改对象列表
        JSONArray objectsArray = update.getJSONArray("objects");
        if (objectsArray!=null) {
            List<String> objects = JSONObject.parseArray(objectsArray.toJSONString(), String.class);
            mongoCondition.addUpdate("objects", objects);
        }
        //改parents 算法可改进 目前是删掉所有的再重新添加
        JSONArray parentsArray = update.getJSONArray("parents");
        if (parentsArray!=null) {
            List<String> parents = JSONObject.parseArray(parentsArray.toJSONString(), String.class);
            List<String> oldParents = mongoTreeDAO.findByKey(id).getParents();
            opParents(id, oldParents, false);
            opParents(id, parents, true);
            mongoCondition.addUpdate("parents", parents);
        }
        //改children
        JSONArray childrenArray = update.getJSONArray("children");
        if (childrenArray!=null) {
            List<String> children = JSONObject.parseArray(childrenArray.toJSONString(), String.class);
            List<String> oldChildren = mongoTreeDAO.findByKey(id).getChildren();
            opChildren(id, oldChildren, false);
            opChildren(id, children, true);
            mongoCondition.addUpdate("children", children);
        }

        mongoCondition.addUpdate("updateTime", new Date());

        //System.out.println(mongoCondition.getUpdate());

        return mongoTreeDAO.update(mongoCondition);
    }


}
