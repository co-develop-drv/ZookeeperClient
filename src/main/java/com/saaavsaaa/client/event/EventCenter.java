package com.saaavsaaa.client.event;

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
public enum EventCenter {
    INSTANCE;
    
    private final LinkedBlockingQueue<Event> queue = new LinkedBlockingQueue<>();
    private final EventThread eventThread = new EventThread(queue);
    private boolean started;
    
    public synchronized void sent(final Event event) {
        if (event == null) {
            throw new IllegalArgumentException("event should not be null");
        }
        if (!started) {
            start();
            started = true;
        }
        queue.add(event);
    }
    
    public boolean containsEvent(final Event event) {
        if (queue.isEmpty()) {
            return false;
        }
        for (Event one : queue) {
            if (one.getEventType().equals(event.getEventType())) {
                return true;
            }
        }
        return false;
    }
    
    public synchronized void start() {
        if (started) {
            return;
        }
        eventThread.setName("event-thread");
        eventThread.start();
        this.started = true;
    }
    
    public void registerListener(final EventType eventType, final EventListener listener) {
        eventThread.registerListener(eventType, listener);
    }
}

class EventThread extends Thread {
    private final static Logger logger = LoggerFactory.getLogger(EventCenter.class);
    private final Map<EventType, EventListener> listeners = new ConcurrentHashMap<>();
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
    
    public void registerListener(final EventType eventType, final EventListener listener) {
        listeners.put(eventType, listener);
    }
}
