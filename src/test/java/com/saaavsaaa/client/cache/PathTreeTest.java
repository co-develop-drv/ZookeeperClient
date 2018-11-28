package com.saaavsaaa.client.cache;

import com.saaavsaaa.client.TestServer;
import com.saaavsaaa.client.action.IClient;
import com.saaavsaaa.client.utility.constant.Constants;
import com.saaavsaaa.client.zookeeper.ClientFactory;
import com.saaavsaaa.client.zookeeper.TestSupport;
import com.saaavsaaa.client.zookeeper.core.BaseTest;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class PathTreeTest extends BaseTest {
    
    private PathTree pathTree;
    
    private IClient testClient;
    
    @Before
    public void start() throws IOException, InterruptedException {
        TestServer.start();
        ClientFactory creator = new ClientFactory();
        testClient = creator.setNamespace(TestSupport.ROOT).authorization(TestSupport.AUTH, TestSupport.AUTH.getBytes(), ZooDefs.Ids.CREATOR_ALL_ACL)
                .newClient(TestSupport.SERVERS, TestSupport.SESSION_TIMEOUT).start();
    
        pathTree = new PathTree(TestSupport.ROOT, testClient);
    }
    
    @After
    public void stop() {
        pathTree.close();
        testClient.close();
    }
    
    @Test
    public void assertLoad() throws KeeperException, InterruptedException {
        final String keyB = "a/b/bb";
        final String valueB = "bbb11";
        testClient.createAllNeedPath(keyB, valueB, CreateMode.PERSISTENT);
        assertTrue(testClient.checkExists(keyB));
        final String keyC = "a/c/cc";
        final String valueC = "ccc11";
        testClient.createAllNeedPath(keyC, valueC, CreateMode.PERSISTENT);
        assertTrue(testClient.checkExists(keyC));
        
        try {
            pathTree.load();
    
            assertThat(new String(pathTree.getValue(keyB)), is(valueB));
            assertThat(new String(pathTree.getValue(keyC)), is(valueC));
        } finally {
            testClient.deleteCurrentBranch(keyC);
            testClient.deleteCurrentBranch(keyB);
        }
    }
    
    @Test
    public void assertGetChildren() throws KeeperException, InterruptedException {
        final String keyB = "a/b";
        final String valueB = "bbb11";

        final String keyC = "a/c";
        final String valueC = "ccc11";
        
        try {
            pathTree.watch();
            testClient.createAllNeedPath(keyB, valueB, CreateMode.PERSISTENT);
            Thread.sleep(200);
            testClient.createAllNeedPath(keyC, valueC, CreateMode.PERSISTENT);
            Thread.sleep(200);
    
            assertThat(pathTree.getChildren("a"), hasItems(valueB, valueC));
        } finally {
            testClient.deleteCurrentBranch(keyC);
            testClient.deleteCurrentBranch(keyB);
        }
    }
    
    @Test
    public void assertPut() {
        final String key = "a/b/bb";
        final String value = "bbb11";
        pathTree.put(key, value);
        assertThat(pathTree.getValue("a"), CoreMatchers.is(Constants.NOTHING_DATA));
        assertThat(pathTree.getValue("a/b"), is(Constants.NOTHING_DATA));
        assertThat(pathTree.getValue(key), is(value.getBytes(Constants.UTF_8)));
    }
    
    @Test
    public void assertGetValue() throws KeeperException, InterruptedException {
        final String key = "a/b/bb";
        final String value = "bbb11";
        try {
            pathTree.watch();
            testClient.createAllNeedPath(key, value, CreateMode.PERSISTENT);
            Thread.sleep(200);
            assertThat(pathTree.getValue(key), is(value.getBytes(Constants.UTF_8)));
        } finally {
            testClient.deleteCurrentBranch(key);
        }
    }
    
    @Test
    public void assertDelete() throws KeeperException, InterruptedException {
        final String key = "a/b/bb";
        final String value = "bbb11";
        try {
            pathTree.watch();
            testClient.createAllNeedPath(key, value, CreateMode.PERSISTENT);
            Thread.sleep(200);
            pathTree.delete(key);
            assertNull(pathTree.getValue(key));
        } finally {
            testClient.deleteCurrentBranch(key);
        }
    }

    @Test
    public void assertWatch() throws KeeperException, InterruptedException {
        final String key = "a/b/bb";
        final String value = "bbb11";
        final String valueNew = "111";
        try {
            createRootOnly(testClient);
            pathTree.watch();

            testClient.createAllNeedPath(key, value, CreateMode.PERSISTENT);
            Thread.sleep(200);
            testClient.update(key, valueNew);
            
            Thread.sleep(1000);
            assertThat(pathTree.getValue(key), is(valueNew.getBytes(Constants.UTF_8)));
        } finally {
            testClient.deleteCurrentBranch(key);
        }
    }
    
    @Test
    public void assertRefreshPeriodic() throws KeeperException, InterruptedException {
        final String key = "a/b/bb";
        final String value = "bbb11";
        final String valueNew = "111";
        try {
            testClient.createAllNeedPath(key, value, CreateMode.PERSISTENT);
            pathTree.refreshPeriodic(100);
            sleep(2000);
            assertThat(pathTree.getValue(key), is(value.getBytes(Constants.UTF_8)));
            testClient.update(key, valueNew);
            sleep(2000);
            assertThat(pathTree.getValue(key), is(valueNew.getBytes(Constants.UTF_8)));
            
            pathTree.refreshPeriodic(10);
        } catch (final IllegalArgumentException ex) {
            assertThat(ex.getMessage(), is("period already set"));
        } finally {
            pathTree.stopRefresh();
            testClient.deleteCurrentBranch(key);
        }
    }
    
    @Test
    public void assertStopRefresh() {
        try {
            pathTree.refreshPeriodic(1);
            sleep(100);
            pathTree.refreshPeriodic(1);
        } catch (final IllegalArgumentException ex) {
            assertThat(ex.getMessage(), is("period already set"));
            pathTree.stopRefresh();
            pathTree.refreshPeriodic(1);
        }
    }
}
