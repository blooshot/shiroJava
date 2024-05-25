package com.hap.shiro.locking;


import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Service
public class DistributedLocker implements DistributedLock {

    private final Logger log = LoggerFactory.getLogger(DistributedLocker.class);

    @Inject
    @Qualifier("redissonClient")
    private RedissonClient redissonClient;

    @Override
    public boolean acquireLock(String key, long lockLeaseTime, long waitTimeToLock) {
        RLock rLock = redissonClient.getLock(key);
        log.info("Requesting to Acquire Lock.");

        if (rLock.isLocked() && rLock.isHeldByCurrentThread()) {
            return true;
        }
        boolean isLockAcquired = false;
        try {
            isLockAcquired = rLock.tryLock(waitTimeToLock, lockLeaseTime, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("Exception in acquiring lock: {}" + e.getMessage() + " "+ Arrays.toString(e.getStackTrace()), e);
        }
        return isLockAcquired;
    }

    @Override
    public boolean releaseLock(String key) {
        RLock rLock = redissonClient.getLock(key);
        if (rLock.isLocked() && rLock.isHeldByCurrentThread()) {
            rLock.unlock();
            return true;
        }
        return false;
    }

    public boolean isLocked(String key){
        RLock rLock = redissonClient.getLock(key);
        return rLock.isLocked();
    }
}
