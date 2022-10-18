package com.jenkins.analyse4me.model;

/*
@Author Ernest Jenkins

*/
public class EventItem extends listItem {

    private messageModel event;

    // here getters and setters
    // for title and so on, built
    // using event

    @Override
    public int getType() {
        return TYPE_EVENT;
    }

    public messageModel getEvent() {
        return event;
    }

    public void setEvent(messageModel event) {
        this.event = event;
    }
}