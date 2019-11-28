package com.sjtu.objectdataengine.service.object;

import com.sjtu.objectdataengine.dao.event.MongoEventDAO;
import com.sjtu.objectdataengine.dao.object.MongoAttrsDAO;
import com.sjtu.objectdataengine.dao.object.MongoHeaderDAO;
import com.sjtu.objectdataengine.dao.object.MongoObjectDAO;
import com.sjtu.objectdataengine.dao.template.MongoTemplateDAO;
import com.sjtu.objectdataengine.dao.tree.MongoTreeDAO;
import com.sjtu.objectdataengine.model.object.AttrsHeader;
import com.sjtu.objectdataengine.model.object.CommonObject;
import com.sjtu.objectdataengine.model.object.MongoAttrs;
import com.sjtu.objectdataengine.model.template.ObjectTemplate;
import com.sjtu.objectdataengine.utils.MongoAttr;
import com.sjtu.objectdataengine.utils.MongoCondition;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Component
public class MongoObjectService {

    @Resource
    MongoAttrsDAO mongoAttrsDAO;

    @Resource
    MongoTemplateDAO mongoTemplateDAO;

    @Resource
    MongoHeaderDAO mongoHeaderDAO;

    @Resource
    MongoObjectDAO mongoObjectDAO;

    @Resource
    MongoEventDAO mongoEventDAO;


    /**
     * 这里分为三步，首先创建对象链的头结点，名为对象id+属性名称+0，头结点内含当前长度等属性
     * 其次创建好单条属性的文档，没有的值为“”（空字符串）
     * 最后多条属性都要塞进去
     * @param id 对象id
     * @param name 唯一名称
     * @param template 对象模板
     * @param kv 属性kv对
     * @param events 关联事件集合
     */
    public void create(String id, String name, String intro, String template, HashMap<String, String> kv, List<String> events) {
        HashMap<String, String> attrsMap = mongoTemplateDAO.findByKey(template).getAttrs();
        HashMap<String, MongoAttr> hashMap = new HashMap<>();
        for (String attr : attrsMap.keySet()) {
            String value = kv.get(attr)==null ? "" : kv.get(attr);
            createHeader(id, attr);
            MongoAttr mongoAttr = createAttr(id, attr, value, 1);
            hashMap.put(attr, mongoAttr);
        }
        createObject(id, name, intro, template, events, hashMap);
    }

    /**
     *
     * @param id 对象id
     * @param name 唯一名称
     * @param intro 简介
     * @param template 对象模板
     * @param eventsList 对象关联
     * @param hashMap kv对
     */
    private void createObject(String id, String name, String intro, String template, List<String> eventsList, HashMap<String, MongoAttr> hashMap) {
        // 加入template的列表
        mongoTemplateDAO.opObjects(template, id, "add");
        ObjectTemplate objectTemplate = mongoTemplateDAO.findByKey(template);
        String type = objectTemplate.getType();

        // 组装events，加入event的列表
        HashMap<String, Date> events = new HashMap<>();
        Date now = new Date();
        for (String event : eventsList) {
            events.put(event, now);
            mongoEventDAO.opObjects(event, id, "add");
        }
        CommonObject commonObject = new CommonObject(id, name, intro, type, template, hashMap, events);
        mongoObjectDAO.create(commonObject);             //创建object
    }

    /**
     * 为一条属性链创建一个创世块
     * @param id 对象id
     * @param name 属性名称
     */
    private void createHeader(String id, String name) {
        //先创建header
        String headerId = id + name + "0";
        AttrsHeader attrsHeader = new AttrsHeader(headerId, id, name, 1);
        mongoHeaderDAO.create(attrsHeader);
    }

    /**
     * 创建一个属性块并且赋予初值
     * @param id 对象id
     * @param name 属性名称
     * @param value 属性值
     * @param size 属性块index
     * @return 返回初始的属性，封装成MongoAttr
     */
    private MongoAttr createAttr(String id, String name, String value, int size) {
        //再创建属性文档
        Date later = new Date();
        String attrId = id + name + size;
        List<MongoAttr> mongoAttrList = new ArrayList<>();
        //创建一条属性的一个mongo attr
        MongoAttr mongoAttr = new MongoAttr(value);
        mongoAttr.setCreateTime(later);
        mongoAttr.setUpdateTime(later);
        //加入属性的mongo attr列表
        mongoAttrList.add(mongoAttr);
        MongoAttrs mongoAttrs = new MongoAttrs(attrId, mongoAttrList, size);
        mongoAttrsDAO.create(mongoAttrs);
        return mongoAttr;
    }

