package com.saaavsaaa.client.zookeeper.core;

import com.saaavsaaa.client.zookeeper.section.ZookeeperEventListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Created by aaa
 */
public abstract class BaseContext {
    protected String servers;
    protected int sessionTimeOut;
    protected String scheme;
    protected byte[] auth;
    protected ZookeeperEventListener globalListener;
    protected final Map<String, ZookeeperEventListener> watchers = new ConcurrentHashMap<>();
    
    /**
     * close.
     */
    public void close() {
        this.watchers.clear();
    }
    
    public String getServers() {
        return servers;
    }
    
    public int getSessionTimeOut() {
        return sessionTimeOut;
    }
    
    public String getScheme() {
        return scheme;
    }
    
    public byte[] getAuth() {
        return auth;
    }
    
    public Map<String, ZookeeperEventListener> getWatchers(){
        return watchers;
    }
}
