package com.saaavsaaa.client.zookeeper.base;

import com.saaavsaaa.client.action.IClient;
import com.saaavsaaa.client.zookeeper.TestSupport;
import com.saaavsaaa.client.zookeeper.section.ClientContext;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by aaa
 */
public class BaseTest {
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
    
    /*@Test
    public void testNotStart() throws IOException, InterruptedException {
        TestClient testClient = new TestClient(new ClientContext(TestSupport.SERVERS, TestSupport.SESSION_TIMEOUT));
        boolean result = testClient.start(100, TimeUnit.MILLISECONDS);
        assert !result;
        System.out.println(result);
        testClient.close();
        
        testClient = new TestClient(new ClientContext(TestSupport.SERVERS, TestSupport.SESSION_TIMEOUT));
        result = testClient.start(10000, TimeUnit.MILLISECONDS);
        assert result;
        System.out.println(result);
        testClient.close();
    }*/
}