    /**
     * 根据属性id获取整条属性，属性id=对象id+属性名字
     * @param id 对象id
     * @param name 属性名字
     * @return 一条属性所有记录
     */
    public List<MongoAttrs> findAttrsByKey(String id, String name) {
        List<MongoAttrs> mongoAttrsList = new ArrayList<>();
        int size = getAttrChainSize(id, name);
        if (size == 0) return mongoAttrsList; //空列表
        for (int i=1; i<=size; ++i) {
            String key = id + name + i;
            mongoAttrsList.add(mongoAttrsDAO.findByKey(key));
        }
        return mongoAttrsList;
    }

    /**
     * 根据属性id获取一块属性
     * @param id 对象id
     * @param name 属性名字
     * @param index 第几块
     * @return 一条属性的一块记录
     */
    private MongoAttrs findAttrsByBlock(String id, String name, int index) {
        String key = id + name + index;
        return mongoAttrsDAO.findByKey(key);
    }

    /**
     * 根据对象id和属性name获取最新属性值
     * @param id 对象id
     * @param name 属性名字
     * @return 最新属性值
     */
    public MongoAttr findLatestAttrByKey(String id, String name) {
        int size = getAttrChainSize(id, name);
        if (size == 0) return null; //null
        String key = id + name + size;
        MongoAttrs mongoAttrs = mongoAttrsDAO.findByKey(key);
        List<MongoAttr> mongoValueList =  mongoAttrs.getAttrs();
        return mongoValueList.get(mongoAttrs.getSize()-1);
    }

    /**
     * 封装this.addValue
     * @param id 对象id
     * @param name 属性名
     * @param value 值
     * @return true or false
     */
    public boolean addAttr(String id, String name, String value, Date date) {
        MongoAttr mongoAttr = new MongoAttr(value);
        mongoAttr.setCreateTime(date);
        mongoAttr.setUpdateTime(date);
        return addValue(id, name, mongoAttr);
    }

    /**
     * 根据对象id和属性name添加一条属性值
     * 一个属性块差一条满了以后，将会创建新的属性块，因为这次操作以后就满了
     * 已经满了后，要向新块写属性，并且要更新创世快size
     * @param id 对象id
     * @param name 属性名字
     * @return true or false
     */
    public boolean addValue(String id, String name, MongoAttr mongoAttr) {
        int size = getAttrChainSize(id, name);
        String key = id + name + size;
        MongoAttrs mongoAttrs = mongoAttrsDAO.findByKey(key);

        updateObject(id, name, mongoAttr);

        if (mongoAttrs == null) {
            return false;
        }
        else if (mongoAttrs.isFull()) {
            int newSize = size + 1;
            String key0 = id + name + "0";
            String newKey = id + name + newSize;
            return (mongoAttrsDAO.addValue(newKey, 0, mongoAttr) && addHeaderSize(key0, size));

        } else if (mongoAttrs.isNearlyFull()) {
            int mongoAttrSize = mongoAttrs.getSize();
            return mongoAttrsDAO.addValue(key, mongoAttrSize, mongoAttr) && addAttrs(id, name, size + 1) && timeSync(id, name, size);
        } else {
            int mongoAttrSize = mongoAttrs.getSize();
            return mongoAttrsDAO.addValue(key, mongoAttrSize, mongoAttr);
        }
    }

    /**
     * 同步两个块的CT和UT，首尾相连
     */

