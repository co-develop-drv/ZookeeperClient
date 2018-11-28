package com.saaavsaaa.client.zookeeper;

import com.saaavsaaa.client.action.IClient;
import com.saaavsaaa.client.utility.constant.Constants;
import com.saaavsaaa.client.utility.constant.StrategyType;
import com.saaavsaaa.client.zookeeper.core.BaseClientTest;
import com.saaavsaaa.client.zookeeper.strategy.*;
import com.saaavsaaa.client.zookeeper.transaction.BaseTransaction;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by aaa
 */
public class UsualClientTest extends BaseClientTest {
    
    @Override
    protected IClient createClient(final ClientFactory creator) throws IOException, InterruptedException {
        return creator.setNamespace(TestSupport.ROOT).authorization(TestSupport.AUTH, TestSupport.AUTH.getBytes(), ZooDefs.Ids.CREATOR_ALL_ACL).newClient(TestSupport.SERVERS, TestSupport.SESSION_TIMEOUT).start();
    }

    @Test
    public void assertUseExecStrategy() {
        testClient.useExecStrategy(StrategyType.CONTEND);
        assertThat(testClient.getExecStrategy().getClass().getName(), is(ContentionStrategy.class.getName()));
        testClient.useExecStrategy(StrategyType.TRANSACTION_CONTEND);
        assertThat(testClient.getExecStrategy().getClass().getName(), is(TransactionContendStrategy.class.getName()));
        testClient.useExecStrategy(StrategyType.SYNC_RETRY);
        assertThat(testClient.getExecStrategy().getClass().getName(), is(SyncRetryStrategy.class.getName()));
        testClient.useExecStrategy(StrategyType.ASYNC_RETRY);
        assertThat(testClient.getExecStrategy().getClass().getName(), is(AsyncRetryStrategy.class.getName()));
        testClient.useExecStrategy(StrategyType.USUAL);
        assertThat(testClient.getExecStrategy().getClass().getName(), is(UsualStrategy.class.getName()));
    }

    @Test
    public void assertGetData() throws KeeperException, InterruptedException {
        String key = "a/b/bb";
        String value = "bbb11";
        testClient.createAllNeedPath(key, value, CreateMode.PERSISTENT);
        assertThat(testClient.getDataString(key), is(value));
        testClient.deleteCurrentBranch(key);
    }

    @Test
    public void assertCreateRoot() throws KeeperException, InterruptedException {
        super.createRoot(testClient);
    }

    @Test
    public void assertCreateChild() throws KeeperException, InterruptedException {
        super.createChild(testClient);
    }

    @Test
    public void assertDeleteBranch() throws KeeperException, InterruptedException {
        super.deleteBranch(testClient);
    }

    @Test
    public void assertExisted() throws KeeperException, InterruptedException {
        super.isExisted(testClient);
    }

    @Test
    public void assertGet() throws KeeperException, InterruptedException {
        super.get(testClient);
    }

    @Test
    public void assertAsyncGet() throws KeeperException, InterruptedException {
        super.asyncGet(testClient);
    }

    @Test
    public void assertGetChildrenKeys() throws KeeperException, InterruptedException {
        super.getChildrenKeys(testClient);
    }

    @Test
    public void assertPersist() throws KeeperException, InterruptedException {
        super.persist(testClient);
    }

    @Test
    public void assertPersistEphemeral() throws KeeperException, InterruptedException {
        super.persistEphemeral(testClient);
    }

    @Test
    public void assertDelAllChildren() throws KeeperException, InterruptedException {
        super.delAllChildren(testClient);
    }

    @Test
    public void assertWatch() throws KeeperException, InterruptedException {
        super.watch(testClient);
    }

    @Test
    public void assertWatchRegister() throws KeeperException, InterruptedException {
        super.watchRegister(testClient);
    }

    @Test
    public void assertClose() {
        super.close(testClient);
    }

    @Test
    public void assertDeleteOnlyCurrent() throws KeeperException, InterruptedException {
        String key = "key";
        String value = "value";
        testClient.createCurrentOnly(key, value, CreateMode.PERSISTENT);
        assertThat(testClient.getDataString(key), is(value));
        assertTrue(testClient.checkExists(key));
        testClient.deleteOnlyCurrent(key);
        assertFalse(testClient.checkExists(key));
        deleteRoot(testClient);
    }

    @Test
    public void assertTransaction() throws KeeperException, InterruptedException {
        String key = "key";
        String value = "value";
        BaseTransaction transaction = testClient.transaction();
        testClient.createCurrentOnly(key, value, CreateMode.PERSISTENT);
        transaction.setData(key, value.getBytes(Constants.UTF_8));
        transaction.commit();
        assertThat(testClient.getDataString(key), is(value));
        testClient.deleteOnlyCurrent(key);
        deleteRoot(testClient);
    }
}