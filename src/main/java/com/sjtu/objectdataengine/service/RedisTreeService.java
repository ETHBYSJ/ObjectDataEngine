package com.sjtu.objectdataengine.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sjtu.objectdataengine.dao.RedisTreeDAO;
import com.sjtu.objectdataengine.model.KnowledgeTreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class RedisTreeService {
    private static ObjectMapper MAPPER = new ObjectMapper();
    @Autowired
    private RedisTreeDAO redisTreeDAO;

    /**
     * 创建一个树节点
     * @param request 请求体
     * @return true代表创建成功，false代表创建失败
     */
    public boolean createTreeNode(String request) {
        JSONObject jsonObject = JSON.parseObject(request);
        String id = jsonObject.getString("id");
        //id不可为空
        if(id == null) return false;
        String name = jsonObject.getString("name");
        if(name == null) name = "";
        String template = jsonObject.getString("template");
        if(template == null) template = "";
        JSONArray parentsArray = jsonObject.getJSONArray("parents");
        JSONArray childrenArray = jsonObject.getJSONArray("children");
        JSONArray objectsArray = jsonObject.getJSONArray("objects");

        //要判断是否有空的
        List<String> parents = parentsArray==null ? new ArrayList<>() : JSONObject.parseArray(parentsArray.toJSONString(), String.class);
        List<String> children =childrenArray==null ? new ArrayList<>() : JSONObject.parseArray(childrenArray.toJSONString(), String.class);
        List<String> objects = objectsArray==null ? new ArrayList<>() : JSONObject.parseArray(objectsArray.toJSONString(), String.class);

        return createTreeNode(id, name, template, parents, children, objects);

    }

    /**
     * 创建一个树节点
     * @param id 节点id
     * @param name 节点名
     * @param template 关联模板
     * @param parents 父节点列表
     * @param children 子节点列表
     * @param objects 关联对象列表
     * @return true代表创建成功，false代表创建失败
     */
    public boolean createTreeNode(String id, String name, String template, List<String> parents, List<String> children, List<String> objects) {
        //id不可为空
        if(id == null) return false;
        if(name == null) name = "";
        if(template == null) template = "";
        if(parents == null) parents = new ArrayList<String>();
        if(children == null) children = new ArrayList<String>();
        if(objects == null) objects = new ArrayList<String>();

        //创建树节点
        String baseKey = id + '#' + "base";
        String childrenKey = id + '#' + "children";
        String parentsKey = id + '#' + "parents";
        String objectsKey = id + '#' + "objects";
        String indexKey = "index";

        //判断已经创建过
        if(redisTreeDAO.sHasKey(indexKey, id)) return false;
        Date now = new Date();
        //首先存入索引表
        redisTreeDAO.sSet(indexKey, id);
        //存储基本信息
        redisTreeDAO.hset(baseKey, "id", id);
        redisTreeDAO.hset(baseKey, "name", name);
        redisTreeDAO.hset(baseKey, "template", template);
        redisTreeDAO.hset(baseKey, "createTime", now);
        redisTreeDAO.hset(baseKey, "updateTime", now);
        if(parents != null && parents.size() != 0) {
            //存储父节点列表
            redisTreeDAO.lSet(parentsKey, parents.toArray());
        }
        if(children != null && children.size() != 0) {
            //存储子节点列表
            redisTreeDAO.lSet(childrenKey, children.toArray());
        }
        if(objects != null && objects.size() != 0) {
            //存储关联对象列表
            redisTreeDAO.lSet(objectsKey, objects.toArray());
        }
        //ops
        opParents(id, parents, true);
        opChildren(id, children, true);
        return true;
    }

    /**
     * 对特定节点的父节点的子属性作修改
     * @param child 特定子节点id
     * @param parents 父节点列表
     * @param flag true代表添加，false代表删除
     * @return true代表成功，false代表失败
     */
    private boolean opParents(String child, List<String> parents, boolean flag) {
        try {
            if(flag) {
                //添加
                for(String parent : parents) {
                    //List<Object> childrenList = redisTreeDAO.lGet(parent + "#children", 0, -1);
                    if(!redisTreeDAO.lHasValue(parent + "#children", child)) {
                        redisTreeDAO.lSet(parent + "#children", child);
                    }
                }
            }
            else {
                //删除
                for(String parent : parents) {
                    /*
                    if(!redisTreeDAO.lHasValue(parent + "#children", child)) {
                        redisTreeDAO.lSet(parent + "#children", child);
                    }
                    */
                    redisTreeDAO.lRemove(parent + "#children", 1, child);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 对特定节点子节点的父属性作修改
     * @param parent 特定父节点id
     * @param children 子节点列表
     * @param flag true代表添加，false代表删除
     * @return true代表成功，false代表失败
     */
    private boolean opChildren(String parent, List<String> children, boolean flag) {
        try {
            if(flag) {
                //添加
                for(String child : children) {
                    //List<Object> parentsList = redisTreeDAO.lGet(child + "#parents", 0, -1);
                    if(!redisTreeDAO.lHasValue(child + "#parents", parent)) {
                        redisTreeDAO.lSet(child + "#parents", parent);
                    }
                }
            }
            else {
                //删除
                for(String child : children) {
                    /*
                    if(!redisTreeDAO.lHasValue(child + "#parents", parent)) {
                        redisTreeDAO.lSet(child + "#parents", parent);
                    }
                    */
                    redisTreeDAO.lRemove(child + "#parents", 1, parent);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 更新父节点列表
     * @param child 子节点id
     * @param parents 父节点列表
     * @return true代表更新成功，false代表更新失败
     */
    private boolean updateParentsList(String child, List<String> parents) {
        try {
            String parentsKey = child + "#parents";
            //清空父节点列表
            redisTreeDAO.lTrim(parentsKey, 1, 0);
            if(parents != null && parents.size() != 0) {
                //更新父节点列表
                redisTreeDAO.lSet(parentsKey, parents.toArray());
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * 更新子节点列表
     * @param parent 父节点id
     * @param children 子节点列表
     * @return true代表更新成功，false代表更新失败
     */
    private boolean updateChildrenList(String parent, List<String> children) {
        try {
            String childrenKey = parent + "#children";
            //清空子节点列表
            redisTreeDAO.lTrim(childrenKey, 1, 0);
            if(children != null && children.size() != 0) {
                //更新子节点列表
                redisTreeDAO.lSet(childrenKey, children.toArray());
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 切断子节点与父节点之间的关系
     * @param child 子节点id
     * @param parents 父节点列表
     * @return true代表成功，false代表失败
     */
    private boolean deleteParentsEdge(String child, List<String> parents) {
        if(opParents(child, parents, false)) {
            List<String> newParents = new ArrayList<>();
            return updateParentsList(child, newParents);
        }
        else {
            return false;
        }

    }

    /**
     * 切断父节点和子节点之间的关系
     * @param parent 父节点id
     * @param children 子节点列表
     * @return true代表成功，false代表失败
     */
    private boolean deleteChildrenEdge(String parent, List<String> children) {
        if (opChildren(parent, children,false)) {
            List<String> newChildren = new ArrayList<>();
            return updateChildrenList(parent, newChildren);
        } else {
            return false;
        }
    }
    /**
     * 删除树节点，只删除节点本身
     * @param key 节点id
     * @return true代表删除成功，false代表删除失败
     */
    private boolean deleteNodeByKey(String key) {
        return redisTreeDAO.deleteByKey(key);
    }

    /**
     * 根据id返回指定树节点
     * @param key 树节点id
     * @return 树节点
     */
    public KnowledgeTreeNode findNodeByKey(String key) {
        return redisTreeDAO.findByKey(key);
    }

    /**
     * 彻底删除
     * @param key 节点id
     * @return true代表成功，false代表失败
     */
    public boolean deleteWholeNodeByKey(String key) {
        try {
            if(!redisTreeDAO.sHasKey("index", key)) {
                return true;
            }
            String parentsKey = key + "#parents";
            String childrenKey = key + "#children";
            List<Object> parentsList = redisTreeDAO.lGet(parentsKey, 0, -1);
            List<String> parents = new ArrayList<String>();
            if(parentsList != null) {
                for(Object parent : parentsList) {
                    parents.add(parent.toString());
                }
            }
            List<Object> childrenList = redisTreeDAO.lGet(childrenKey, 0, -1);
            List<String> children = new ArrayList<String>();
            if(childrenList != null) {
                for(Object child : childrenList) {
                    children.add(child.toString());
                }
            }
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
     * 对树节点的更新
     * @param query 更新请求
     * @return true代表更新成功，false代表更新失败
     */
    /*
    public boolean updateNodeByKey(String query) {
        JSONObject queryObject = JSON.parseObject(query);
        //读id
        String id = queryObject.getString("id");
        if(id==null) return false;
        String baseKey = id + '#' + "base";
        String childrenKey = id + '#' + "children";
        String parentsKey = id + '#' + "parents";
        String objectsKey = id + '#' + "objects";
        String indexKey = id + '#' + "index";
        //读update体
        JSONObject update = queryObject.getJSONObject("update");
        //改名字
        String name = update.getString("name");
        if(name != null) {
            redisTreeDAO.hset(baseKey, "name", name);
        }
        String template = update.getString("template");
        if(template!=null) {
            redisTreeDAO.hset(baseKey, "template", template);
        }
        JSONArray objectsArray = update.getJSONArray("objects");
        if (objectsArray!=null) {
            List<String> objects = JSONObject.parseArray(objectsArray.toJSONString(), String.class);
            redisTreeDAO.lTrim(objectsKey, 1, 0);
            redisTreeDAO.lSet(objectsKey, objects.toArray());
        }
        JSONArray parentsArray = update.getJSONArray("parents");
        if (parentsArray!=null) {
            List<String> parents = JSONObject.parseArray(parentsArray.toJSONString(), String.class);
        }
        JSONArray childrenArray = update.getJSONArray("children");
        if (childrenArray!=null) {
            List<String> children = JSONObject.parseArray(childrenArray.toJSONString(), String.class);
        }
        return true;
    }
    */

}
