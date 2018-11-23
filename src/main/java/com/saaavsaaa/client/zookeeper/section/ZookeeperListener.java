package com.saaavsaaa.client.zookeeper.section;

import org.apache.zookeeper.WatchedEvent;

/**
 * Created by aaa
 */
public abstract class ZookeeperListener {
    private final String key;

    private String path;

    public ZookeeperListener() {
        this(null);
    }

    public ZookeeperListener(final String path) {
        this.key = path + System.currentTimeMillis();
        this.path = path;
    }

    /**
     * Process.
     *
     * @param event event
     */
    public abstract void process(WatchedEvent event);

    public String getKey() {
        return key;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
