import redis
import time
def transDate(date):
    #e.g. date='2019-10-22 13:00:00'
    timeArray = time.strptime(date, "%Y-%m-%d %H:%M:%S")
    timeStamp = time.mktime(timeArray)
    return timeStamp * 1000
if __name__ == "__main__":
    pool = redis.ConnectionPool(host = 'localhost', port = 6379, password = None)
    r = redis.StrictRedis(connection_pool = pool)
    id = '2'
    attrs = ['age', 'name']
    #set
    '''
    r.srem("2", 2)
    r.sadd("2", '\"name\"')
    r.sadd("2", '\"age\"')
    '''
    #zset
    r.zremrangebyrank(id + '#age#time', 0, -1)
    r.zremrangebyrank(id + '#name#time', 0, -1)

    r.zadd(id + '#age#time', {'\"1\"':transDate('2019-10-22 13:00:00')})
    r.zadd(id + '#age#time', {'\"2\"':transDate('2019-10-22 14:00:00')})
    r.zadd(id + '#age#time', {'\"3\"':transDate('2019-10-22 15:00:00')})
    r.zadd(id + '#age#time', {'\"4\"':transDate('2019-10-22 16:00:00')})
    r.zadd(id + '#age#time', {'\"5\"':transDate('2019-10-22 17:00:00')})

    r.zadd(id + '#name#time', {'\"a\"':transDate('2019-10-22 13:00:00')})
    r.zadd(id + '#name#time', {'\"b\"':transDate('2019-10-22 13:30:00')})
    r.zadd(id + '#name#time', {'\"c\"':transDate('2019-10-22 14:00:00')})
    r.zadd(id + '#name#time', {'\"d\"':transDate('2019-10-22 16:30:00')})
    r.zadd(id + '#name#time', {'\"e\"':transDate('2019-10-22 18:00:00')})
