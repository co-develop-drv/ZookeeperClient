package com.saaavsaaa.client.util.event;

/**
 * Created by aaa.
 */
public interface EventListener {
    boolean ready();
    void process(Event event);
}
