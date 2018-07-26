package com.saaavsaaa.client.utility.event;

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
