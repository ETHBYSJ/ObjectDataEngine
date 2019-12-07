package com.sjtu.objectdataengine.dao.object;

import com.sjtu.objectdataengine.dao.MongoBaseDAO;
import com.sjtu.objectdataengine.model.object.AttrsHeader;
import com.sjtu.objectdataengine.model.object.AttrsModel;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class MongoHeaderDAO extends MongoBaseDAO<AttrsHeader> {
    @Resource
    MongoAttrsDAO mongoAttrsDAO;

    public boolean deleteAttrs(String id, String attr, Class<AttrsHeader> tClass) {
        AttrsHeader attrsHeader = findById(id, tClass);
        if (attrsHeader == null) return false;
        try {
            for (int i=1; i<=attrsHeader.getSize(); ++i) {
                mongoAttrsDAO.deleteById(id + attr + i, AttrsModel.class);
            }
            deleteById(id + attr + "0", tClass);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
