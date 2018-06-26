package com.saaavsaaa.client.event;

/**
 * Created by aaa on 18-6-22.
 */
public class Event {
    private EventType eventType;
    public Event(final EventType eventType){
        this.eventType = eventType;
    }
    
    public EventType getEventType() {
        return eventType;
    }
}
