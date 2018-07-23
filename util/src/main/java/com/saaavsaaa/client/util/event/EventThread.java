package com.saaavsaaa.client.util.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by aaa
 */
class EventThread extends Thread {
    private final static Logger logger = LoggerFactory.getLogger(EventCenter.class);
    private final Map<Enum, EventListener> listeners = new ConcurrentHashMap<>();
    private final ThreadPoolExecutor eventExecutor;
    private final int corePoolSize = Runtime.getRuntime().availableProcessors();
    private final int maximumPoolSize = corePoolSize;
    private final long keepAliveTime = 0;
    private final int closeDelay = 60;
    private final LinkedBlockingQueue<Event> queue;
    
    EventThread(final LinkedBlockingQueue<Event> queue){
        this.queue = queue;
        eventExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(10), new ThreadFactory() {
            private final AtomicInteger threadIndex = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                thread.setName("event-exec-" + threadIndex.incrementAndGet());
                return thread;
            }
        });
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                eventExecutor.shutdown();
                try {
                    eventExecutor.awaitTermination(closeDelay, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    // ignore
                }
                queue.clear();
            }
        });
    }
    
    @Override
    public void run() {
        for (;;) {
            final Event event;
            try {
                event = queue.take();
                final EventListener listener;
                if (listeners.containsKey(event.getEventType())) {
                    listener = listeners.get(event.getEventType());
                } else {
                    logger.warn("case : there is not a listener that watch the event type : {}", event.getEventType());
                    listener = null;
                }
                if (listener == null || listener.ready()) {
                    eventExecutor.submit(new Runnable() {
                        @Override
                        public void run() {
                            listener.process(event);
                        }
                    });
                }
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
            }
        }
    }
    
    public Map<Enum, EventListener> getListeners() {
        return listeners;
    }
    
    public void registerListener(final Enum eventType, final EventListener listener) {
        listeners.put(eventType, listener);
    }
}
