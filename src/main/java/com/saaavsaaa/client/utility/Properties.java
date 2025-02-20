package com.saaavsaaa.client.utility;

import java.util.ResourceBundle;

/*
 * Created by aaa
 */
public enum Properties {
    INSTANCE;
    
    private final ResourceBundle bundle;
    
    private Properties(){
        bundle = ResourceBundle.getBundle("client");
    }
    
    public String getClientId() {
        // ResourceBundle caches the value in Thread
        String clientId = bundle.getString("client.id");
        if (StringUtil.isNullOrBlank(clientId)) {
            throw new IllegalArgumentException("client.id doesn't exist");
        }
        return clientId;
    }
    
    public boolean watchOn() {
        String result = bundle.getString("client.watch.on");
        if (StringUtil.isNullOrBlank(result)) {
            throw new IllegalArgumentException("client.watch.on doesn't exist");
        }
        return "up".equals(result);
    }
    
    public long getThreadInitialDelay() {
        String result = bundle.getString("client.thread.delay");
        if (StringUtil.isNullOrBlank(result)) {
            throw new IllegalArgumentException("client.thread.delay doesn't exist");
        }
        return Long.valueOf(result);
    }
    
    public long getThreadPeriod() {
        String result = bundle.getString("client.thread.period");
        if (StringUtil.isNullOrBlank(result)) {
            throw new IllegalArgumentException("client.thread.period doesn't exist");
        }
        return Long.valueOf(result);
    }
    
    public int getNodeElectionCount() {
        String result = bundle.getString("node.election.count");
        if (StringUtil.isNullOrBlank(result)) {
            throw new IllegalArgumentException("node.election.count doesn't exist");
        }
        return Integer.valueOf(result);
    }

    public int getEventThreadPoolSize() {
        String result = bundle.getString("event.thread.pool.size");
        if (StringUtil.isNullOrBlank(result)) {
            return 0;
        }
        return Integer.valueOf(result);
    }

    public String getSessionUser() {
        return bundle.getString("login.session.user");
    }

    public String getSessionToken() {
        return bundle.getString("login.token.key");
    }

    public int getEventThreadCacheSize() {
        String result = bundle.getString("event.thread.cache.size");
        if (StringUtil.isNullOrBlank(result)) {
            return 0;
        }
        return Integer.parseInt(result);
    }
}
