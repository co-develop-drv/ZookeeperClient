package com.saaavsaaa.client.zookeeper.strategy;

import com.saaavsaaa.client.zookeeper.operation.CreateAllNeedOperation;
import com.saaavsaaa.client.zookeeper.operation.DeleteAllChildrenOperation;
import com.saaavsaaa.client.zookeeper.section.Connection;
import com.saaavsaaa.client.action.IProvider;
import com.saaavsaaa.client.retry.AsyncRetryCenter;
import com.saaavsaaa.client.retry.DelayRetryPolicy;
import com.saaavsaaa.client.zookeeper.operation.DeleteCurrentBranchOperation;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by aaa
 */
public class AllAsyncRetryStrategy extends AsyncRetryStrategy {
    private static final Logger logger = LoggerFactory.getLogger(AllAsyncRetryStrategy.class);
    public AllAsyncRetryStrategy(final IProvider provider, final DelayRetryPolicy delayRetryPolicy) {
        super(provider, delayRetryPolicy);
    }

    @Override
    public void createAllNeedPath(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        try {
            super.createAllNeedPath(key, value, createMode);
        } catch (KeeperException e) {
            if (Connection.needRetry(e)) {
                logger.warn("AllAsyncRetryStrategy SessionExpiredException CreateAllNeedOperation:{}", key);
                AsyncRetryCenter.INSTANCE.add(new CreateAllNeedOperation(getProvider(), key, value, createMode));
            } else {
                throw e;
            }
        }
    }
    
    @Override
    public void deleteAllChildren(final String key) throws KeeperException, InterruptedException {
        try {
            super.deleteAllChildren(key);
        } catch (KeeperException e) {
            if (Connection.needRetry(e)) {
                logger.warn("AllAsyncRetryStrategy SessionExpiredException deleteAllChildren:{}", key);
                AsyncRetryCenter.INSTANCE.add(new DeleteAllChildrenOperation(getProvider(), key));
            } else {
                throw e;
            }
        }
    }
    
    @Override
    public void deleteCurrentBranch(final String key) throws KeeperException, InterruptedException {
        try {
            super.deleteCurrentBranch(key);
        } catch (KeeperException e) {
            if (Connection.needRetry(e)) {
                logger.warn("AllAsyncRetryStrategy SessionExpiredException deleteCurrentBranch:{}", key);
                AsyncRetryCenter.INSTANCE.add(new DeleteCurrentBranchOperation(getProvider(), key));
            } else {
                throw e;
            }
        }
    }
}
