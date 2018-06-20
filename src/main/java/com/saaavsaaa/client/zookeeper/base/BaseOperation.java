package com.saaavsaaa.client.zookeeper.base;

import com.saaavsaaa.client.action.IProvider;
import com.saaavsaaa.client.retry.DelayPolicyExecutor;
import com.saaavsaaa.client.zookeeper.section.Connection;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/*
 * base async retry operation.
 *
 * Created by aaa
 */
public abstract class BaseOperation implements Delayed {
    private static final Logger logger = LoggerFactory.getLogger(BaseOperation.class);
    protected final IProvider provider;
    protected DelayPolicyExecutor delayPolicyExecutor;
    
    protected BaseOperation(final IProvider provider) {
        this.provider = provider;
    }
    
    public void setRetrial(final DelayPolicyExecutor delayPolicyExecutor) {
        this.delayPolicyExecutor = delayPolicyExecutor;
    }
    
    /**
     * queue precedence.
     */
    @Override
    public int compareTo(final Delayed delayed) {
        return (int) (this.getDelay(TimeUnit.MILLISECONDS) - delayed.getDelay(TimeUnit.MILLISECONDS));
    }
    
    @Override
    public long getDelay(final TimeUnit unit) {
        long absoluteBlock = this.delayPolicyExecutor.getNextTick() - System.currentTimeMillis();
        logger.debug("queue getDelay block:{}", absoluteBlock);
        long result = unit.convert(absoluteBlock, TimeUnit.MILLISECONDS);
        return result;
    }

    protected abstract void execute() throws KeeperException, InterruptedException;
    
    /**
     * queue precedence.
     *
     * @return whether or not continue enqueue
     * @throws KeeperException Keeper Exception
     * @throws InterruptedException InterruptedException
     */
    public boolean executeOperation() throws KeeperException, InterruptedException {
        boolean result;
        try {
            execute();
            result = true;
        } catch (KeeperException ee) {
            if (Connection.needReset(ee)) {
                provider.resetConnection();
            }
            result = false;
        }
        if (!result && delayPolicyExecutor.hasNext()) {
            delayPolicyExecutor.next();
            return true;
        }
        return false;
    }
}
