package com.saaavsaaa.client.zookeeper.section;

import com.saaavsaaa.client.utility.StringUtil;
import com.saaavsaaa.client.utility.constant.ZookeeperConstants;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WatchedDataEvent extends WatchedEvent {
    
    private static final Logger logger = LoggerFactory.getLogger(WatchedDataEvent.class);
    
    private final String data;
    
    public WatchedDataEvent(final WatchedEvent event) {
        this(event, null);
    }
    
    public WatchedDataEvent(final WatchedEvent event, final ZooKeeper zooKeeper) {
        super(event.getType(), event.getState(), event.getPath());
        data = initData(event, zooKeeper);
    }
    
    private String initData(final WatchedEvent event, final ZooKeeper zooKeeper) {
        if (zooKeeper == null) {
            return null;
        }
        if (!zooKeeper.getState().isConnected() && !zooKeeper.getState().isAlive()) {
            return null;
        }
        if (StringUtil.isNullOrBlank(event.getPath())) {
            return null;
        }

        byte[] result;
        try {
            if (zooKeeper.exists(event.getPath(), true) != null) {
                result = zooKeeper.getData(event.getPath(), true, null);
                return new String(result, ZookeeperConstants.UTF_8);
            }
        } catch (final KeeperException | InterruptedException ex) {
            logger.warn("init data:{}", ex.getMessage());
        }
        return null;
    }
    
    public String getData() {
        return data;
    }
    
    @Override
    public String toString() {
        return super.toString() + " data : " + data;
    }
}
