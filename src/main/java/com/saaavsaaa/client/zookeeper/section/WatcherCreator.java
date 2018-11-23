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

    /**
     * Get string type data.
     *
     * @param eventListener listener
     * @return watcher
     */
    public static Watcher deleteWatcher(final ZookeeperListener eventListener) {
        return new Watcher() {

            @Override
            public void process(final WatchedEvent event) {
                if (eventListener.getPath().equals(event.getPath()) && Event.EventType.NodeDeleted.equals(event.getType())) {
                    eventListener.process(event);
                }
            }
        };
    }
}
