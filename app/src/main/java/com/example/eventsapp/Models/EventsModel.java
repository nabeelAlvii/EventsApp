package com.example.eventsapp.Models;

import java.io.Serializable;

public class EventsModel implements Serializable {
    private String eventId;
    private String title, location, date, time, description, creator, imageUri;

    public EventsModel() {};
    public EventsModel(String title, String location, String date, String time, String description, String creator, String imageUri) {
        this.title = title;
        this.location = location;
        this.date = date;
        this.time = time;
        this.description = description;
        this.creator = creator;
        this.imageUri = imageUri;
    }

    // Getters and setters for all fields
    public String getEventId() {
        return eventId;
    }
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }
}
