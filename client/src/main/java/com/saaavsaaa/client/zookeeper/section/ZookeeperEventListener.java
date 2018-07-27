package com.saaavsaaa.client.zookeeper.section;

import org.apache.zookeeper.WatchedEvent;

/**
 * Created by aaa
 */
public abstract class ZookeeperEventListener {
    private final String key;
    private String path;
    
    public ZookeeperEventListener(){
        this(null);
    }
    public ZookeeperEventListener(final String path){
        this.path = path;
        this.key = path + System.currentTimeMillis();
    }
    
    public abstract void process(final WatchedEvent event);
    
    public String getPath() {
        return path;
    }
    
    public void setPath(final String path){
        this.path = path;
    }
    
    public String getKey() {
        return key;
    }
}
