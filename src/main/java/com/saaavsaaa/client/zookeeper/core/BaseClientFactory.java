package com.saaavsaaa.client.zookeeper.core;

import com.saaavsaaa.client.action.IClient;
import com.saaavsaaa.client.zookeeper.section.Listener;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.apache.zookeeper.ZooDefs.Ids.OPEN_ACL_UNSAFE;

/*
 * Created by aaa
 */
public abstract class BaseClientFactory {
    protected BaseClient client;
    protected Listener globalListener;
    protected String namespace;
    protected String scheme;
    protected byte[] auth;
    protected List<ACL> authorities;
    protected BaseContext context;
    
    /**
     * start.
     *
     * @return client
     * @throws IOException IO Exception
     * @throws InterruptedException InterruptedException
     */
    public IClient start() throws IOException, InterruptedException {
        prepareClient();
        client.start();
        return client;
    }
    
    /**
     * start until Timeout.
     *
     * @param wait wait
     * @param units units
     * @return connected
     * @throws IOException IO Exception
     * @throws InterruptedException InterruptedException
     * @throws KeeperException OperationTimeoutException
     */
    public IClient start(final int wait, final TimeUnit units) throws IOException, InterruptedException, KeeperException {
        prepareClient();
        if (!client.start(wait, units)) {
            client.close();
            throw new KeeperException.OperationTimeoutException();
        }
        return client;
    }
    
    private void prepareClient() {
        client.setRootNode(namespace);
        if (scheme == null) {
            authorities = ZooDefs.Ids.OPEN_ACL_UNSAFE;
        }
        client.setAuthorities(scheme, auth, authorities);
        if (globalListener != null) {
            client.registerWatch(globalListener);
        }
    }
}
