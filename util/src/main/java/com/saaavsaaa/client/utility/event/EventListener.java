package com.saaavsaaa.client.utility.event;

/**
 * Created by aaa.
 */
public interface EventListener {
    boolean ready();
    void process(Event event);
}
