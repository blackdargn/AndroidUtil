package com.android.util.manager;


public class RaskManager extends TaskManager {

    private static RaskManager instance;
    private RaskManager() {
        super();
    }
    public static RaskManager getInstance() {
        if(instance == null) {
            instance = new RaskManager();
        }
        return instance;
    }
}