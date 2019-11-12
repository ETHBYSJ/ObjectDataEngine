import redis
import time
def transDate(date):
    #e.g. date='2019-10-22 13:00:00'
    timeArray = time.strptime(date, "%Y-%m-%d %H:%M:%S")
    timeStamp = time.mktime(timeArray)
    return timeStamp * 1000
if __name__ == "__main__":
    #pool = redis.ConnectionPool(host = 'localhost', port = 6379, password = None)
    #r = redis.StrictRedis(connection_pool = pool)
    id = '1'
    '''
    attrs = ['age', 'name']
    r = redis.Redis(host = 'localhost', port = 6379, db = 0, password = None)
    r.lpush(id, '\"name\"')
    r.lpush(id, '\"age\"')
    '''
    r = redis.Redis(host = 'localhost', port = 6379, db = 1, password = None)
    #zset

    r.zremrangebyrank(id + '#age#time', 0, -1)
    r.zremrangebyrank(id + '#name#time', 0, -1)

    r.zadd(id + '#age#time', {'\"1\"':transDate('2019-11-12 14:00:00')})
    r.zadd(id + '#age#time', {'\"2\"':transDate('2019-11-12 15:00:00')})
    r.zadd(id + '#age#time', {'\"3\"':transDate('2019-11-12 16:00:00')})
    r.zadd(id + '#age#time', {'\"4\"':transDate('2019-11-12 17:00:00')})
    r.zadd(id + '#age#time', {'\"5\"':transDate('2019-11-12 18:00:00')})

    r.zadd(id + '#name#time', {'\"a\"':transDate('2019-11-12 13:00:00')})
    r.zadd(id + '#name#time', {'\"b\"':transDate('2019-11-12 13:30:00')})
    '''
    r.zadd(id + '#name#time', {'\"c\"':transDate('2019-11-12 14:00:00')})
    r.zadd(id + '#name#time', {'\"d\"':transDate('2019-11-12 16:30:00')})
    r.zadd(id + '#name#time', {'\"e\"':transDate('2019-11-12 18:00:00')})
    '''
