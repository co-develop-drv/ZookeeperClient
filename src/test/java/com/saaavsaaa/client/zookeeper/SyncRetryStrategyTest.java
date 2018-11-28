package com.saaavsaaa.client.zookeeper;

import com.saaavsaaa.client.retry.TestCallable;
import com.saaavsaaa.client.action.IClient;
import com.saaavsaaa.client.action.IProvider;
import com.saaavsaaa.client.retry.DelayRetryPolicy;
import com.saaavsaaa.client.retry.TestResultCallable;
import com.saaavsaaa.client.utility.PathUtil;
import com.saaavsaaa.client.utility.constant.StrategyType;
import com.saaavsaaa.client.zookeeper.section.ZookeeperListener;
import com.saaavsaaa.client.zookeeper.strategy.UsualStrategy;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by aaa
 */
public class SyncRetryStrategyTest extends UsualClientTest {
    private IProvider provider;

    @Override
    protected IClient createClient(final ClientFactory creator) throws IOException, InterruptedException {
        ZookeeperListener listener = TestSupport.buildListener();
        IClient client = creator.setNamespace(TestSupport.ROOT).authorization(TestSupport.AUTH, TestSupport.AUTH.getBytes(), ZooDefs.Ids.CREATOR_ALL_ACL).newClient(TestSupport.SERVERS, TestSupport.SESSION_TIMEOUT)
                .watch(listener).start();
        client.useExecStrategy(StrategyType.SYNC_RETRY);
        provider = client.getExecStrategy().getProvider();
        return client;
    }

    @Test
    public void createChild() throws KeeperException, InterruptedException {
        final String key = "a/b/bb";
        new UsualStrategy(provider).deleteCurrentBranch(key);
        TestCallable callable = new TestCallable(provider, DelayRetryPolicy.newNoInitDelayPolicy()) {

            @Override
            public void test() throws KeeperException, InterruptedException {
                testClient.useExecStrategy(StrategyType.USUAL);
                testClient.createAllNeedPath(key, "bbb11", CreateMode.PERSISTENT);
                testClient.useExecStrategy(StrategyType.SYNC_RETRY);
            }
        };
        callable.exec();
        assertTrue(testClient.checkExists(key));
        new UsualStrategy(provider).deleteCurrentBranch(key);
        assertFalse(testClient.checkExists(key));
    }

    @Test
    public void deleteBranch() throws KeeperException, InterruptedException {
        final String keyB = "a/b/bb";
        final String value = "bbb11";
        testClient.useExecStrategy(StrategyType.USUAL);
        testClient.createAllNeedPath(keyB, value, CreateMode.PERSISTENT);
        testClient.useExecStrategy(StrategyType.SYNC_RETRY);

        assertTrue(testClient.checkExists(keyB));
        final String keyC = "a/c/cc";
        new UsualStrategy(provider).createAllNeedPath(keyC, "ccc11", CreateMode.PERSISTENT);
        assertTrue(testClient.checkExists(keyC));

        TestCallable callable = getDeleteBranch(keyC);
        callable.exec();
        assertFalse(testClient.checkExists(keyC));
        assertTrue(testClient.checkExists("a"));

        callable = getDeleteBranch(keyB);
        callable.exec();
        assertFalse(testClient.checkExists(PathUtil.checkPath(TestSupport.ROOT)));
        testClient.createAllNeedPath(keyB, "bbb11", CreateMode.PERSISTENT);
        assertTrue(testClient.checkExists(keyB));

        callable.exec();
        assertFalse(testClient.checkExists(PathUtil.checkPath(TestSupport.ROOT)));
    }

    private TestCallable getDeleteBranch(final String key){
        TestCallable callable = new TestCallable(provider, DelayRetryPolicy.newNoInitDelayPolicy()) {
            @Override
            public void test() throws KeeperException, InterruptedException {
                testClient.useExecStrategy(StrategyType.USUAL);
                testClient.deleteCurrentBranch(key);
                testClient.useExecStrategy(StrategyType.SYNC_RETRY);
            }
        };
        return callable;
    }

    @Test
    public void isExisted() throws KeeperException, InterruptedException {
        String key = "a/b/bb";
        testClient.useExecStrategy(StrategyType.USUAL);
        testClient.createAllNeedPath(key, "", CreateMode.PERSISTENT);
        testClient.useExecStrategy(StrategyType.SYNC_RETRY);

        TestResultCallable<Boolean> callable = new TestResultCallable<Boolean>(provider, DelayRetryPolicy.newNoInitDelayPolicy()) {

            @Override
            public void test() throws KeeperException, InterruptedException {
                setResult(provider.exists(provider.getRealPath(key)));
            }
        };
        System.out.println(callable.getResult());
        assertTrue(callable.getResult());

        testClient.useExecStrategy(StrategyType.USUAL);
        testClient.deleteCurrentBranch(key);
        testClient.useExecStrategy(StrategyType.SYNC_RETRY);
    }

