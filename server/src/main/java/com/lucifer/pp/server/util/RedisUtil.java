package com.lucifer.pp.server.util;

import com.lucifer.pp.common.base.BaseConstant;
import com.lucifer.pp.server.PPServerContext;
import com.lucifer.pp.server.pojo.HeartBeatContext;
import com.lucifer.pp.server.pojo.LockUser;
import jakarta.annotation.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {
    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    public void setStringValue(String key,Object value){
        redisTemplate.opsForValue().set(key,value);
    }

    public Object getStringValue(String key){
        return redisTemplate.opsForValue().get(key);
    }

    public void setKeyExpire(String key,Long seconds){
        redisTemplate.expire(key,seconds,TimeUnit.SECONDS);
    }

    public void setHashValue(String key,Object hashKey,Object value){
        redisTemplate.opsForHash().put(key,hashKey,value);
    }

    public Object getHashValue(String key,String hashKey){
        return redisTemplate.opsForHash().get(key,hashKey);
    }

    public boolean willBeLocked(Long uid){
        return redisTemplate.opsForHash().hasKey(BaseConstant.REDIS_LOCK_USER_KEY,String.valueOf(uid));
    }

    public void setLockUser(Long uid, int errorCount){
        LockUser lockUser = new LockUser(uid,errorCount);
        redisTemplate.opsForHash().put(BaseConstant.REDIS_LOCK_USER_KEY,String.valueOf(uid),lockUser);
    }

    public LockUser getLockUser(Long uid){
        return (LockUser) redisTemplate.opsForHash().get(BaseConstant.REDIS_LOCK_USER_KEY, String.valueOf(uid));
    }

    public boolean isLocked(Long uid){
        return redisTemplate.hasKey(BaseConstant.REDIS_LOCKED_USER_KEY+uid);
    }

    public void lockUser(Long uid){
        redisTemplate.execute(new SessionCallback<List<Object>>() {
            @Override
            public List<Object> execute(RedisOperations operations) throws DataAccessException {
                operations.multi();
                operations.opsForValue().set(BaseConstant.REDIS_LOCKED_USER_KEY+uid,"");
                redisTemplate.expire(BaseConstant.REDIS_LOCKED_USER_KEY+uid,BaseConstant.LOCK_TIME,TimeUnit.MILLISECONDS);
                return operations.exec();
            }
        });
    }

    public void removeLockUser(Long uid){
        redisTemplate.opsForHash().delete(BaseConstant.REDIS_LOCK_USER_KEY,String.valueOf(uid));
    }

    public void setHeartBeat(Long uid, String token, String ip){
        try {
            PPServerContext.heartBeatLock.writeLock().lock();
            HeartBeatContext heartBeatContext = new HeartBeatContext(token,ip,System.currentTimeMillis());
            redisTemplate.execute(new SessionCallback<List<Object>>() {
                @Override
                public List<Object> execute(RedisOperations operations) throws DataAccessException {
                    operations.multi();
                    operations.opsForHash().put(BaseConstant.REDIS_ONLINE_USER_KEY,String.valueOf(uid),heartBeatContext);
                    return operations.exec();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            PPServerContext.heartBeatLock.writeLock().unlock();
        }
    }

    public boolean isOnline(Long uid){
        PPServerContext.heartBeatLock.readLock().lock();
        boolean result = redisTemplate.opsForHash().hasKey(BaseConstant.REDIS_ONLINE_USER_KEY,String.valueOf(uid));
        PPServerContext.heartBeatLock.readLock().unlock();
        return result;
    }

    public HeartBeatContext getHeartBeatContext(Long uid){
        PPServerContext.heartBeatLock.readLock().lock();
        HeartBeatContext result = (HeartBeatContext) redisTemplate.opsForHash().get(BaseConstant.REDIS_ONLINE_USER_KEY,String.valueOf(uid));
        PPServerContext.heartBeatLock.readLock().unlock();
        return result;
    }

    public List<Object> getAllHeartBeat(){
        PPServerContext.heartBeatLock.readLock().lock();
        List<Object> result = redisTemplate.opsForHash().values(BaseConstant.REDIS_ONLINE_USER_KEY);
        PPServerContext.heartBeatLock.readLock().unlock();
        return result;
    }

    public void removeOnlineUser(Long uid){
        try{
            PPServerContext.heartBeatLock.writeLock().lock();
            redisTemplate.opsForHash().delete(BaseConstant.REDIS_ONLINE_USER_KEY,String.valueOf(uid));
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            PPServerContext.heartBeatLock.writeLock().unlock();
        }

    }
}
