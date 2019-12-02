package com.sjtu.objectdataengine.service.object;

import com.sjtu.objectdataengine.dao.event.MongoEventDAO;
import com.sjtu.objectdataengine.dao.object.MongoAttrsDAO;
import com.sjtu.objectdataengine.dao.object.MongoHeaderDAO;
import com.sjtu.objectdataengine.dao.object.MongoObjectDAO;
import com.sjtu.objectdataengine.dao.template.MongoTemplateDAO;
import com.sjtu.objectdataengine.model.object.AttrsHeader;
import com.sjtu.objectdataengine.model.object.AttrsModel;
import com.sjtu.objectdataengine.model.object.CommonObject;
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
    public void create(String id, String name, String intro, String template, HashMap<String, String> kv, List<String> events, Date date) {
        HashMap<String, String> attrsMap = mongoTemplateDAO.findByKey(template).getAttrs();
        HashMap<String, MongoAttr> hashMap = new HashMap<>();
        for (String attr : attrsMap.keySet()) {
            String value = kv.get(attr)==null ? "" : kv.get(attr);
            createHeader(id, attr, date);
            MongoAttr mongoAttr = createAttr(id, attr, value, date,1);
            hashMap.put(attr, mongoAttr);
        }
        createObject(id, name, intro, template, events, hashMap, date);
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
    private void createObject(String id, String name, String intro, String template, List<String> eventsList, HashMap<String, MongoAttr> hashMap, Date date) {
        // 加入template的列表
        mongoTemplateDAO.opObjects(template, id, "add");
        ObjectTemplate objectTemplate = mongoTemplateDAO.findByKey(template);
        String type = objectTemplate.getType();

        // 组装events，加入event的列表
        HashMap<String, Date> events = new HashMap<>();

        for (String event : eventsList) {
            events.put(event, date);
            mongoEventDAO.opObjects(event, id, "add");
        }
        CommonObject commonObject = new CommonObject(id, name, intro, type, template, hashMap, events);
        commonObject.setCreateTime(date);
        commonObject.setUpdateTime(date);
        mongoObjectDAO.create(commonObject);             //创建object
    }

    /**
     * 为一条属性链创建一个创世块
     * @param id 对象id
     * @param name 属性名称
     */
    private void createHeader(String id, String name, Date date) {
        //先创建header
        String headerId = id + name + "0";
        AttrsHeader attrsHeader = new AttrsHeader(headerId, id, name, 1);
        attrsHeader.setCreateTime(date);
        attrsHeader.setUpdateTime(date);
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
    private MongoAttr createAttr(String id, String name, String value, Date date, int size) {
        //再创建属性文档
        String attrId = id + name + size;
        List<MongoAttr> mongoAttrList = new ArrayList<>();
        //创建一条属性的一个mongo attr
        MongoAttr mongoAttr = new MongoAttr(value);
        mongoAttr.setUpdateTime(date);
        //加入属性的mongo attr列表
        mongoAttrList.add(mongoAttr);
        AttrsModel mongoAttrs = new AttrsModel(attrId, mongoAttrList, size);
        mongoAttrs.setCreateTime(date);
        mongoAttrs.setUpdateTime(date);
        mongoAttrsDAO.create(mongoAttrs);
        return mongoAttr;
    }

    /**
     * 根据属性id获取整条属性，属性id=对象id+属性名字
     * @param id 对象id
     * @param name 属性名字
     * @return 一条属性所有记录
     */
    public List<AttrsModel> findAttrsByKey(String id, String name) {
        List<AttrsModel> mongoAttrsList = new ArrayList<>();
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
    private AttrsModel findAttrsByBlock(String id, String name, int index) {
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
        AttrsModel mongoAttrs = mongoAttrsDAO.findByKey(key);
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
        mongoAttr.setUpdateTime(date);
        return addValue(id, name, mongoAttr, date);
    }

    /**
     * 根据对象id和属性name添加一条属性值
     * 一个属性块差一条满了以后，将会创建新的属性块，因为这次操作以后就满了
     * 已经满了后，要向新块写属性，并且要更新创世快size
     * @param id 对象id
     * @param name 属性名字
     * @param date 公共date
     * @return true or false
     */
    private boolean addValue(String id, String name, MongoAttr mongoAttr, Date date) {
        int size = getAttrChainSize(id, name);
        String key = id + name + size;
        AttrsModel mongoAttrs = mongoAttrsDAO.findByKey(key);

        // 在object表里更新
        updateObject(id, name, mongoAttr, date);

        // 在attr表里更新
        if (mongoAttrs == null) {
            return false;
        }
        else if (mongoAttrs.isFull()) {
            int newSize = size + 1;
            String key0 = id + name + "0";
            String newKey = id + name + newSize;
            return (mongoAttrsDAO.addValue(newKey, 0, mongoAttr, date) && addHeaderSize(key0, size));

        } else if (mongoAttrs.isNearlyFull()) {
            int mongoAttrSize = mongoAttrs.getSize();
            return mongoAttrsDAO.addValue(key, mongoAttrSize, mongoAttr, date) && addAttrs(id, name, size + 1) && timeSync(id, name, size);
        } else {
            int mongoAttrSize = mongoAttrs.getSize();
            return mongoAttrsDAO.addValue(key, mongoAttrSize, mongoAttr, date);
        }
    }

    /**
     * 同步两个块的CT和UT，首尾相连
     */

    private boolean timeSync(String id, String name, int size) {
        try{
            int newSize = size + 1;
            String newKey = id + name + newSize;
            AttrsModel preMongoAttrs = findAttrsByBlock(id, name ,size);
            Date ut = preMongoAttrs.getUpdateTime();
            MongoCondition mongoCondition = new MongoCondition();
            mongoCondition.addQuery("id", newKey);
            mongoCondition.addUpdate("createTime", ut);
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
    private void updateObject(String id, String name, MongoAttr mongoAttr, Date date) {
        mongoObjectDAO.updateAttrList(id, name, mongoAttr, date);
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
        AttrsModel mongoAttrs = new AttrsModel(newKey, new ArrayList<>(), size);
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
        return mongoHeaderDAO.update(mongoCondition);
    }

    /**
     * 创世块自检size
     * @param id 对象id
     * @param name 属性名称
     * @return 实际size
     */
    public int findActualSize(String id, String name) {
        List<AttrsModel> mongoAttrsList = findAttrsByKey(id, name);
        int size = mongoAttrsList.size();
        if (size == 0) return 0;
        AttrsModel mongoAttrs = mongoAttrsList.get(size-1);
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
        // 获取到对应属性链的长度
        int cSize = getAttrChainSize(id, name);
        AttrsModel mongoAttrs = divFindAttrsByTime(id, name, time, cSize);
        if (mongoAttrs == null) return null;
        Date firstUt = mongoAttrs.getAttrs().get(0).getUpdateTime();
        //如果发现第一个ut都大于这个时间，说明是上一块的最后一个
        if (time.before(firstUt) && cSize > 1) {
            AttrsModel mongoAttrs1 = findAttrsByBlock(id, name, cSize-1);
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
        // st <= et
        if (st.after(et)) return null;
        int cSize = getAttrChainSize(id, name); //chain size
        //找到起止块index
        // 如果et早于开始时间，就会查到null，说明应该返回空
        AttrsModel endMongoAttrs = divFindAttrsByTime(id, name, et, cSize);
        if (endMongoAttrs == null) {
            return new ArrayList<>();
        }
        AttrsModel startMongoAttrs = divFindAttrsByTime(id, name, st, cSize);
        // 如果st早于开始时间，就会查到null，说明起始index应该为1
        if (startMongoAttrs == null) {
            startMongoAttrs = mongoAttrsDAO.findByKey(id+name+"1");
        }
        int startIndex = startMongoAttrs.getIndex();
        int endIndex = endMongoAttrs.getIndex();

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

        if (startIndex < endIndex) {
            //开始块
            mongoAttrList.add(startAttr);
            for (int s=startAttrIndex+1; s<startSize-1; ++s) {
                MongoAttr tmp = startAttrsList.get(s);
                mongoAttrList.add(tmp);
            }

            //中间块
            for (int i=startIndex+1; i<endIndex; ++i) {
                AttrsModel mongoAttrs1 = findAttrsByBlock(id, name, i);
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
        // System.out.println("findObjectByTime: id = " + id + ", time = " + time + ", time.getTime() = " + time.getTime());
        // 初始化Date为0
        Date ut = new Date(0);
        // 取下最新的属性，作为模板，更换对应时间的属性
        CommonObject commonObject = mongoObjectDAO.findByKey(id);
        // 如果早过创建时间，就返回空
        if (time.before(commonObject.getCreateTime())) return null;
        // 取出属性列表的key集合
        Set<String> attrName = commonObject.getAttr().keySet();
        // 对每个属性循环
        for(String name : attrName) {
            // 找到对应时间点的属性
            MongoAttr mongoAttr = findAttrByTime(id, name, time); // error: time=createTime为null
            // 放入模板object中
            commonObject.putAttr(name, mongoAttr);
            // 比较更新updateTime
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
     * @param id 查询obj的id
     * @param st 查询开始时间
     * @param et 查询结束时间
     */
    public List<CommonObject> findObjectByStartAndEnd(String id, Date st, Date et) {
        Set<Date> dateSet = new HashSet<>();
        CommonObject commonObject = mongoObjectDAO.findByKey(id);
        // 如果et
        Date createTime = commonObject.getCreateTime();
        if (et.before(createTime)) return null;

        Date startTime = st;
        // 先修剪st, 如果st小于createTime，就把它变成createTime
        if (st.before(createTime)) {
            startTime = createTime;
        }

        Set<String> attrName = commonObject.getAttr().keySet();
        List<MongoAttr> mongoAttrList = new ArrayList<>();
        for (String name : attrName) {
            mongoAttrList.addAll(findAttrByStartAndEnd(id, name, createTime, et));
        }
        // 避免重复，先使用set
        // 先加入st
        dateSet.add(startTime);
        for (MongoAttr mongoAttr : mongoAttrList) {
            // 加入所有st之后更新的时间，不包括st
            if (mongoAttr.getUpdateTime().after(startTime))
                dateSet.add(mongoAttr.getUpdateTime());
        }
        List<Date> dateList = new ArrayList<>(dateSet);
        Collections.sort(dateList);
        List<CommonObject> commonObjectList = new ArrayList<>();
        for (Date date : dateList) {
            CommonObject temp = findObjectByTime(id, date);
            // 如果开始时间早于创建时间，会出现null
            if (temp != null) commonObjectList.add(temp);
        }

        return commonObjectList;
    }

    /**
     * 二分法在指定的属性块内，查找对应时间点的属性
     */
    private MongoAttr divFindAttrByTime(AttrsModel mongoAttrs, Date time) {
        List<MongoAttr> mongoAttrList = mongoAttrs.getAttrs();
        //System.out.println("div:" + time + mongoAttrs);
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
    private AttrsModel divFindAttrsByTime(String id, String name, Date time, int cSize) {
        // System.out.println("divFindAttrsByTime: id = " + id + ", name = " + name + ", time.getTime() = " + time.getTime() + ", cSize = " + cSize);
        int high = cSize;
        int low = 1;
        // 当前链的第一块
        AttrsModel startBlock = findAttrsByBlock(id, name, 1);
        // 第一块的创建时间
        Date startTime = startBlock.getCreateTime();
        // 当前链的最后一块
        AttrsModel endBlock = findAttrsByBlock(id, name, cSize);
        // 最后一块的最后更新时间
        Date endTime = endBlock.getUpdateTime();
        // 如果查询时间>=最后更新时间，那就是最后一块
        if (!time.before(endTime)) {
            return endBlock;
        }
        // 如果查询时间=开始时间，那就是第一块
        else if (time.equals(startTime)) {
            return startBlock;
        }
        // 如果查询时间<开始时间，那就是null
        else if (time.before(startTime)) {
            return null;
        }
        /* 否则就用二分法
         * 这里，关于ut和ct有以下规律
         * 只有第一块的第一个属性updateTime和createTime相同，其他的createTime都小于第一块的updateTime
         * 一个满块的updateTime和下一块的createTime是相同的
         */
        while (low <= high) {
            int mid = (low + high) / 2;
            AttrsModel mongoAttrs = findAttrsByBlock(id, name, mid);
            Date ct = mongoAttrs.getCreateTime();
            Date ut = mongoAttrs.getUpdateTime();
            //time <= ct
            if (time.before(ct) || time.equals(ct)) {
                high = mid - 1;
            } //time > ct && time <= ut
            else if (time.before(ut) || time.equals(ut)) {
                return mongoAttrs;
            } // time > ct && time > ut
            else if (time.after(ut)) {
                low = mid + 1;
            }
        }
        return null;
    }

}
