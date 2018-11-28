package com.saaavsaaa.client.zookeeper.core;

import com.saaavsaaa.client.action.IClient;
import org.apache.zookeeper.KeeperException;

public class BaseTest {
    
    protected final void createRootOnly(final IClient client) throws KeeperException, InterruptedException {
        ((BaseClient) client).createNamespace();
    }
    
    protected final void deleteRoot(final IClient client) throws KeeperException, InterruptedException {
        ((BaseClient) client).deleteNamespace();
    }
    
    protected final void sleep(final long tick) {
        try {
            Thread.sleep(tick);
        } catch (final InterruptedException ignore) {
        }
    }
}
