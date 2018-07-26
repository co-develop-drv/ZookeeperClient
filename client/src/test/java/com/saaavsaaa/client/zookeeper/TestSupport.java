package com.saaavsaaa.client.zookeeper;

import com.saaavsaaa.client.zookeeper.section.WatchedDataEvent;
import com.saaavsaaa.client.zookeeper.section.ZookeeperEventListener;
import org.apache.zookeeper.WatchedEvent;

/**
 * Created by aaa
 */
public class TestSupport {
    public static final String AUTH = "digest";
    public static final String SERVERS = "192.168.2.44:2181";
    public static final int SESSION_TIMEOUT = 200000;//ms
    public static final String ROOT = "test";
    
    public static ZookeeperEventListener buildListener(){
        ZookeeperEventListener listener = new ZookeeperEventListener(null) {
            @Override
            public void process(WatchedDataEvent event) {
                System.out.println("==========================================================");
                System.out.println(event.toString());
                System.out.println("==========================================================");
            }
        };
        return listener;
    }
}
