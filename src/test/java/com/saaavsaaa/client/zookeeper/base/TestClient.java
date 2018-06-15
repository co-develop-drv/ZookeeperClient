package com.saaavsaaa.client.zookeeper.base;

import com.saaavsaaa.client.zookeeper.UsualClient;

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
//        ((TestHolder)holder).setConnected0(true);
//        System.out.println("setConnected0:" + ((TestHolder)holder).isConnected0());
        holder.start(wait, units);
        return ((TestHolder)holder).isConnected0();
    }
}
