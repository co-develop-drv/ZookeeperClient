package com.saaavsaaa.client.zookeeper.strategy;

import com.saaavsaaa.client.action.IProvider;
import com.saaavsaaa.client.retry.AsyncRetryCenter;
import com.saaavsaaa.client.retry.DelayRetryPolicy;
import com.saaavsaaa.client.zookeeper.operation.*;
import com.saaavsaaa.client.zookeeper.section.Connection;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by aaa
 */
public class AsyncRetryStrategy extends SyncRetryStrategy {
    private static final Logger logger = LoggerFactory.getLogger(AsyncRetryStrategy.class);
    
    public AsyncRetryStrategy(final IProvider provider, final DelayRetryPolicy delayRetryPolicy){
        super(provider, delayRetryPolicy);
        AsyncRetryCenter.INSTANCE.init(this.delayRetryPolicy);
        AsyncRetryCenter.INSTANCE.start();
    }

    @Override
    public void createCurrentOnly(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        String path = getProvider().getRealPath(key);
        try {
            getProvider().create(path, value, createMode);
        } catch (KeeperException ex) {
            if (Connection.needRetry(ex)) {
                logger.warn(String.format("AsyncRetryStrategy SessionExpiredException createCurrentOnly: %s", path), ex);
                AsyncRetryCenter.INSTANCE.add(new CreateCurrentOperation(getProvider(), path, value, createMode));
            } else {
                throw ex;
            }
        }
    }

    @Override
    public void update(final String key, final String value) throws KeeperException, InterruptedException {
        String path = getProvider().getRealPath(key);
        try {
            getProvider().update(path, value);
        } catch (KeeperException ex) {
            if (Connection.needRetry(ex)) {
                logger.warn(String.format("AsyncRetryStrategy SessionExpiredException update: %s", path), ex);
                AsyncRetryCenter.INSTANCE.add(new UpdateOperation(getProvider(), path, value));
            } else {
                throw ex;
            }
        }
    }

    @Override
    public void deleteOnlyCurrent(final String key) throws KeeperException, InterruptedException {
        String path = getProvider().getRealPath(key);
        try {
            getProvider().delete(path);
        } catch (KeeperException ex) {
            if (Connection.needRetry(ex)) {
                logger.warn(String.format("AsyncRetryStrategy SessionExpiredException deleteOnlyCurrent: %s", path), ex);
                AsyncRetryCenter.INSTANCE.add(new DeleteCurrentOperation(getProvider(), path));
            } else {
                throw ex;
            }
        }
    }

    @Override
    public void createAllNeedPath(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        try {
            super.createAllNeedPath(key, value, createMode);
        } catch (KeeperException ex) {
            if (Connection.needRetry(ex)) {
                logger.warn(String.format("AllAsyncRetryStrategy SessionExpiredException CreateAllNeedOperation: %s", key), ex);
                AsyncRetryCenter.INSTANCE.add(new CreateAllNeedOperation(getProvider(), key, value, createMode));
            } else {
                throw ex;
            }
        }
    }

    @Override
    public void deleteAllChildren(final String key) throws KeeperException, InterruptedException {
        try {
            super.deleteAllChildren(key);
        } catch (KeeperException ex) {
            if (Connection.needRetry(ex)) {
                logger.warn(String.format("AllAsyncRetryStrategy SessionExpiredException deleteAllChildren: %s", key), ex);
                AsyncRetryCenter.INSTANCE.add(new DeleteAllChildrenOperation(getProvider(), key));
            } else {
                throw ex;
            }
        }
    }

    @Override
    public void deleteCurrentBranch(final String key) throws KeeperException, InterruptedException {
        try {
            super.deleteCurrentBranch(key);
        } catch (KeeperException ex) {
            if (Connection.needRetry(ex)) {
                logger.warn(String.format("AllAsyncRetryStrategy SessionExpiredException deleteCurrentBranch: %s", key), ex);
                AsyncRetryCenter.INSTANCE.add(new DeleteCurrentBranchOperation(getProvider(), key));
            } else {
                throw ex;
            }
        }
    }
}
