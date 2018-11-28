package com.saaavsaaa.client.zookeeper;

import com.saaavsaaa.client.action.IClient;
import com.saaavsaaa.client.zookeeper.section.ZookeeperListener;
import org.apache.zookeeper.ZooDefs;

import java.io.IOException;

/**
 * Created by aaa
 */
public class UsualWatchClientTest extends UsualClientTest {
    
    @Override
    protected IClient createClient(final ClientFactory creator) throws IOException, InterruptedException {
        ZookeeperListener listener = TestSupport.buildListener();
        return creator.setNamespace(TestSupport.ROOT).authorization(TestSupport.AUTH, TestSupport.AUTH.getBytes(), ZooDefs.Ids.CREATOR_ALL_ACL).newClient(TestSupport.SERVERS, TestSupport.SESSION_TIMEOUT).watch(listener).start();
    }
}