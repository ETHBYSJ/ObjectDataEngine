package com.sjtu.objectdataengine.service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sjtu.objectdataengine.dao.RedisAttrDAO;
import com.sjtu.objectdataengine.dao.RedisObjectDAO;
import com.sjtu.objectdataengine.dao.RedisTemplateDAO;
import com.sjtu.objectdataengine.model.MongoAttr;
import com.sjtu.objectdataengine.model.MongoObject;
import com.sjtu.objectdataengine.model.ObjectTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sjtu.objectdataengine.dao.RedisDAO;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class RedisObjectService {
    @Value("${redis.cache-size}")
    private int cacheSize;
    @Value("${redis.evict-size}")
    private int evictSize;
    @Autowired
    private static ObjectMapper MAPPER;
    @Autowired
    private RedisAttrDAO redisAttrDAO;
    @Autowired
    private RedisObjectDAO redisObjectDAO;
    @Autowired
    private RedisTemplateDAO redisTemplateDAO;

    private static final Logger logger = LoggerFactory.getLogger(RedisObjectService.class);

    /**
     * 创建新对象
     * @param request json请求体
     * @return true代表创建成功，false代表创建失败
     * json请求体结构：
     * {
     *     "id": "1",
     *     "template": "1",
     *     "objects": ["2", "3", "5"],
     *      "attrs": {
     *          "name": "Tony",
     *          "age": "18",
     *          "friends": [
     *              {
     *                  "id": "2",
     *                  "template": "1",
     *                  "objects": ["1"],
     *                  "attrs": {
     *                      "name": "Jack",
     *                      "age": "18",
     *                      "friends": [
     *
     *                      ],
     *                  }
     *              }
     *           ],
     *      }
     * }
     */
    /*
    public boolean create(String request) {

        return true;
    }
    */
    /**
     * 创建新对象
     * @param id 对象id
     * @param intro 描述信息
     * @param template 模板id
     * @param objects 关联对象集合
     * @param attrMap 属性集合
     * @return true代表创建成功，false代表创建失败
     */
    public boolean create(String id, String intro, String template, List<String> objects, HashMap<String, String> attrMap) {
        ObjectTemplate objectTemplate = redisTemplateDAO.findById(template);
        //没有找到模板，返回false
        if(objectTemplate == null) return false;
        Set<String> attrs = objectTemplate.getAttrs();
        HashMap<String, MongoAttr> hashMap = new HashMap<String, MongoAttr>();
        Date date = new Date();
        for(String attr : attrs) {
            String value = attrMap.get(attr) == null ? "" : attrMap.get(attr);
            MongoAttr mongoAttr = new MongoAttr(value);
            mongoAttr.setCreateTime(date);
            mongoAttr.setUpdateTime(date);
            hashMap.put(attr, mongoAttr);
        }
        return createObject(id, intro, objectTemplate, objects, hashMap);
    }

    /**
     * 创建新对象
     * @param id 对象id
     * @param intro 描述信息
     * @param objectTemplate 模板
     * @param objects 关联对象
     * @param hashMap 属性集合
     * @return true代表创建成功，false代表创建失败
     */
    public boolean createObject(String id, String intro, ObjectTemplate objectTemplate, List<String> objects, HashMap<String, MongoAttr> hashMap) {
        if(redisAttrDAO.hsize(id + '#' + "META") != 0) {
            //说明对象已存在，创建失败
            return false;
        }
        if(intro == null) intro = "";
        String templateId = objectTemplate.getId();
        String nodeId = objectTemplate.getNodeId();
        String type = objectTemplate.getType();
        Date date = new Date();
        //存储对象基本信息
        redisAttrDAO.hset(id + "#META", "template", templateId);
        redisAttrDAO.hset(id + "#META", "nodeId", nodeId);
        redisAttrDAO.hset(id + "#META", "type", type);
        redisAttrDAO.hset(id + "#META", "createTime", date);
        redisAttrDAO.hset(id + "#META", "updateTime", date);
        redisAttrDAO.hset(id + "#META", "intro", intro);

        //存储关联对象
        if(objects != null) {
            for(String object : objects) {
                redisAttrDAO.hset(id + "#object", object, date);
            }
        }
        //存储属性基本信息
        for(Map.Entry<String, MongoAttr> entry : hashMap.entrySet()) {
            String attrName = entry.getKey();
            MongoAttr mongoAttr = entry.getValue();
            Date attrCT = mongoAttr.getCreateTime();
            Date attrUT = mongoAttr.getUpdateTime();
            //插入属性
            addAttrByObjectId(id, attrName);

            redisObjectDAO.hset(id + '#' + attrName, "createTime", attrCT);
            redisObjectDAO.hset(id + '#' + attrName, "updateTime", attrUT);
            boolean addSucc = addAttr(id, attrName, mongoAttr.getValue(), date);
            if(!addSucc) {
                return false;
            }
        }
        return true;
    }
    /**
     * 根据对象id获取对象所有属性
     * @param id 对象id
     * @return 对象属性集合
     */
    public List<String> findAttrByObjectId(String id) {
        return (List<String>) redisAttrDAO.lGet(id, 0, -1);
    }

    /**
     * 为指定对象添加属性
     * @param id 对象id
     * @param attr 属性名
     * @return true代表插入成功，false代表失败
     */
    public boolean addAttrByObjectId(String id, String attr) {
        if(redisAttrDAO.lHasValue(id, attr)) {
            return false;
        }
        return redisAttrDAO.lSet(id, attr);
    }
    /**
     * 删除指定对象的属性
     * @param id 对象id
     * @param attr 属性名
     * @return true代表删除成功，false代表失败
     */
    public boolean removeAttrByObjectId(String id, String attr) {
        return redisAttrDAO.lRemove(id, 1, attr) > 0;
    }
    /**
     * 更新指定对象的属性
     * @param id 对象id
     * @param oldAttr 旧属性名
     * @param newAttr 新属性名
     * @return true代表更新成功，false代表更新失败
     */
    public boolean updateAttrByObjectId(String id, String oldAttr, String newAttr) {
        return removeAttrByObjectId(id, oldAttr) && addAttrByObjectId(id, newAttr);
    }

    /**
     * 创建对象属性表
     * @param request json请求体
     * @return true代表创建成功，false代表创建失败
     */
    public boolean createAttr(String request) {
        JSONObject jsonObject = JSON.parseObject(request);
        //必须传入id
        String id = jsonObject.getString("id");
        if(id == null) return false;
        JSONArray jsonArray = jsonObject.getJSONArray("attr");
        List<String> attr = jsonArray==null ? new ArrayList<>() : JSONObject.parseArray(jsonArray.toJSONString(), String.class);
        //这里传入的属性必须不能重复
        return redisAttrDAO.lSet(id, attr.toArray());
    }

    /**
     * 向属性值表中插入一条记录
     * @param id 对象id
     * @param mongoAttr 属性
     * @return true代表插入成功，false代表插入失败
     */
    public boolean addAttr(String id, String name, MongoAttr mongoAttr) {
        return addAttr(id, name, mongoAttr.getValue(), mongoAttr.getUpdateTime());
    }
    /**
     * 属性值表:
     * key=id+name+time->|time1|value1|
     *                   |time2|value2|
     *                   |time3|value3|
     *                   ...
     * 向属性值表中插入一条记录
     * @param id 对象id
     * @param attr 对象属性名
     * @param value 属性值
     * @param date 日期，精确到秒，格式为:yyyy-MM-dd HH:mm:ss，如:2019-06-01
     * @return true代表插入成功，false代表插入失败
     */
    public boolean addAttr(String id, String attr, String value, Date date) {
        if(id == null || attr == null || value == null) {
            //不允许任何一个参数为null值
            return false;
        }
        List<String> attrList = (List<String>) redisAttrDAO.lGet(id, 0, -1);
        if(attrList == null || attrList.size() == 0) {
            return false;
        }
        //将属性加入属性表中
        addAttrByObjectId(id, attr);
        //更新时间
        redisAttrDAO.hset(id + "#META", "updateTime", date);
        /*
        if(redisObjectDAO.hget(id + '#' + attr, "createTime") == null) {
            redisObjectDAO.hset(id + '#' + attr, "createTime", date);
        }
        */
        redisObjectDAO.hset(id + '#' + attr, "createTime", date);
        redisObjectDAO.hset(id + '#' + attr, "updateTime", date);
        //拼接成key
        String key = id + '#' + attr + '#' + "time";
        //检查表的大小
        if(cacheSize <= redisObjectDAO.Zcard(key)) {
            doEvict(id, attr);
            redisObjectDAO.Zadd(key, value, (double)date.getTime());
        }
        else {
            redisObjectDAO.Zadd(key, value, (double)date.getTime());
        }
        return true;
    }

    /**
     * 执行淘汰策略
     * @param id 对象id
     * @param attr 属性名
     */
    private void doEvict(String id, String attr) {
        String key = id + '#' + attr + '#' + "time";
        //超出了最大允许值,执行淘汰策略
        //淘汰策略:将当前属性表中最小元素删除,同时记录其时间戳,按照时间戳将同一对象中所有属性在此时间戳之前的值全部删除,
        // 同时添加一条新记录,值为被删除的最新记录的值,如:
        //attr A:| 08:00:00 |      attr B:| 07:00:00 |
        //       | 09:00:00 |             | 07:30:00 |
        //       | 10:00:00 |             | 09:30:00 |
        //此时向A中插入一条新纪录:| 2019-10-21 | 11:00:00 |,触发了淘汰动作,A中第一条记录被删除
        //为了查询一致性考虑,相应必须删除B中第一条记录和第二条记录,并插入一条新记录| 09:00:00 |,对应值与| 07:30:00 |的值相同。
        //最后表的情况如下图所示:
        //attr A:| 09:00:00 |      attr B:| 09:00:00 |
        //       | 10:00:00 |             | 09:30:00 |
        //       | 11:00:00 |
        //为了提高效率,每次淘汰一批数据而不是一个数据,以避免频繁触发淘汰动作
        //记录最后被淘汰值的时间戳
        Set<ZSetOperations.TypedTuple<Object>> tempSet = redisObjectDAO.ZrangeWithScores(key, evictSize, evictSize);
        Iterator<ZSetOperations.TypedTuple<Object>> it = tempSet.iterator();
        ZSetOperations.TypedTuple<Object> tuple = it.next();
        //时间戳
        double d = tuple.getScore();
        //执行淘汰策略
        redisObjectDAO.ZremoveRange(key, 0, evictSize - 1);
        //redisObjectDAO.Zadd(key, value, (double)date.getTime());
        List<String> attrList = (List<String>) redisAttrDAO.lGet(id, 0, -1);
        for(Object everyAttr : attrList) {
            if(!everyAttr.equals(attr)) {
                String k = id + '#' + everyAttr + '#' + "time";
                //删除
                long cnt = redisObjectDAO.Zcount(k, 0.0, d);
                if(cnt > 0) {
                    Set<Object> delSet = redisObjectDAO.Zrange(k, cnt - 1, cnt - 1);
                    Iterator<Object> delIt = delSet.iterator();
                    Object lastValue = delIt.next();
                    redisObjectDAO.ZremoveRangeByScore(k, 0.0, d);
                    //添加
                    redisObjectDAO.Zadd(k, lastValue, d);
                }
            }
        }
    }

    /**
     * 根据对象id和属性名查找最新属性值
     * @param id 对象id
     * @param attr 属性名
     * @return 属性值
     */
    public MongoAttr findAttrByKey(String id, String attr) {
        if(id == null || attr == null) {
            return null;
        }
        String key = id + '#' + attr + '#' + "time";
        long cnt = redisObjectDAO.Zcard(key);
        if(cnt > 0) {
            //获得属性最新值
            Set<Object> delSet = redisObjectDAO.Zrange(key, cnt - 1, cnt - 1);
            Iterator<Object> delIt = delSet.iterator();
            Object lastValue = delIt.next();
            MongoAttr mongoAttr = new MongoAttr(lastValue.toString());
            //添加更新时间和创建时间
            Date attrUpdateTime = (Date) redisObjectDAO.hget(id + '#' + attr, "updateTime");
            Date attrCreateTime = (Date) redisObjectDAO.hget(id + '#' + attr, "createTime");

            mongoAttr.setUpdateTime(attrUpdateTime);
            mongoAttr.setCreateTime(attrCreateTime);
            return mongoAttr;
        }
        return null;
    }

    /**
     * 查找某个时间点的属性值
     * @param id 对象id
     * @param attr 属性名
     * @param date 时间
     * @return
     */
    public MongoAttr findAttrByTime(String id, String attr, Date date) {
        String key = id + '#' + attr + '#' + "time";
        System.out.println(date.getTime());
        long cnt = redisObjectDAO.Zcount(key, 0.0, (double)date.getTime());
        //如果没有符合条件的记录，说明给定时间的记录不存在或已被淘汰，需要去mongoDB中查找
        if(cnt == 0) {
            return null;
        }
        //此处如果给定时间大于数据库中所有记录对应的时间，则取数据库中最后一条记录
        Set<ZSetOperations.TypedTuple<Object>> latestSet = redisObjectDAO.ZrangeWithScores(key, cnt - 1, cnt - 1);
        Iterator<ZSetOperations.TypedTuple<Object>> delIt = latestSet.iterator();
        ZSetOperations.TypedTuple<Object> lastValue = delIt.next();
        MongoAttr mongoAttr = new MongoAttr(lastValue.getValue().toString());
        Date attrCreateTime = (Date) redisObjectDAO.hget(id + '#' + attr, "createTime");
        mongoAttr.setCreateTime(attrCreateTime);
        Date attrUpdateTime = new Date(lastValue.getScore().longValue());
        mongoAttr.setUpdateTime(attrUpdateTime);
        return mongoAttr;
    }
    /**
     * 根据对象id查找对象的最新值
     * @param id 对象id
     * @return 对象最新值
     */
    public MongoObject findObjectById(String id) {
        if(id == null) {
            return null;
        }
        List<String> attrList = (List<String>) redisAttrDAO.lGet(id, 0, -1);
        if(attrList == null || attrList.size() == 0) {
            return null;
        }
        MongoObject mongoObject = findById(id);
        if(mongoObject == null) {
            return null;
        }
        Date ut = new Date(0);
        for(Object everyAttr : attrList) {
            MongoAttr mongoAttr = findAttrByKey(id, everyAttr.toString());
            if(mongoAttr != null) {
                mongoObject.putAttr(everyAttr.toString(), mongoAttr);
                if(ut.before(mongoAttr.getUpdateTime())) {
                    ut = mongoAttr.getUpdateTime();
                }
            }
        }
        //关联对象
        mongoObject.setUpdateTime(ut);
        HashMap<String, Date> cutMap = cutObjects(ut, id);
        mongoObject.setObjects(cutMap);
        return mongoObject;
    }

    /**
     * 工具函数
     * 从关联对象表objectMap消除时间ut之后的关联对象
     * @param ut
     * @param id 对象id
     * @return 新的关联对象表
     */
    private HashMap<String, Date> cutObjects(Date ut, String id) {
        HashMap<String, Date> objectMap = (HashMap<String, Date>) redisAttrDAO.hmget(id + "#object");
        HashMap<String, Date> cutMap = new HashMap<String, Date>();
        for(Map.Entry<String, Date> entry : objectMap.entrySet()) {
            String objId = entry.getKey();
            Date objDate = entry.getValue();
            if(!objDate.after(ut)) {
                cutMap.put(objId, objDate);
            }
        }
        return cutMap;
    }
    /**
     * 工具函数
     * 根据对象id获得对象的元信息
     * @param id 对象id
     * @return 初始化的MongoObject对象
     */
    private MongoObject findById(String id) {
        if(redisAttrDAO.hsize(id + "#META") > 0) {
            String template = redisAttrDAO.hget(id + "#META", "template").toString();
            String nodeId = redisAttrDAO.hget(id + "#META", "nodeId").toString();
            String type = redisAttrDAO.hget(id + "#META", "type").toString();
            String intro = redisAttrDAO.hget(id + "#META", "intro") != null ? redisAttrDAO.hget(id + "#META", "intro").toString(): "";
            Date createTime = (Date) redisAttrDAO.hget(id + "#META", "createTime");
            Date updateTime = (Date) redisAttrDAO.hget(id + "#META", "updateTime");
            MongoObject mongoObject = new MongoObject(id, intro, type, template, nodeId, new HashMap<String, MongoAttr>(), new HashMap<String, Date>());
            mongoObject.setCreateTime(createTime);
            mongoObject.setUpdateTime(updateTime);
            return mongoObject;
        }
        return null;
    }
    /**
     * 根据对象id和给定时间查找对象在某一时刻的值
     * @param id 对象id
     * @param date 日期
     * @return 初始化的MongoObject对象
     */
    public MongoObject findObjectById(String id, Date date) {
        if(id == null) {
            return null;
        }
        List<String> attrList = (List<String>) redisAttrDAO.lGet(id, 0, -1);
        if(attrList == null || attrList.size() == 0) {
            return null;
        }
        MongoObject mongoObject = findById(id);
        if(mongoObject == null) {
            return null;
        }
        Date ut = new Date(0);
        for(Object everyAttr : attrList) {
            MongoAttr mongoAttr = findAttrByTime(id, everyAttr.toString(), date);
            if(mongoAttr != null) {
                mongoObject.putAttr(everyAttr.toString(), mongoAttr);
                if(ut.before(mongoAttr.getUpdateTime())) {
                    ut = mongoAttr.getUpdateTime();
                }
            }
        }
        mongoObject.setUpdateTime(ut);
        HashMap<String, Date> cutMap = cutObjects(ut, id);
        mongoObject.setObjects(cutMap);
        return mongoObject;
    }

    /**
     * 根据对象id获得一段时间内对象的状态集合
     * @param id 对象id
     * @param startDate 起始时间
     * @param endDate 结束时间
     * @return
     */
    public List<MongoObject> findObjectBetweenTime(String id, Date startDate, Date endDate) {
        if(id == null) {
            return null;
        }
        List<String> attrList = (List<String>) redisAttrDAO.lGet(id, 0, -1);

        if(attrList == null || attrList.size() == 0) {
            return null;
        }
        List<ArrayList<ZSetOperations.TypedTuple<Object>>> arr = new ArrayList<ArrayList<ZSetOperations.TypedTuple<Object>>>();
        int i = 0;
        for(Object everyAttr : attrList) {
            String key = id + '#' + everyAttr + '#' + "time";
            //System.out.println(key);
            Set<ZSetOperations.TypedTuple<Object>> tempSet = redisObjectDAO.ZrangeByScoreWithScores(key, (double)startDate.getTime(), (double)endDate.getTime());
            arr.add(new ArrayList<ZSetOperations.TypedTuple<Object>>(tempSet));
            //按时间排序
            Collections.sort(arr.get(i), new Comparator<ZSetOperations.TypedTuple<Object>>() {
                public int compare(ZSetOperations.TypedTuple<Object> t1, ZSetOperations.TypedTuple<Object> t2) {
                    if(t1.getScore() > t2.getScore()) {
                        return 1;
                    }
                    else if(t1.getScore() < t2.getScore()) {
                        return -1;
                    }
                    else {
                        return 0;
                    }
                }
            });
            i += 1;
        }
        //记录前一个时间点的数据
        List<ZSetOperations.TypedTuple<Object>> pre = new ArrayList<ZSetOperations.TypedTuple<Object>>(attrList.size());
        //每一个属性当前扫描到的位置
        List<Integer> indexList = new ArrayList<Integer>(attrList.size());

        //存储最终返回结果
        List<MongoObject> retList = new ArrayList<MongoObject>();
        //初始化
        for(i = 0; i < attrList.size(); i++) {
            //从第一个数据开始
            indexList.add(0);
            pre.add(null);
        }
        //初始化(待完成)
        for(i = 0; i < attrList.size(); i++) {
            String key = id + '#' + attrList.get(i) + '#' + "time";
            //获得小于或等于起始时间的最大的数
            long cnt = redisObjectDAO.Zcount(key, 0.0, (double)startDate.getTime());
            if(cnt > 0) {
                Set<ZSetOperations.TypedTuple<Object>> delSet = redisObjectDAO.ZrangeWithScores(key, cnt - 1, cnt - 1);
                Iterator<ZSetOperations.TypedTuple<Object>> delIt = delSet.iterator();
                ZSetOperations.TypedTuple<Object> lastValue = delIt.next();
                pre.set(i, lastValue);
            }
            else {
                //如果全部大于起始时间
                Set<ZSetOperations.TypedTuple<Object>> delSet = redisObjectDAO.ZrangeWithScores(key, 0,  0);
                Iterator<ZSetOperations.TypedTuple<Object>> delIt = delSet.iterator();
                ZSetOperations.TypedTuple<Object> lastValue = delIt.next();
                pre.set(i, lastValue);
            }

        }
        //对完成排序的数据进行处理
        boolean isComplete = false;
        while(!isComplete) {
            //是否有剩余数据待处理
            boolean notRemain = true;
            MongoObject mongoObject = findById(id);
            double minTime = Double.MAX_VALUE;
            for(i = 0; i < attrList.size(); i++) {
                //当前属性还没有扫描到最后一个数据
                if(indexList.get(i) < arr.get(i).size()) {
                    notRemain = false;
                    if(arr.get(i).get(indexList.get(i)).getScore() < minTime) {
                        minTime = arr.get(i).get(indexList.get(i)).getScore();
                    }
                }
            }
            //如果已经没有剩余元素，就完成
            if(notRemain) {
                isComplete = true;
            }
            else {
                //设置更新时间
                mongoObject.setUpdateTime(new Date(new Double(minTime).longValue()));
                for(i = 0; i < attrList.size(); i++) {
                    //属性名
                    String attrName = attrList.get(i).toString();
                    Date attrCreateTime = (Date) redisObjectDAO.hget(id + '#' + attrName, "createTime");
                    Date attrUpdateTime;
                    MongoAttr mongoAttr;
                    if(indexList.get(i) < arr.get(i).size()) {
                        //填入各属性值
                        if (arr.get(i).get(indexList.get(i)).getScore() != minTime) {
                            mongoAttr = new MongoAttr(pre.get(i).getValue().toString());
                            attrUpdateTime = new Date(pre.get(i).getScore().longValue());
                        } else {
                            mongoAttr = new MongoAttr(arr.get(i).get(indexList.get(i)).getValue().toString());
                            attrUpdateTime = new Date(new Double(minTime).longValue());
                            pre.set(i, arr.get(i).get(indexList.get(i)));
                            indexList.set(i, indexList.get(i) + 1);
                        }
                        mongoAttr.setUpdateTime(attrUpdateTime);
                    }
                    else {
                        mongoAttr = new MongoAttr(pre.get(i).getValue().toString());
                        attrUpdateTime = new Date(pre.get(i).getScore().longValue());
                        mongoAttr.setUpdateTime(attrUpdateTime);
                    }
                    mongoAttr.setCreateTime(attrCreateTime);
                    HashMap<String, Date> cutMap = cutObjects(mongoObject.getUpdateTime(), id);
                    mongoObject.setObjects(cutMap);
                    mongoObject.putAttr(attrName, mongoAttr);
                }
                retList.add(mongoObject);
            }
        }
        return retList;

        //pretty print
        /*
        for(int j = 0; j < arr.size(); j++) {
            for(int k = 0; k < arr.get(j).size(); k++) {
                System.out.print(arr.get(j).get(k).getValue() + " ");
            }
            System.out.println();
        }
        */
    }
    /**
     * 根据对象id删除对象
     * @param id 对象id
     * @return true代表删除成功,false代表删除失败
     */
    /*未完成
    public boolean delObjectById(String id) {
        redisDAO.switchToAttrRedisTemplate();
        //id必须非空
        if(id == null) {
            return false;
        }
        Set<Object> attrSet = redisDAO.sGet(id);
        //对象不存在，删除失败
        if(attrSet.size() == 0) {
            return false;
        }
        redisDAO.switchToObjectRedisTemplate();
        //按照属性分别删除
        for(Object everyAttr : attrSet) {
            String key = id + '#' + everyAttr + '#' + "time";
            redisDAO.ZremoveRange(key, 0, -1);
        }
        //最后删除属性表

        return true;

    }
    */

}
