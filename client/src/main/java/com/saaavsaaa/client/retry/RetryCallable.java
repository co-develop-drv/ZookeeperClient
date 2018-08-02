package com.saaavsaaa.client.retry;

import com.saaavsaaa.client.action.IProvider;
import com.saaavsaaa.client.zookeeper.section.Connection;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by aaa
 */
public abstract class RetryCallable {
    private static final Logger logger = LoggerFactory.getLogger(RetryCallable.class);

    protected final DelayPolicyExecutor delayPolicyExecutor;
    protected final IProvider provider;

    public RetryCallable(final IProvider provider, final DelayRetryPolicy delayRetryPolicy) {
        this.delayPolicyExecutor = new DelayPolicyExecutor(delayRetryPolicy);
        this.provider = provider;
    }
    
    public abstract void call() throws KeeperException, InterruptedException;
    
    
    public void exec() throws KeeperException, InterruptedException {
        try {
            call();
        } catch (KeeperException e) {
            logger.warn("exec KeeperException:{}", e.getMessage());
            delayPolicyExecutor.next();
            if (Connection.needReset(e)) {
                provider.resetConnection();
            }
            execDelay();
        } catch (InterruptedException e) {
            throw e;
        }
    }
    
    protected void execDelay() throws KeeperException, InterruptedException {
        for (;;) {
            long delay = delayPolicyExecutor.getNextTick() - System.currentTimeMillis();
            if (delay > 0) {
                try {
                    logger.debug("exec delay:{}", delay);
                    Thread.sleep(delay);
                } catch (InterruptedException ee) {
                    throw ee;
                }
            } else {
                if (delayPolicyExecutor.hasNext()) {
                    logger.debug("exec hasNext");
                    exec();
                }
                break;
            }
        }
    }
}
