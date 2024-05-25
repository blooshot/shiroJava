package com.hap.shiro.locking;

public interface DistributedLock {

    /**
     * <p><strong>Acquires the lock.</strong></p>
     * Acquire the lock immediately for specified <strong>leaseTime</strong>, if LOCK is available.
     * <br/>
     * Else, Wait for specified <strong>waitTime</strong> to be re-try to attempt for lock.
     * @param leaseTime Wait time if lock is not available.
     * @param waitTime Hold time after acquired the lock
     * @return true if lock acquired.
     */
    boolean acquireLock(String key, long leaseTime, long waitTime);

    /**
     * <p><strong>Release the lock.</strong></p>
     * @return true if <strong>Lock</strong> released, false if <strong>Already</strong> released.
     */
    boolean releaseLock(String key);
}
