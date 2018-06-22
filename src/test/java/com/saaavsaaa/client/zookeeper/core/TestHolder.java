package com.saaavsaaa.client.zookeeper.core;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by aaa
 */
public class TestHolder extends Holder {
    private final CountDownLatch CONNECTING = new CountDownLatch(1);
    
    private volatile AtomicBoolean connected0 = new AtomicBoolean();
    
    TestHolder(final BaseContext context) {
        super(context);
    }
    
    @Override
    protected void start(final int wait, final TimeUnit units) throws IOException, InterruptedException {
        initZookeeper();
        System.out.println("begin start await0:" + this.connected0.get());
        CONNECTING.await(wait, units);
        System.out.println("await:"+ wait);
        System.out.println("start connected0:" + this.connected0.get());
    }
    
    @Override
    protected void processConnection(final WatchedEvent event) {
        if (Watcher.Event.EventType.None == event.getType()) {
            if (Watcher.Event.KeeperState.SyncConnected == event.getState()) {
                try {
                    System.out.println("begin processConnection wait0:" + this.connected0.get() + " ThreadId : " + Thread.currentThread().getId());
                    Thread.sleep(1000);
                    System.out.println("processConnection done. ThreadId : " + Thread.currentThread().getId());
                } catch (Exception e) {
                    System.out.println("wait " + e.getMessage());
                }
                this.connected0.set(true);
                System.out.println("processConnection connected0:" + this.connected0.get());
                CONNECTING.countDown();
                return;
            }
        }
    }
    
    public void setConnected0(boolean is){
        connected0.set(is);
    }
    
    public boolean isConnected0() {
        return connected0.get();
    }
}
