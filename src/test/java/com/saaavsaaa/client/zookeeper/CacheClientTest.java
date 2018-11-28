package com.saaavsaaa.client.zookeeper;

import com.saaavsaaa.client.action.IClient;
import com.saaavsaaa.client.utility.PathUtil;
import com.saaavsaaa.client.utility.constant.Constants;
import com.saaavsaaa.client.zookeeper.core.BaseClient;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * Created by aaa
 * todo test check cache content
 */
public class CacheClientTest extends UsualClientTest {
    @Override
    protected IClient createClient(ClientFactory creator) throws IOException, InterruptedException {
        return creator.setNamespace(TestSupport.ROOT).authorization(TestSupport.AUTH, TestSupport.AUTH.getBytes(), ZooDefs.Ids.CREATOR_ALL_ACL).newCacheClient(TestSupport.SERVERS, TestSupport.SESSION_TIMEOUT).start();
    }

    @Test
    public void deleteRoot() throws KeeperException, InterruptedException {
        createRootOnly(testClient);
        deleteRoot(testClient);
        checkChangeKey(testClient, PathUtil.checkPath(TestSupport.ROOT));
    }

    @Ignore
    @Test
    public void assertPersist() throws KeeperException, InterruptedException {
        String key = "a";
        String value = "aa";
        String newValue = "aaa";
        if (!isExisted(key, testClient)) {
            testClient.createAllNeedPath(key, value, CreateMode.PERSISTENT);
        } else {
            updateWithCheck(key, value, testClient);
        }

        assertThat(getDirectly(key, testClient), is(value));

        updateWithCheck(key, newValue, testClient);

        String path = PathUtil.getRealPath(TestSupport.ROOT, key);
        assertThat(new String(getZooKeeper(testClient).getData(path, false, null), Constants.UTF_8)
                , is(value));
        sleep(200);

        assertThat(getDirectly(key, testClient), is(newValue));
        testClient.deleteCurrentBranch(key);
    }
}
