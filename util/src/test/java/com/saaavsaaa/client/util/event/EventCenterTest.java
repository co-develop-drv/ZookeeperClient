package com.saaavsaaa.client.util.event;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/*
 * Created by aaa
 */
public class EventCenterTest {
    @Test
    public void testSent() throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(2);
        EventCenter.INSTANCE.registerListener(Type.Test1, new EventListener() {
            @Override
            public boolean ready() {
                return true;
            }
    
            @Override
            public void process(Event event) {
                System.out.println("event test1 : " + event.getEventType().name());
                countDownLatch.countDown();
            }
        });
    
        EventCenter.INSTANCE.registerListener(Type.Test2, new EventListener() {
            @Override
            public boolean ready() {
                return true;
            }
        
            @Override
            public void process(Event event) {
                System.out.println("event test2 : " + event.getEventType().name());
                countDownLatch.countDown();
            }
        });
        
        EventCenter.INSTANCE.sent(new Event(Type.Test1));
        EventCenter.INSTANCE.sent(new Event(Type.Test2));
        countDownLatch.await();
        EventCenter.INSTANCE.close();
    }
}

enum Type {
    Test1,
    Test2
}
