package com.talk.walk.Models;

public class Notifications {
    private String notification_id;
    private String user_id;
    private String met_user_id;
    private long timestamp;
    private String type;
    private boolean isUserRead = false;
    private boolean isMetUserRead = false;

    public Notifications() {
    }

    public Notifications(String notification_id, String user_id, String met_user_id, long timestamp, String type, boolean isUserRead, boolean isMetUserRead) {
        this.notification_id = notification_id;
        this.met_user_id = met_user_id;
        this.user_id = user_id;
        this.timestamp = timestamp;
        this.type = type;
        this.isUserRead = isUserRead;
        this.isMetUserRead = isMetUserRead;
    }

    public boolean isUserRead() {
        return isUserRead;
    }

    public void setUserRead(boolean userRead) {
        isUserRead = userRead;
    }

    public boolean isMetUserRed() {
        return isMetUserRead;
    }

    public void setMetUserRed(boolean metUserRead) {
        isMetUserRead = metUserRead;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getNotification_id() {
        return notification_id;
    }

    public void setNotification_id(String notification_id) {
        this.notification_id = notification_id;
    }

    public String getMet_user_id() {
        return met_user_id;
    }

    public void setMet_user_id(String met_user_id) {
        this.met_user_id = met_user_id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
