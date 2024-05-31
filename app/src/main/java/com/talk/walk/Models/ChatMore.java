package com.talk.walk.Models;

import android.graphics.drawable.Drawable;

public class ChatMore {

    private Drawable drawable;
    private String name;
    private boolean isEnabled;

    public ChatMore(Drawable drawable, String name, boolean isEnabled) {
        this.drawable = drawable;
        this.name = name;
        this.isEnabled = isEnabled;
    }

    public boolean getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
