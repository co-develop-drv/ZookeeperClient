package com.saaavsaaa.client.zookeeper.core;

import com.saaavsaaa.client.utility.Properties;
import com.saaavsaaa.client.utility.StringUtil;
import com.saaavsaaa.client.utility.constant.Constants;
import com.saaavsaaa.client.zookeeper.section.Listener;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/*
 * zookeeper connection holder
 *
 * Created by aaa
 */
public class Holder {
    private static final Logger logger = LoggerFactory.getLogger(Holder.class);
    private static final CountDownLatch CONNECTING = new CountDownLatch(1);
    
    protected ZooKeeper zooKeeper;
    protected final BaseContext context;
    private volatile AtomicBoolean connected = new AtomicBoolean();// false
    
    Holder(final BaseContext context){
        this.context = context;
    }
    
    protected void start() throws IOException, InterruptedException {
        initZookeeper();
        CONNECTING.await();
    }
    
    protected void start(final int wait, final TimeUnit units) throws IOException, InterruptedException {
        initZookeeper();
        CONNECTING.await(wait, units);
    }
    
    protected void initZookeeper() throws IOException {
        logger.debug("Holder servers:{},sessionTimeOut:{}", context.servers, context.sessionTimeOut);
        zooKeeper = new ZooKeeper(context.servers, context.sessionTimeOut, startWatcher());
        if (!StringUtil.isNullOrBlank(context.scheme)) {
            zooKeeper.addAuthInfo(context.scheme, context.auth);
            logger.debug("Holder scheme:{},auth:{}", context.scheme, context.auth);
        }
    }
    
    private Watcher startWatcher() {
        return new Watcher() {
            public void process(final WatchedEvent event) {
                processConnection(event);
                if (context.globalListener != null) {
                    context.globalListener.process(event);
                    logger.debug("BaseClient " + Constants.GLOBAL_LISTENER_KEY + " process");
                }
                if (Properties.INSTANCE.watchOn()) {
                    for (Listener listener : context.getWatchers().values()) {
                        if (listener.getPath() == null || listener.getPath().equals(event.getPath())) {
                            logger.debug("listener process:{}, listener:{}", listener.getPath(), listener.getKey());
                            listener.process(event);
                        }
                    }
                }
            }
        };
    }
    
    protected void processConnection(final WatchedEvent event) {
        logger.debug("BaseClient process event:{}", event.toString());
        if (Watcher.Event.EventType.None == event.getType()) {
            if (Watcher.Event.KeeperState.SyncConnected == event.getState()) {
                CONNECTING.countDown();
                connected.set(true);
                logger.debug("BaseClient startWatcher SyncConnected");
                return;
            } else if (Watcher.Event.KeeperState.Expired == event.getState()) {
                connected.set(false);
                try {
                    logger.warn("startWatcher Event.KeeperState.Expired");
                    reset();
                    // CHECKSTYLE:OFF
                } catch (Exception e) {
                    // CHECKSTYLE:ON
                    logger.error("event state Expired:{}", e.getMessage(), e);
                }
            }
        }
    }
    
    public void reset() throws IOException, InterruptedException {
        logger.debug("zk reset....................................");
        close();
        start();
        logger.debug("....................................zk reset");
    }
    
    public void close() {
        try {
            zooKeeper.close();
            connected.set(false);
            logger.debug("zk closed");
            this.context.close();
        } catch (Exception ee) {
            logger.warn("Holder close:{}", ee.getMessage());
        }
    }
    
    public ZooKeeper getZooKeeper() {
        return zooKeeper;
    }
    
    public boolean isConnected() {
        return connected.get();
    }
}