    @Test
    public void get() throws KeeperException, InterruptedException {
        final String key = "a/b";
        testClient.useExecStrategy(StrategyType.USUAL);
        testClient.createAllNeedPath(key, "bbb11", CreateMode.PERSISTENT);
        testClient.useExecStrategy(StrategyType.SYNC_RETRY);

        TestResultCallable<String> callable = getData("a");
        assertThat(callable.getResult(), is(""));
        callable = getData(key);
        assertThat(callable.getResult(), is("bbb11"));

        testClient.useExecStrategy(StrategyType.USUAL);
        testClient.deleteCurrentBranch(key);
        testClient.useExecStrategy(StrategyType.SYNC_RETRY);
    }

    private TestResultCallable getData(final String key){
        return new TestResultCallable<String>(provider, DelayRetryPolicy.newNoInitDelayPolicy()) {

            @Override
            public void test() throws KeeperException, InterruptedException {
                setResult(new String(provider.getData(provider.getRealPath(key))));
            }
        };
    }

    @Test
    public void getChildrenKeys() throws KeeperException, InterruptedException {
        final String key = "a/b";
        final String current = "a";

        testClient.useExecStrategy(StrategyType.USUAL);
        testClient.createAllNeedPath(key, "", CreateMode.PERSISTENT);
        testClient.createAllNeedPath("a/c", "", CreateMode.PERSISTENT);
        testClient.useExecStrategy(StrategyType.SYNC_RETRY);

        final TestResultCallable<List<String>> callable = new TestResultCallable<List<String>>(provider, DelayRetryPolicy.newNoInitDelayPolicy()) {

            @Override
            public void test() throws KeeperException, InterruptedException {
                setResult(provider.getChildren(provider.getRealPath(current)));
            }
        };
        final List<String> result = callable.getResult();
        Collections.sort(result, new Comparator<String>() {
            public int compare(final String o1, final String o2) {
                return o2.compareTo(o1);
            }
        });
        assertThat(result.get(0), is("c"));
        assertThat(result.get(1), is("b"));

        testClient.useExecStrategy(StrategyType.USUAL);
        testClient.deleteAllChildren(PathUtil.checkPath(TestSupport.ROOT));
        testClient.useExecStrategy(StrategyType.SYNC_RETRY);
    }

    @Test
    public void update() throws KeeperException, InterruptedException {
        final String key = "a";
        final String value = "aa";
        final String newValue = "aaa";
        testClient.deleteCurrentBranch(key);
        testClient.createAllNeedPath(key, value, CreateMode.PERSISTENT);
        String data = testClient.getDataString(key);
        System.out.println(data);
        assertThat(data, is(value));

        final TestCallable callable = new TestCallable(provider, DelayRetryPolicy.newNoInitDelayPolicy()) {

            @Override
            public void test() throws KeeperException, InterruptedException {
                provider.update(provider.getRealPath(key), newValue);
            }
        };
        callable.exec();
        assertThat(testClient.getDataString(key), is(newValue));
        testClient.deleteCurrentBranch(key);
    }

    @Test
    public void delAllChildren() throws KeeperException, InterruptedException {
        String key = "a/b/bb";
        testClient.createAllNeedPath(key, "bb", CreateMode.PERSISTENT);
        key = "a/c/cc";
        testClient.createAllNeedPath(key, "cc", CreateMode.PERSISTENT);
        assertNotNull(getZooKeeper(testClient).exists(PathUtil.getRealPath(TestSupport.ROOT, key), false));

        TestCallable callable = new TestCallable(provider, DelayRetryPolicy.newNoInitDelayPolicy()) {
            @Override
            public void test() throws KeeperException, InterruptedException {
                new UsualStrategy(provider).deleteAllChildren("a");
            }
        };
        callable.exec();
        assertNull(getZooKeeper(testClient).exists(PathUtil.getRealPath(TestSupport.ROOT, key), false));
        assertNotNull(getZooKeeper(testClient).exists(PathUtil.checkPath(TestSupport.ROOT), false));
        super.deleteRoot(testClient);
    }
}