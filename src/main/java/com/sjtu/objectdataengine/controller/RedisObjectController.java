package com.sjtu.objectdataengine.controller;

import com.alibaba.fastjson.JSONObject;
import com.sjtu.objectdataengine.dao.RedisDAO;
import com.sjtu.objectdataengine.dao.RedisTreeDAO;
import com.sjtu.objectdataengine.model.*;
import com.sjtu.objectdataengine.service.RedisObjectService;
import com.sjtu.objectdataengine.service.RedisTemplateService;
import com.sjtu.objectdataengine.service.RedisTreeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RequestMapping("/redisobject")
@RestController
public class RedisObjectController {
    @Autowired
    private RedisObjectService redisObjectService;
    @Autowired
    private RedisTemplateService redisTemplateService;
    @Autowired
    private RedisTreeService redisTreeService;
    @Autowired
    private RedisTreeDAO redisTreeDAO;
    //------------------------------redisDAO test--------------------------------//

    @GetMapping("delete")
    public void delete(@RequestParam String key) {
        redisTreeDAO.del(key);
    }

    @GetMapping("test_type")
    public List<String> testType(@RequestParam String key) {
        return (List<String>) redisTreeDAO.lGet(key, 0, -1);
    }
    @GetMapping("hmset")
    public boolean hmset(@RequestParam String key) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("1", "1");
        map.put("2", "2");
        return redisTreeDAO.hmset(key, map);
    }
    @GetMapping("trim")
    public boolean testTrim(@RequestParam String key, @RequestParam long start, @RequestParam long end) {
        return redisTreeDAO.lTrim(key, start, end);
    }
    //-------------------------------tree-------------------------------------//
    @PostMapping("create_tree")
    public boolean createTree(@RequestBody String request) {
        return redisTreeService.createTreeNode(request);
    }
    @GetMapping("del_node")
    public boolean delTreeNode(@RequestParam String id) {
        return redisTreeService.deleteWholeNodeByKey(id);
    }
    @PostMapping("update_tree")
    public boolean updateTree(@RequestBody String request) {
        return redisTreeService.updateNodeByKey(request);
    }
    @GetMapping("get_tree_by_root")
    public TreeNodeReturn findTreeByRoot(@RequestParam String id) {
        return redisTreeService.findTreeByRoot(id);
    }
    @GetMapping("get_node_by_id")
    public KnowledgeTreeNode getNodeById(@RequestParam String id) {
        return redisTreeService.findNodeByKey(id);
    }
    //-------------------------------template---------------------------------//
    @GetMapping("get_all_template")
    public List<ObjectTemplate> getAllTemplate() {
        return redisTemplateService.findAllTemplate();
    }

    @GetMapping("delete_template_by_id")
    public boolean deleteTemplateById(@RequestParam String id) {
        return redisTemplateService.deleteTemplateById(id);
    }

    @PostMapping("create_template")
    public boolean createTemplate(@RequestBody String request) {
        return redisTemplateService.createTemplate(request);
    }

    @GetMapping("get_template_by_id")
    public ObjectTemplate getTemplateById(@RequestParam String id) {
        return redisTemplateService.findTemplateById(id);
    }

    //------------------------------object------------------------------------//
    @GetMapping("get_attr_by_time")
    public MongoAttr getAttrByTime(@RequestParam String id, @RequestParam String attr, @RequestParam Date date) {
        return redisObjectService.findAttrByTime(id, attr, date);
    }

    @GetMapping("get_latest_attr")
    public MongoAttr getLatestAttr(@RequestParam String id, @RequestParam String attr) {
        return redisObjectService.findAttrByKey(id, attr);
    }

    @GetMapping("create_obj")
    public boolean createObject() {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("age", "12");
        hashMap.put("name", "jack");
        List<String> objects = new ArrayList<String>();
        objects.add("1");
        objects.add("2");
        return redisObjectService.create("1", "test_obj", "2",objects, hashMap);
    }

    @GetMapping("addAttr")
    public boolean addAttr(@RequestParam String id, @RequestParam String attr, @RequestParam String value, @RequestParam Date date) {
        return redisObjectService.addAttr(id, attr, value, date);
    }

    @GetMapping("get_latest_obj")
    public MongoObject getLatest(@RequestParam String id) {
        return redisObjectService.findObjectById(id);
    }

    @GetMapping("get_obj_by_time")
    public MongoObject getByTime(@RequestParam String id, @RequestParam Date date) {
        return redisObjectService.findObjectById(id, date);
    }
    @GetMapping("get_attr_by_time_interval")
    public List<MongoAttr> getAttrByTimeInterval(@RequestParam String id, @RequestParam String name, @RequestParam Date startDate, @RequestParam Date endDate) {
        return redisObjectService.findAttrBetweenTime(id, name, startDate, endDate);
    }
    @GetMapping("get_by_time_interval")
    public List<MongoObject> getByTimeInterval(@RequestParam String id, @RequestParam Date startDate, @RequestParam Date endDate) {
        return redisObjectService.findObjectBetweenTime(id, startDate, endDate);
    }

    @GetMapping("find_attr")
    public List<String> findAttr(@RequestParam String id) {
        return redisObjectService.findAttrByObjectId(id);
    }

    @GetMapping("remove_attr")
    public boolean removeAttr(@RequestParam String id, @RequestParam String attr) {
        return redisObjectService.removeAttrByObjectId(id, attr);
    }
    @GetMapping("add_attr")
    public boolean addAttr(@RequestParam String id, @RequestParam String attr) {
        return redisObjectService.addAttrByObjectId(id, attr);
    }
    @GetMapping("update_attr")
    public boolean updateAttr(@RequestParam String id, @RequestParam String oldAttr, @RequestParam String newAttr) {
        return redisObjectService.updateAttrByObjectId(id, oldAttr, newAttr);
    }
    @PostMapping("create_attr")
    public boolean createAttr(@RequestBody String request) {
        return redisObjectService.createAttr(request);
    }

}
