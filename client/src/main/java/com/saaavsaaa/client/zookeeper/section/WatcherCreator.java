package com.saaavsaaa.client.zookeeper.section;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by aaa
 */
public class WatcherCreator {
    private static final Logger logger = LoggerFactory.getLogger(WatcherCreator.class);
    public static Watcher deleteWatcher(ZookeeperEventListener listener){
        return new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if (listener.getPath().equals(event.getPath()) && Watcher.Event.EventType.NodeDeleted.equals(event.getType())){
                    listener.process(new WatchedDataEvent(event));
                    logger.debug("delete node event:{}", event.toString());
                }
            }
        };
    }
}
