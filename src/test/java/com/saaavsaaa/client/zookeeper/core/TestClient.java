package com.saaavsaaa.client.zookeeper.core;

import com.saaavsaaa.client.zookeeper.UsualClient;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by aaa
 */
public class TestClient extends UsualClient {
    TestClient(BaseContext context) {
        super(context);
    }
    
    @Override
    public synchronized boolean start(final int wait, final TimeUnit units) throws InterruptedException, IOException {
        holder = new TestHolder(getContext());
        holder.start(wait, units);
        return ((TestHolder)holder).isConnected0();
    }
    
    public ZooKeeper getZookeeper() {
        return holder.getZooKeeper();
    }
}
