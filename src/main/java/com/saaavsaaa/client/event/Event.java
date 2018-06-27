package com.saaavsaaa.client.event;

/*
 * Created by aaa
 */
public class Event {
    private Enum eventType;
    
    public Event(final Enum eventType) {
        this.eventType = eventType;
    }
    
    public Enum getEventType() {
        return eventType;
    }
}
