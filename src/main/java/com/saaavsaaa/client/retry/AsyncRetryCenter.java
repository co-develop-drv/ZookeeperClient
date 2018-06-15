package com.saaavsaaa.client.retry;

import com.saaavsaaa.client.zookeeper.base.BaseOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.DelayQueue;

/*
 * async retry center
 *
 * Created by aaa
 */
public enum AsyncRetryCenter {
    INSTANCE;
    
    private static final Logger logger = LoggerFactory.getLogger(AsyncRetryCenter.class);
    private final DelayQueue<BaseOperation> queue = new DelayQueue<>();
    private final RetryThread retryThread = new RetryThread(queue);
    
    private boolean started = false;
    private DelayRetryPolicy delayRetryPolicy;
    
    /**
     * init.
     *
     * @param delayRetryPolicy delayRetryPolicy
     */
    public void init(final DelayRetryPolicy delayRetryPolicy) {
        logger.debug("delayRetryPolicy init");
        if (delayRetryPolicy == null) {
            logger.warn("delayRetryPolicy is null and auto init with DelayRetryPolicy.newNoInitDelayPolicy");
            this.delayRetryPolicy = DelayRetryPolicy.newNoInitDelayPolicy();
        }
        this.delayRetryPolicy = delayRetryPolicy;
    }
    
    /**
     * start.
     */
    public synchronized void start() {
        if (started) {
            return;
        }
        retryThread.setName("retry-thread");
        retryThread.start();
        this.started = true;
    }
    
    /**
     * add async operation.
     *
     * @param operation operation
     */
    public void add(final BaseOperation operation) {
        if (delayRetryPolicy == null) {
            logger.warn("delayRetryPolicy no init and auto init with DelayRetryPolicy.newNoInitDelayPolicy");
            delayRetryPolicy = DelayRetryPolicy.newNoInitDelayPolicy();
        }
        operation.setRetrial(new DelayPolicyExecutor(delayRetryPolicy));
        queue.offer(operation);
        logger.debug("enqueue operation:{}", operation.toString());
    }
}
