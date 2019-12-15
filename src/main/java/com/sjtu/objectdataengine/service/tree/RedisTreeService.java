package com.sjtu.objectdataengine.service.tree;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sjtu.objectdataengine.dao.object.RedisAttrDAO;
import com.sjtu.objectdataengine.dao.tree.RedisRootDAO;
import com.sjtu.objectdataengine.dao.template.RedisTemplateDAO;
import com.sjtu.objectdataengine.dao.tree.RedisTreeDAO;
import com.sjtu.objectdataengine.model.tree.TreeNode;
import com.sjtu.objectdataengine.model.object.CommonObject;
import com.sjtu.objectdataengine.model.tree.TreeNodeReturn;
import com.sjtu.objectdataengine.service.object.RedisObjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RedisTreeService {
    private static ObjectMapper MAPPER = new ObjectMapper();
    @Autowired
    private RedisTreeDAO redisTreeDAO;
    @Autowired
    private RedisRootDAO redisRootDAO;
    @Autowired
    private RedisTemplateDAO redisTemplateDAO;
    @Autowired
    private RedisObjectService redisObjectService;
    @Autowired
    private RedisAttrDAO redisAttrDAO;


    /**
     * 创建一个树节点
     * @param id 节点id
     * @param name 名称（简介）
     * @param intro 简介
     * @param parent 父节点列表
     * @param children 子节点列表
     * @return true代表创建成功，false代表创建失败
     */
    public boolean createTreeNode(String id, String name, String intro, String parent, List<String> children, Date date) {
        //id不可为空
        if(id == null) return false;
        if(name == null) name = "";
        if(parent == null) /*parent = "";*/return false;
        if(intro == null) intro = "";
        if(children == null) children = new ArrayList<String>();

        //创建树节点
        String baseKey = id + '#' + "base";
        String childrenKey = id + '#' + "children";
        String indexKey = "index";
        //判断已经创建过
        if(redisTreeDAO.sHasKey(indexKey, id)) return false;
        //Date now = new Date();
        //首先存入索引表
        redisTreeDAO.sSet(indexKey, id);
        //存储基本信息
        redisTreeDAO.hset(baseKey, "id", id);
        redisTreeDAO.hset(baseKey, "name", name);

        redisTreeDAO.hset(baseKey, "createTime", date);
        redisTreeDAO.hset(baseKey, "updateTime", date);
        redisTreeDAO.hset(baseKey, "intro", intro);
        //存储父节点
        redisTreeDAO.hset(baseKey, "parent", parent);

        if(children != null && children.size() != 0) {
            //存储子节点列表
            redisTreeDAO.lSet(childrenKey, children.toArray());
        }
        //更新根节点列表
        if(parent.equals("root")) {
            redisRootDAO.addNewRoot(id, name, date);
        }
        else {
            //ops
            opParent(id, parent, true, date);
        }
        return true;
    }

    /**
     * 对特定节点的父节点的子属性作修改
     * @param child 特定子节点id
     * @param parent 父节点id
     * @param flag true代表添加，false代表删除
     * @return true代表成功，false代表失败
     */
    private boolean opParent(String child, String parent, boolean flag, Date date) {
        try {
            //Date now = new Date();
            redisTreeDAO.hset(child + "#base", "updateTime", date);
            if(flag) {
                //添加
                if(!redisTreeDAO.lHasValue(parent + "#children", child)) {
                    redisTreeDAO.lSet(parent + "#children", child);
                }
                redisTreeDAO.hset(parent + "#base", "updateTime", date);
            }
            else {
                //删除
                redisTreeDAO.lRemove(parent + "#children", 1, child);
                redisTreeDAO.hset(parent + "#base", "updateTime", date);
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
     * @return true代表成功，false代表失败
     */
    private boolean opChildren(String parent, List<String> children, Date date) {
        try {
            //Date now = new Date();
            redisTreeDAO.hset(parent + "#base", "updateTime", date);
            //更新children列表中的parent属性
            for(String child : children) {
                redisTreeDAO.hset(child + "#base", "parent", parent);
                redisTreeDAO.hset(child + "#base", "updateTime", date);
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
     * @param parent 父节点id
     * @return true代表更新成功，false代表更新失败
     */
    private boolean updateParent(String child, String parent, Date date) {
        try {
            String baseKey = child + "#base";
            redisTreeDAO.hset(baseKey, "updateTime", date);
            redisTreeDAO.hset(baseKey, "parent", parent);
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
    private boolean updateChildren(String parent, List<String> children, Date date) {
        try {
            redisTreeDAO.hset(parent + "#base", "updateTime", date);
            String childrenKey = parent + "#children";
            //清空子节点列表
            redisTreeDAO.del(childrenKey);
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
     * @param parent 父节点id
     * @return true代表成功，false代表失败
     */
    private boolean deleteParentEdge(String child, String parent, Date date) {
        if(opParent(child, parent, false, date)) {
            return updateParent(child, "", date);
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
    private boolean deleteChildrenEdge(String parent, List<String> children, Date date) {
        if (opChildren("", children, date)) {
            List<String> newChildren = new ArrayList<>();
            return updateChildren(parent, newChildren, date);
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
     * sss
     * @param key 树节点id
     * @return 树节点
     */
    public TreeNodeReturn findTreeByRoot(String key) {
        return redisTreeDAO.findTreeByRoot(key);
    }
    /**
     * 根据id返回指定树节点
     * @param key 树节点id
     * @return 树节点(无嵌套)
     */
    public TreeNode findNodeByKey(String key) {
        return redisTreeDAO.findByKey(key);
    }

    /**
     * 删除根节点的备份
     * @param key 树节点id
     * @return true代表删除成功，false代表删除失败
     */
    private boolean deleteRootEdge(String key, Date date) {
        return redisRootDAO.deleteRoot(key, date);
    }
    /**
     * 彻底删除
     * @param key 节点id
     * @return true代表成功，false代表失败
     */
    public boolean deleteWholeNodeByKey(String key, Date date) {
        try {
            if(!redisTreeDAO.sHasKey("index", key)) {
                return true;
            }
            //解除与template的绑定
            Object template = redisTreeDAO.hget(key + "#base", "template");
            if(template != null) {
                //将对应模板也删除
                redisTemplateDAO.deleteById(template.toString());
            }
            String baseKey = key + "#base";
            String childrenKey = key + "#children";
            String parent = redisTreeDAO.hget(baseKey, "parent").toString();
            List<String> children = (List<String>) redisTreeDAO.lGet(childrenKey, 0, -1);
            if(children != null && children.size() != 0) {
                deleteChildrenEdge(key, children, date);
            }
            if(parent.equals("root")) {
                //是根节点
                deleteRootEdge(key, date);
            }
            else {
                //非根节点
                deleteParentEdge(key, parent, date);
            }
            deleteNodeByKey(key);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 对树节点的更新
     * @param id ID
     * @return true代表更新成功，false代表更新失败
     */
    public boolean updateNodeByKey(String id, String name, String intro, String parent, Date date) {
        try {
            String baseKey = id + '#' + "base";
            //改名字
            if(name != null) {
                redisTreeDAO.hset(baseKey, "name", name);
            }
            /*
            if(template!=null && redisTemplateDAO.sHasKey("index", template)) {
                redisTreeDAO.hset(baseKey, "template", template);
            }
            */
            if(intro != null) {
                redisTreeDAO.hset(baseKey, "intro", intro);
            }
            if (parent!=null) {
                String oldParent = redisTreeDAO.hget(baseKey, "parent").toString();
                //从旧父节点孩子节点中删除
                opParent(id, oldParent, false, date);
                //新父节点建立关联
                opParent(id, parent, true, date);
                //更新父节点
                updateParent(id, parent, date);
            }
            //更新时间
            redisTreeDAO.hset(baseKey, "updateTime", date);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public boolean hasKey(String id) {
        return redisTreeDAO.sHasKey("index", id);
    }
}
