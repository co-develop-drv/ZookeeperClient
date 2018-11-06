package com.saaavsaaa.client.utility;

public class ThreadUtil {

    public static Thread.UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                System.out.println("Thread:" + t.getName() + ",e:" + e.getMessage());
            }
        };
    }
}