    private boolean timeSync(String id, String name, int size) {
        try{
            int newSize = size + 1;
            String newKey = id + name + newSize;
            MongoAttrs preMongoAttrs = findAttrsByBlock(id, name ,size);
            Date ut = preMongoAttrs.getUpdateTime();
            MongoCondition mongoCondition = new MongoCondition();
            mongoCondition.addQuery("id", newKey);
            mongoCondition.addUpdate("createTime", ut);
            //System.out.println(mongoCondition.getUpdate());
            return mongoAttrsDAO.update(mongoCondition);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 更新Object最新值
     * @param id 对象id
     * @param name 属性名称
     * @param mongoAttr 最新值
     */
    private void updateObject(String id, String name, MongoAttr mongoAttr) {
        mongoObjectDAO.updateAttrList(id, name, mongoAttr);
    }

    /**
     * 增加一个新的属性块，用于一个属性块即将满了以后
     * @param id 对象id
     * @param name 属性name
     * @param size 属性块数量+1
     * @return true or false
     */
    private boolean addAttrs(String id, String name, int size) {
        String newKey = id + name + size;
        //创建一个空的
        MongoAttrs mongoAttrs = new MongoAttrs(newKey, new ArrayList<>(), size);
        return mongoAttrsDAO.create(mongoAttrs);
    }

    /**
     * 获取链长度
     * @param id 对象id
     * @param name 属性名称
     * @return 返回长度，若不存在则返回0
     */
    private int getAttrChainSize(String id, String name) {
        String header = id + name + "0";
        AttrsHeader attrsHeader = mongoHeaderDAO.findByKey(header);
        if (attrsHeader == null) {
            return 0;
        } else {
            return mongoHeaderDAO.findByKey(header).getSize();
        }
    }

    /**
     * 创世块增加size
     * @param key0 创世块id
     * @param size 原size
     * @return true or false
     */
    private boolean addHeaderSize(String key0, int size) {
        MongoCondition mongoCondition = new MongoCondition();
        mongoCondition.addQuery("id", key0);
        mongoCondition.addUpdate("size", size + 1);
        //System.out.println(mongoCondition.getUpdate());
        return mongoHeaderDAO.update(mongoCondition);
    }

    /**
     * 创世块自检size
     * @param id 对象id
     * @param name 属性名称
     * @return 实际size
     */
    public int findActualSize(String id, String name) {
        List<MongoAttrs> mongoAttrsList = findAttrsByKey(id, name);
        int size = mongoAttrsList.size();
        if (size == 0) return 0;
        MongoAttrs mongoAttrs = mongoAttrsList.get(size-1);
        if (mongoAttrs.getAttrs().size() == 0) {
            return size - 1;
        } else {
            return size;
        }
    }

    /**
     * 查找最新的object
     * @param id 对象id
     * @return 最新对象
     */
    public CommonObject findLatestObjectByKey(String id) {
        return mongoObjectDAO.findByKey(id);
    }

    /**
     * 查找某个时间点的属性
     */
    public MongoAttr findAttrByTime(String id, String name, Date time) {
        int cSize = getAttrChainSize(id, name); //chain size
        MongoAttrs mongoAttrs = divFindAttrsByTime(id, name, time, cSize);
        if (mongoAttrs == null) return null;
        Date firstUt = mongoAttrs.getAttrs().get(0).getUpdateTime();
        //System.out.println(mongoAttrs.getAttrs());
        //如果发现第一个ut都大于这个时间，说明是上一块的最后一个
        if (time.before(firstUt) && cSize > 1) {
            MongoAttrs mongoAttrs1 = findAttrsByBlock(id, name, cSize-1);
            List<MongoAttr> mongoAttrList = mongoAttrs1.getAttrs();
            return mongoAttrList.get(mongoAttrs1.getSize()-1);
        }
        //反之 用二分法查找属性
        return divFindAttrByTime(mongoAttrs, time);
    }

    /**
     * 查找某个时间段的属性
     */
    public List<MongoAttr> findAttrByStartAndEnd(String id, String name, Date st, Date et) {
        int cSize = getAttrChainSize(id, name); //chain size
        //找到起止块index
        MongoAttrs startMongoAttrs = divFindAttrsByTime(id, name, st, cSize);
        MongoAttrs endMongoAttrs = divFindAttrsByTime(id, name, et, cSize);
        int startIndex = startMongoAttrs.getIndex();
        int endIndex = endMongoAttrs.getIndex();
        //System.out.println(startIndex);
        //System.out.println(endIndex);
        //mongoAttrList 用于返回
        List<MongoAttr> mongoAttrList = new ArrayList<>();
        //找到开始块内ct的index
        MongoAttr startAttr = divFindAttrByTime(startMongoAttrs, st);
        List<MongoAttr> startAttrsList = startMongoAttrs.getAttrs();
        int startAttrIndex = startAttrsList.indexOf(startAttr);
        int startSize = startAttrsList.size();
        //找到结束块内et的index
        MongoAttr endAttr = divFindAttrByTime(endMongoAttrs, et);
        List<MongoAttr> endAttrsList = endMongoAttrs.getAttrs();
        int endAttrIndex = endAttrsList.indexOf(endAttr);

        //System.out.println(startAttrIndex);
        //System.out.println(endAttrIndex);


        if (startIndex < endIndex) {
            //开始块
            mongoAttrList.add(startAttr);
            for (int s=startAttrIndex+1; s<startSize-1; ++s) {
                MongoAttr tmp = startAttrsList.get(s);
                mongoAttrList.add(tmp);
            }

            //中间块
            for (int i=startIndex+1; i<endIndex; ++i) {
                MongoAttrs mongoAttrs1 = findAttrsByBlock(id, name, i);
                if(mongoAttrs1!=null) {
                    List<MongoAttr> mongoAttrList1 = mongoAttrs1.getAttrs();
                    mongoAttrList.addAll(mongoAttrList1);
                }
            }

            //结束块
            for (int e=0; e<endAttrIndex; ++e) {
                MongoAttr tmp = endAttrsList.get(e);
                mongoAttrList.add(tmp);
            }
            mongoAttrList.add(endAttr);
        } else {
            for (int j=startAttrIndex; j<=endAttrIndex; ++j) {
                mongoAttrList.add(startAttrsList.get(j));
            }
        }


        return mongoAttrList;
    }

    /**
     * 查找某个时间点的obj
     */
    public CommonObject findObjectByTime(String id, Date time) {
        Date ut = new Date(0);
        CommonObject commonObject = mongoObjectDAO.findByKey(id);
        Set<String> attrName = commonObject.getAttr().keySet();
        for(String name : attrName) {
            MongoAttr mongoAttr = findAttrByTime(id, name, time);
            commonObject.putAttr(name, mongoAttr);
            if(ut.before(mongoAttr.getUpdateTime())) {
                ut = mongoAttr.getUpdateTime();
            }
        }
        commonObject.cutObjects(time);
        commonObject.setUpdateTime(ut);
        return commonObject;
    }

    /**
     * 查找某个时间段的obj
     */
    public List<CommonObject> findObjectByStartAndEnd(String id, Date st, Date et) {
        Set<Date> dateSet = new HashSet<>();
        CommonObject commonObject = mongoObjectDAO.findByKey(id);
        Set<String> attrName = commonObject.getAttr().keySet();
        List<MongoAttr> mongoAttrList = new ArrayList<>();
        //System.out.println(attrName);
        for (String name : attrName) {
            mongoAttrList.addAll(findAttrByStartAndEnd(id, name, st, et));
        }
        for (MongoAttr mongoAttr : mongoAttrList) {
            //每个属性至少有一个初始值，不会有空的
            dateSet.add(mongoAttr.getUpdateTime());
        }
        List<Date> dateList = new ArrayList<>(dateSet);
        Collections.sort(dateList);
        int len = 0;
        for(int i=0; i<dateList.size(); ++i) {
            //i <= st && i+1 <= st
            if((dateList.get(i).before(st) || dateList.get(i).equals(st)) && dateList.get(i+1).before(st) || dateList.get(i+1).equals(st)) {
                len += 1;
            } else {
                break;
            }
        }
        //System.out.println(dateList);
        for (int j=0; j<len; ++j) {
            dateList.remove(0);
        }
        System.out.println(dateList);

        List<CommonObject> commonObjectList = new ArrayList<>();
        for (Date date : dateList) {
            commonObjectList.add(findObjectByTime(id, date));
        }

        return commonObjectList;
    }

    /**
     * 二分法在指定的属性块内，查找对应时间点的属性
     */
    private MongoAttr divFindAttrByTime(MongoAttrs mongoAttrs, Date time) {
        List<MongoAttr> mongoAttrList = mongoAttrs.getAttrs();
        int low = 0;
        int high = mongoAttrList.size()-1;
        while(high - low > 1) {
            int mid = (low + high) / 2;
            //System.out.println(low + " " + high + " " + mid);
            MongoAttr midAttr = mongoAttrList.get(mid);
            Date ut = midAttr.getUpdateTime();
            if (time.before(ut)) {
                high = mid;
            } else if(time.after(ut)) {
                low = mid;
            } else {
                return midAttr;
            }
        }
        if (time.after(mongoAttrList.get(high).getUpdateTime()) || time.equals(mongoAttrList.get(high).getUpdateTime())) {
            return mongoAttrList.get(high);
        } else {
            return mongoAttrList.get(low);
        }
    }

    /**
     * 二分法查找对应时间点的属性块
     * @return 返回块
     */
    private MongoAttrs divFindAttrsByTime(String id, String name, Date time, int cSize) {
        int high = cSize;
        int low = 1;
        //System.out.println(id+name+time+cSize);
        MongoAttrs endBlock = findAttrsByBlock(id, name, cSize);
        List<MongoAttr> endBlockList = endBlock.getAttrs();
        //System.out.println(endBlockList);
        Date endTime = endBlockList.get(endBlockList.size()-1).getUpdateTime();
        if (endTime.before(time)) {
            //System.out.println("return endBlock");
            return endBlock;
        }

        while (low <= high) {
            int mid = (low + high) / 2;
            //System.out.println(low + " " + high + " " + mid);
            MongoAttrs mongoAttrs = findAttrsByBlock(id, name, mid);
            Date ct = mongoAttrs.getCreateTime();
            Date ut = mongoAttrs.getUpdateTime();
            //time.before(ct)<=> time < ct
            if (time.before(ct) || time.equals(ct)) {
                high = mid - 1;
            } //time > ct && time <= ut
            else if (time.after(ct) && (time.before(ut) || time.equals(ut))) {
                //System.out.println();
                return mongoAttrs;
            } else if (time.after(ct) && time.after(ut)) {
                low = mid + 1;
            }
        }
        return null;
    }

}
