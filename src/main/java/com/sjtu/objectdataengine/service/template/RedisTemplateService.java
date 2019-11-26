package com.sjtu.objectdataengine.service.template;

import com.sjtu.objectdataengine.dao.template.RedisTemplateDAO;
import com.sjtu.objectdataengine.dao.tree.RedisTreeDAO;
import com.sjtu.objectdataengine.model.template.ObjectTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Component
public class RedisTemplateService {

    @Resource
    private RedisTemplateDAO redisTemplateDAO;

    @Resource
    private RedisTreeDAO redisTreeDAO;

    /**
     * 创建模板
     * @param id 模板id
     * @param name 名字
     * @param type 类型
     * @param nodeId 节点id
     * @param attrs 属性列表
     * @return true代表创建成功，false代表创建失败
     */
    boolean createTemplate(String id, String name, String intro, String type, String nodeId, HashMap<String, String> attrs) {
        Date now = new Date();
        //创建模板
        //id索引表
        String indexKey = "index";
        String attrsKey = id + '#' + "attrs";
        String baseKey = id + '#' +"base";
        //判断是否是第一次创建
        if(redisTemplateDAO.sHasKey(indexKey, id)) {
            return false;
        }
        //首先存入id索引表
        redisTemplateDAO.sSet(indexKey, id);
        if(attrs != null && attrs.size() > 0) {
            //存储属性列表
            redisTemplateDAO.hmset(attrsKey, attrs);
        }

        //存储基本信息
        redisTemplateDAO.hset(baseKey, "id", id);
        if(!name.equals("")) redisTemplateDAO.hset(baseKey, "name", name);
        if(!intro.equals("")) redisTemplateDAO.hset(baseKey, "intro", intro);
        if(!type.equals("")) redisTemplateDAO.hset(baseKey, "type", type);
        if(!nodeId.equals("")) redisTemplateDAO.hset(baseKey, "nodeId", nodeId);
        //如果树节点已经存在，建立树节点与模板的关联
        if(redisTreeDAO.sHasKey("index", nodeId)) {
            redisTreeDAO.hset(nodeId + "#base", "template", id);
        }
        redisTemplateDAO.hset(baseKey, "createTime", now);
        redisTemplateDAO.hset(baseKey, "updateTime", now);
        return true;
    }

    /**
     * 返回全部模板
     * @return 全部模板
     */
    public List<ObjectTemplate> findAllTemplate() {
        return redisTemplateDAO.findAll();
    }

    /**
     * 根据id返回模板
     * @param id 模板id
     * @return 模板
     */
    ObjectTemplate findTemplateById(String id) {
        return redisTemplateDAO.findById(id);
    }

    /**
     * 根据id删除模板
     * @param id 模板id
     * @return true代表删除成功，false代表删除失败
     */
    boolean deleteTemplateById(String id) {
        //解除树节点的绑定
        Object nodeId = redisTemplateDAO.hget(id + "#base", "nodeId");
        if(nodeId != null) {
            redisTreeDAO.hset(nodeId + "#base", "template", "");
            redisTreeDAO.hset(nodeId + "#base", "updateTime", new Date());
        }
        return redisTemplateDAO.deleteById(id);
    }

    public boolean hasKey(String id) {
        return redisTemplateDAO.hasKey(id);
    }

    boolean addAttrs(String id, String name, String nickname) {
        return redisTemplateDAO.hset(id + "#attrs", name, nickname);
    }

    boolean delAttrs(String id, String name) {
        return (redisTemplateDAO.hdel(id + "#attrs", name) > 0);
    }

    boolean addObject(String id, String objId) {
        return redisTemplateDAO.lSet(id + "#objects", objId);
    }

    boolean delObject(String id, String objId) {
        return (redisTemplateDAO.lRemove(id + "#objects", 1, objId) > 0;
    }

    /**
     * 根据条件更新对象模板
     * @param id ID
     * @return true or false
     */
    boolean updateBaseInfo(String id, String name, String intro){
        String baseKey = id + "#base";
        if(id == null) return false;
        if(!this.hasKey(id)) return false;
        if(name != null) {
            redisTemplateDAO.hset(baseKey, "name", name);
        }
        if(intro != null) {
            redisTemplateDAO.hset(baseKey, "intro", intro);
        }
        return true;
    }

}
