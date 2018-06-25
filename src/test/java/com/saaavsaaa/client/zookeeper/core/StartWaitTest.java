package com.saaavsaaa.client.zookeeper.core;

import com.saaavsaaa.client.action.IClient;
import com.saaavsaaa.client.zookeeper.TestSupport;
import com.saaavsaaa.client.zookeeper.section.ClientContext;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by aaa
 */
public class StartWaitTest {
    @Test
    public void testStart() throws IOException, InterruptedException {
        IClient testClient = new TestClient(new ClientContext(TestSupport.SERVERS, TestSupport.SESSION_TIMEOUT));
        boolean result = testClient.start(10000, TimeUnit.MILLISECONDS);
        assert result;
        System.out.println(result);
        testClient.close();
    }
    
    @Test
    public void testSortStart() throws IOException, InterruptedException {
        TestClient testClient = new TestClient(new ClientContext(TestSupport.SERVERS, TestSupport.SESSION_TIMEOUT));
        boolean result = testClient.start(100, TimeUnit.MILLISECONDS);
        assert !result;
        System.out.println(result);
        testClient.close();
    }
    
    @Test
    public void testNotStart() throws IOException, InterruptedException {
        TestClient c = new TestClient(new ClientContext(TestSupport.SERVERS, TestSupport.SESSION_TIMEOUT));
        boolean r = c.start(100, TimeUnit.MILLISECONDS);
        System.out.println("result : " + r);
        assert !r;
        c.close();
        assert !c.getZookeeper().getState().isConnected();
        
        System.out.println("===============================================================================");
    
        TestClient c1 = new TestClient(new ClientContext(TestSupport.SERVERS, TestSupport.SESSION_TIMEOUT));
        boolean r1 = c1.start(10000, TimeUnit.MILLISECONDS);
        System.out.println("result : " + r1);
        assert r1;
        c1.close();
    }
}
