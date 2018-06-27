package com.saaavsaaa.client.event;

import java.util.concurrent.LinkedBlockingQueue;

/*
 * Created by aaa
 */
public enum EventCenter {
    INSTANCE;
    
    private final LinkedBlockingQueue<Event> queue = new LinkedBlockingQueue<>();
    private final EventThread eventThread = new EventThread(queue);
    private boolean started;
    
    public synchronized void sent(final Event event) throws InterruptedException {
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
    
    public synchronized void start() throws InterruptedException {
        if (started) {
            return;
        }
        eventThread.setName("event-thread");
        eventThread.start();
        this.started = true;
    }
    
    public synchronized void close() {
        if (started) {
            started = false;
            queue.clear();
        }
    }
    
    public void updateListener(final Enum eventType, final EventListener listener) {
        eventThread.registerListener(eventType, listener);
    }
    
    public void registerListener(final Enum eventType, final EventListener listener) {
        if (eventThread.getListeners().containsKey(eventType)) {
            return;
        }
        eventThread.registerListener(eventType, listener);
    }
}

