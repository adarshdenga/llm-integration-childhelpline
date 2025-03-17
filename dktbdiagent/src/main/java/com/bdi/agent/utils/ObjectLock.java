package com.bdi.agent.utils;

public class ObjectLock {

    private final Object lock = new Object();

    public Object getLock() {
        return lock;
    }
}
