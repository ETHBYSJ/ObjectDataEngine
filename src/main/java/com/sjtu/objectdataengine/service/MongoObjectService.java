package com.sjtu.objectdataengine.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sjtu.objectdataengine.dao.MongoHeaderDAO;
import com.sjtu.objectdataengine.dao.MongoObjectDAO;
import com.sjtu.objectdataengine.dao.MongoTemplateDAO;
import com.sjtu.objectdataengine.model.AttrsHeader;
import com.sjtu.objectdataengine.model.MongoAttr;
import com.sjtu.objectdataengine.model.MongoAttrs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class MongoObjectService {

    private static ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    MongoObjectDAO mongoObjectDAO;

    @Autowired
    MongoTemplateDAO mongoTemplateDAO;

    @Autowired
    MongoHeaderDAO mongoHeaderDAO;

    /**
     * 这里分为三步，首先创建对象链的头结点，名为对象id+属性名称+0，头结点内含当前长度等属性
     * 其次创建好单条属性的文档，没有的值为“”（空字符串）
     * 最后多条属性都要塞进去
     * @param id 对象id
     * @param template 对象模板
     * @return true or false
     */
    public boolean createObject(String id, String template, HashMap<String, String> kv) {
        try {
            Set<String> attrs = mongoTemplateDAO.findByKey(template).getAttr();
            for (String attr : attrs) {
                String value = kv.get(attr)==null ? "" : kv.get(attr);
                createAttr(id, attr, value);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    private void createAttr(String id, String name, String value) {
        //先创建header
        Date now = new Date();
        String headerId = id + name + "0";
        AttrsHeader attrsHeader = new AttrsHeader(headerId, id, name, 1);
        attrsHeader.setCreateTime(now);
        attrsHeader.setUpdateTime(now);
        mongoHeaderDAO.create(attrsHeader);
        //再创建属性文档
        Date later = new Date();
        String attrId = id + name + "1";
        List<MongoAttr> mongoAttrList = new ArrayList<>();
        //创建一条属性的一个mongo attr
        MongoAttr mongoAttr = new MongoAttr(value);
        mongoAttr.setCreateTime(later);
        mongoAttr.setUpdateTime(later);
        //加入属性的mongo attr列表
        mongoAttrList.add(mongoAttr);
        MongoAttrs mongoAttrs = new MongoAttrs(attrId, mongoAttrList);
        mongoAttrs.setCreateTime(later);
        mongoAttrs.setUpdateTime(later);
        mongoObjectDAO.create(mongoAttrs);
    }

    /**
     * 根据属性id获取整条属性，属性id=对象id+属性名字
     * @param id 对象id
     * @param name 属性名字
     * @return 一条属性所有记录
     */
    public List<MongoAttrs> findAttrsByKey(String id, String name) {
        List<MongoAttrs> mongoAttrsList = new ArrayList<>();
        String header = id + name + "0";
        //System.out.println(header);
        //System.out.println(mongoHeaderDAO.findByKey("110"));
        AttrsHeader attrsHeader= mongoHeaderDAO.findByKey(header);
        if (attrsHeader == null) return mongoAttrsList;
        int size = attrsHeader.getSize();
        for (int i=1; i<=size; ++i) {
            String key = id + name + i;
            mongoAttrsList.add(mongoObjectDAO.findByKey(key));
        }
        return mongoAttrsList;
    }

    /**
     * 根据属性id获取最新属性值
     * @param id 对象id
     * @param name 属性名字
     * @return 最新属性值
     */
    public MongoAttr findLatestAttrByKey(String id, String name) {
        String header = id + name + "0";
        int size = mongoHeaderDAO.findByKey(header).getSize();
        String key = id + name + size;
        MongoAttrs mongoAttrs = mongoObjectDAO.findByKey(key);
        List<MongoAttr> mongoValueList =  mongoAttrs.getAttrs();
        return mongoValueList.get(mongoAttrs.getSize()-1);
    }
}
