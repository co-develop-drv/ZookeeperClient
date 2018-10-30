package com.saaavsaaa.client;

import org.apache.curator.test.TestingServer;

import java.io.IOException;

public final class TestServer {

    private static final int PORT = 3333;

    private static volatile TestingServer testingServer;

    public static void start() {
        if (null != testingServer) {
            return;
        }
        try {
            testingServer = new TestingServer(PORT);
//            testingServer.start();
        } catch (final Exception ex) {
            System.out.println("ex:" + ex.getMessage());
        } finally {
            Runtime.getRuntime().addShutdownHook(new Thread() {

                @Override
                public void run() {
                    try {
                        testingServer.close();
                    } catch (final IOException ex) {
                    }
                }
            });
        }
    }
}