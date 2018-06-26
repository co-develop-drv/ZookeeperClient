package com.saaavsaaa.client.event;

/**
 * Created by aaa.
 */
public interface EventListener {
    boolean ready();
    void process(Event event);
}
