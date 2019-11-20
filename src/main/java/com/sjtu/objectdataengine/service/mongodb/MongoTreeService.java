package com.sjtu.objectdataengine.service.mongodb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sjtu.objectdataengine.dao.MongoRootDAO;
import com.sjtu.objectdataengine.dao.MongoTemplateDAO;
import com.sjtu.objectdataengine.dao.MongoTreeDAO;
import com.sjtu.objectdataengine.model.TreeNode;
import com.sjtu.objectdataengine.utils.MongoCondition;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Component
public class MongoTreeService {

    private static ObjectMapper MAPPER = new ObjectMapper();

    @Resource
    MongoTreeDAO mongoTreeDAO;

    @Resource
    MongoRootDAO mongoRootDAO;

    @Resource
    MongoTemplateDAO mongoTemplateDAO;

    /**
     * 创建结点
     * @param id 节点id
     * @param name 名称（简介）
     * @param parent 父节点id
     * @param children 子节点id
     * @return true表示成功，false反之
     */
    public void createTreeNode(String id, String name, String intro, String parent, List<String> children) {
        TreeNode treeNode = new TreeNode(id, name, intro, "", parent, children);
        if(mongoTreeDAO.create(treeNode)) {
            if(parent.equals("root")) {
                mongoRootDAO.addNewRoot(id , name);
            } else {
                //为其父结点添加关系
                opParents(id, parent, true);
                //为其子结点添加关系
                // opChildren(id, children, true);
            }
        }
    }

    /**
     * 为指定结点列表增加/减少指定子结点，父结点->子结点的关系添加/删除
     * @param child 子结点id
     * @param parent 父结点id
     * @param flag true表示增加，false表示减少
     */
    private boolean opParents(String child, String parent, boolean flag) {
        MongoCondition mongoCondition = new MongoCondition();
        try {
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
     * @return true表示成功，false表示失败
     */
    private boolean opChildren(String parent, List<String> children) {
        MongoCondition mongoCondition = new MongoCondition();
        try {
            for (String child : children) {
                mongoCondition.addQuery("id", child);
                mongoCondition.addUpdate("parent", parent);
                mongoCondition.addUpdate("updateTime", new Date());
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
     * @param parent 父结点id列表
     * @return true表示成功，false反之
     */
    private boolean updateParent(String child, String parent) {
        MongoCondition mongoCondition = new MongoCondition();
        mongoCondition.addQuery("id", child);
        mongoCondition.addUpdate("parent", parent);
        return mongoTreeDAO.update(mongoCondition);
    }

    /**
     * 为指定父节点更新子结点列表，只更新列表不更新关系
     * @param children 子结点id列表
     * @param parent 父结点id
     * @return true表示成功，false反之
     */
    private boolean updateChildren(String parent, List<String> children) {
        MongoCondition mongoCondition = new MongoCondition();
        mongoCondition.addQuery("id", parent);
        mongoCondition.addUpdate("children", children);
        return mongoTreeDAO.update(mongoCondition);
    }

    /**
     * 删除父结点->子结点有向边，首先删除父结点children中的子结点id，然后删除子结点中的parents列表
     * 注意这里只删除边，子结点依旧存在，子结点的删除在deleteNode中
     * @param child 子结点id
     * @param parent 父结点id
     */
    private boolean deleteParentsEdge(String child, String parent) {
        //opParents false操作
        if (opParents(child, parent,false)) {
            return updateParent(child, "");
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
        return opChildren("", children);

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
    public TreeNode findNodeByKey(String key) {
        return mongoTreeDAO.findByKey(key);
    }

    /**
     * 完全删除指定key的节点，删除其所有边和本身，保留其孩子和父亲
     * @param key 节点key:id
     */
    public void deleteWholeNodeByKey(String key, String template) {
        TreeNode treeNode = findNodeByKey(key);
        String parent = treeNode.getParent();
        List<String> children = treeNode.getChildren();
        deleteChildrenEdge(key, children);
        // 删除template中的node
        if (!template.equals("")) {
            // 处理方式为删掉模板
            mongoTemplateDAO.deleteByKey(template);
        }
        if (!parent.equals("root")) {
            deleteParentsEdge(key, parent);
        } else {
            deleteRootEdges(key);
        }
        deleteNodeByKey(key);
    }

    /**
     * 修改指定key的结点的属性,只能修改name，intro，parent
     * @param id ID
     * @return true表示成功，false反之
     */
    public boolean updateNodeByKey(String id, String name, String intro, String parent) {
        MongoCondition mongoCondition = new MongoCondition();
        mongoCondition.addQuery("id", id);
        //改名字
        if (name != null) mongoCondition.addUpdate("name", name);
        if (intro != null) mongoCondition.addUpdate("intro", intro);

        //改parents 算法可改进 目前是删掉所有的再重新添加
        if (parent!=null) {
            String oldParent = mongoTreeDAO.findByKey(id).getParent();
            // 在oldParent结点的children中删除该结点
            opParents(id, oldParent, false);
            // 在新的parent结点的children中添加该结点
            opParents(id, parent, true);
            // 把该结点的parent改成当前parent
            mongoCondition.addUpdate("parent", parent);
        }

        mongoCondition.addUpdate("updateTime", new Date());
        return mongoTreeDAO.update(mongoCondition);
    }

    /**
     * 如果是根节点，删除root内的备份
     * @param key id
     */
    private void deleteRootEdges(String key) {
        mongoRootDAO.deleteRoot(key);
    }

}
