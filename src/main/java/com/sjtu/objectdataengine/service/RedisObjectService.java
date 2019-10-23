package com.sjtu.objectdataengine.service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
    private RedisDAO redisDAO;
    private static final Logger logger = LoggerFactory.getLogger(RedisObjectService.class);
    /**
     * 根据对象id获取对象所有属性
     * @param id 对象id
     * @return 对象属性集合
     */
    /*public Set<Object> findAttrByObjectId(String id) {
        return redisDAO.sGet(id);
    }
    */
    public List<Object> findAttrByObjectId(String id) {
        redisDAO.switchToAttrRedisTemplate();
        return redisDAO.lGet(id, 0, -1);
    }

    /**
     * 为指定对象添加属性
     * @param id 对象id
     * @param attr 属性名
     * @return true代表插入成功，false代表失败
     */
    /*
    public boolean addAttrByObjectId(String id, String attr) {
        //如果属性已存在，插入失败
        if(redisDAO.sHasKey(id, attr)) {
            return false;
        }
        if(redisDAO.sSet(id, attr) > 0) {
            return true;
        }
        return false;
    }
    */
    public boolean addAttrByObjectId(String id, String attr) {
        redisDAO.switchToAttrRedisTemplate();
        //如果属性已存在，插入失败
        List<Object> attrList = redisDAO.lGet(id, 0, -1);
        for(Object everyAttr : attrList) {
            if(everyAttr.equals(attr)) {
                return false;
            }
        }
        return redisDAO.lSet(id, attr);
    }
    /**
     * 删除指定对象的属性
     * @param id 对象id
     * @param attr 属性名
     * @return true代表删除成功，false代表失败
     */
    /*
    public boolean removeAttrByObjectId(String id, String attr) {
        //如果属性名不存在，删除失败
        if(!redisDAO.sHasKey(id, attr)) {
            return false;
        }
        if(redisDAO.setRemove(id, attr) > 0) {
            return true;
        }
        return false;
    }
    */
    public boolean removeAttrByObjectId(String id, String attr) {
        redisDAO.switchToAttrRedisTemplate();
        return redisDAO.lRemove(id, 1, attr) > 0;
    }
    /**
     * 更新指定对象的属性
     * @param id 对象id
     * @param oldAttr 旧属性名
     * @param newAttr 新属性名
     * @return true代表更新成功，false代表更新失败
     */
    public boolean updateAttrByObjectId(String id, String oldAttr, String newAttr) {
        redisDAO.switchToAttrRedisTemplate();
        return removeAttrByObjectId(id, oldAttr) && addAttrByObjectId(id, newAttr);
    }

    /**
     * 创建对象属性表
     * @param request json请求体
     * @return true代表创建成功，false代表创建失败
     */
    /*
    public boolean createAttr(String request) {
        JSONObject jsonObject = JSON.parseObject(request);
        //必须传入id
        String id = jsonObject.getString("id");
        if(id == null) return false;
        JSONArray jsonArray = jsonObject.getJSONArray("attr");
        List<String> attr = jsonArray==null ? new ArrayList<>() : JSONObject.parseArray(jsonArray.toJSONString(), String.class);
        return redisDAO.sSet(id, attr.toArray()) > 0;
    }
    */
    public boolean createAttr(String request) {
        redisDAO.switchToAttrRedisTemplate();
        JSONObject jsonObject =  JSON.parseObject(request);
        //必须传入id
        String id = jsonObject.getString("id");
        if(id == null) return false;
        JSONArray jsonArray = jsonObject.getJSONArray("attr");
        List<String> attr = jsonArray==null ? new ArrayList<>() : JSONObject.parseArray(jsonArray.toJSONString(), String.class);
        //这里传入的属性必须不能重复
        return redisDAO.lSet(id, attr.toArray());
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
    public boolean Zadd(String id, String attr, String value, Date date) {
        redisDAO.switchToAttrRedisTemplate();
        if(id == null || attr == null || value == null) {
            //不允许任何一个参数为null值
            return false;
        }
        List<Object> attrList = redisDAO.lGet(id, 0, -1);

        redisDAO.switchToObjectRedisTemplate();
        //拼接成key
        String key = id + '#' + attr + '#' + "time";
        //检查表的大小
        if(cacheSize <= redisDAO.Zcard(key)) {
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
            Set<ZSetOperations.TypedTuple<Object>> tempSet = redisDAO.ZrangeWithScores(key, evictSize, evictSize);
            Iterator<ZSetOperations.TypedTuple<Object>> it = tempSet.iterator();
            ZSetOperations.TypedTuple<Object> tuple = it.next();
            //时间戳
            double d = tuple.getScore();
            //执行淘汰策略
            redisDAO.ZremoveRange(key, 0, evictSize - 1);
            redisDAO.Zadd(key, value, (double)date.getTime());
            for(Object everyAttr : attrList) {
                if(!everyAttr.equals(attr)) {
                    String k = id + '#' + everyAttr + '#' + "time";
                    //删除
                    long cnt = redisDAO.Zcount(k, 0.0, d);
                    Set<Object> delSet = redisDAO.Zrange(k, cnt - 1, cnt - 1);
                    Iterator<Object> delIt = delSet.iterator();
                    Object lastValue = delIt.next();
                    redisDAO.ZremoveRangeByScore(k, 0.0, d);
                    //添加
                    redisDAO.Zadd(k, lastValue, d);
                }
            }
        }
        else {
            redisDAO.Zadd(key, value, (double)date.getTime());
        }
        return true;
    }

    /**
     * 根据对象id查找对象的最新值
     * @param id 对象id
     * @return 对象最新值
     */
    public JSONObject findObjectById(String id) {
        redisDAO.switchToAttrRedisTemplate();
        if(id == null) {
            return null;
        }
        List<Object> attrList = redisDAO.lGet(id, 0, -1);
        if(attrList.size() == 0) {
            return null;
        }
        redisDAO.switchToObjectRedisTemplate();
        JSONObject jsonObject = new JSONObject();
        for(Object everyAttr : attrList) {
            String key = id + '#' + everyAttr + '#' + "time";
            //获得属性最新值
            long cnt = redisDAO.Zcard(key);
            Set<Object> delSet = redisDAO.Zrange(key, cnt - 1, cnt - 1);
            Iterator<Object> delIt = delSet.iterator();
            Object lastValue = delIt.next();
            //cast
            jsonObject.put(everyAttr.toString(), lastValue);
        }
        return jsonObject;
    }

    /**
     * 根据对象id和给定时间查找对象在某一时刻的值
     * @param id 对象id
     * @param date 日期
     */
    public JSONObject findObjectById(String id, Date date) {
        redisDAO.switchToAttrRedisTemplate();
        if(id == null) {
            return null;
        }
        List<Object> attrList = redisDAO.lGet(id, 0, -1);
        if(attrList.size() == 0) {
            return null;
        }
        redisDAO.switchToObjectRedisTemplate();
        JSONObject jsonObject = new JSONObject();
        for(Object everyAttr : attrList) {
            String key = id + '#' + everyAttr + '#' + "time";
            long cnt = redisDAO.Zcount(key, 0.0, (double)date.getTime());
            //如果没有符合条件的记录，说明给定时间的记录不存在或已被淘汰，需要去mongoDB中查找
            if(cnt == 0) {
                return null;
            }
            //此处如果给定时间大于数据库中所有记录对应的时间，则取数据库中最后一条记录
            Set<Object> latestSet = redisDAO.Zrange(key, cnt - 1, cnt - 1);
            Iterator<Object> delIt = latestSet.iterator();
            Object lastValue = delIt.next();
            jsonObject.put(everyAttr.toString(), lastValue);
        }
        return jsonObject;
    }

    /**
     * 根据对象id获得一段时间内对象的状态集合
     * @param id 对象id
     * @param startDate 起始时间
     * @param endDate 结束时间
     * @return
     */
    public List<JSONObject> findObjectBetweenTime(String id, Date startDate, Date endDate) {

        return null;
    }
    /**
     * 根据对象id删除对象
     * @param id 对象id
     * @return true代表删除成功,false代表删除失败
     */
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


}
