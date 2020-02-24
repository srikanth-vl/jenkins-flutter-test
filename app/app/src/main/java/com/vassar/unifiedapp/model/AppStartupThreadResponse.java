package com.vassar.unifiedapp.model;

public class AppStartupThreadResponse {

    private boolean isCompleted;
    private boolean isLoggedIn;

    public AppStartupThreadResponse(boolean isCompleted, boolean isLoggedIn) {
        this.isCompleted = isCompleted;
        this.isLoggedIn = isLoggedIn;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }
}
