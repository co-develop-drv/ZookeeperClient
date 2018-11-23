package com.saaavsaaa.client.retry;
import com.saaavsaaa.client.action.IProvider;
import com.saaavsaaa.client.zookeeper.section.Connection;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Created by aaa
 */
public abstract class RetryResultCallable<T> extends RetryCallable {
    private static final Logger logger = LoggerFactory.getLogger(RetryResultCallable.class);
    private T result;

    public RetryResultCallable(IProvider provider, DelayRetryPolicy delayRetryPolicy) {
        super(provider, delayRetryPolicy);
    }

    public void setResult(T result) {
        this.result = result;
    }

    public T getResult() throws KeeperException, InterruptedException {
        if (result == null) {
            exec();
        }
        logger.debug("result:{}", result);
        return result;
    }
}