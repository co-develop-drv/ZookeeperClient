package com.saaavsaaa.client.election;

import com.saaavsaaa.client.action.IProvider;
import com.saaavsaaa.client.utility.PathUtil;
import com.saaavsaaa.client.utility.Properties;
import com.saaavsaaa.client.utility.constant.Constants;
import com.saaavsaaa.client.zookeeper.section.WatcherCreator;
import com.saaavsaaa.client.zookeeper.section.ZookeeperListener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Created by aaa
 * It is not recommended to be used as a global variable
 */
public abstract class LeaderElection {
    private static final Logger logger = LoggerFactory.getLogger(LeaderElection.class);
    private int retryCount = Properties.INSTANCE.getNodeElectionCount();

    private boolean done;

    /**
     * Listener will be register when the contention of the path is unsuccessful.
     *
     * @param nodeBeContend node be contend
     * @param provider provider
     * @throws KeeperException zookeeper exception
     * @throws InterruptedException interrupted exception
     */
    public void executeContention(final String nodeBeContend, final IProvider provider) throws KeeperException, InterruptedException {
        boolean canBegin;
        final String realNode = provider.getRealPath(nodeBeContend);
        String contendNode = PathUtil.getRealPath(realNode, Constants.CHANGING_KEY);
        canBegin = contend(contendNode, provider, new ZookeeperListener(contendNode) {

            @Override
            public void process(final WatchedEvent event) {
                try {
                    retryCount--;
                    if (retryCount < 0) {
                        return;
                    }
                    executeContention(realNode, provider);
                } catch (final KeeperException | InterruptedException ex) {
                    logger.error("Listener Exception executeContention:{}", ex.getMessage(), ex);
                }
            }
        });
        if (canBegin) {
            try {
                action();
                done = true;
                callback();
            } catch (final KeeperException | InterruptedException ex) {
                logger.error("action Exception executeContention:{}", ex.getMessage(), ex);
            }
            provider.delete(contendNode);
        }
    }

    private boolean contend(final String node, final IProvider provider, final ZookeeperListener eventListener) throws KeeperException, InterruptedException {
        boolean result = false;
        try {
            // TODO EPHEMERAL_SEQUENTIAL check index value
            provider.create(node, Properties.INSTANCE.getClientId(), CreateMode.EPHEMERAL);
            result = true;
        } catch (final KeeperException.NodeExistsException ex) {
            logger.info("contend not result");
            // TODO or changing_key node value == current client id
            provider.exists(node, WatcherCreator.deleteWatcher(eventListener));
        }
        return result;
    }
    
    /**
     * wait done.
     */
    public void waitDone() {
        while (!done) {
            try {
                Thread.sleep(10L);
            } catch (InterruptedException e) {
                logger.error("waitDone:{}", e.getMessage(), e);
            }
        }
    }
    
//    public abstract void actionWhenUnreached() throws KeeperException, InterruptedException;
    
    /**
     * contend exec.
     *
     * @throws KeeperException Zookeeper Exception
     * @throws InterruptedException InterruptedException
     */
    public abstract void action() throws KeeperException, InterruptedException;
    
    /**
     * callback.
     */
    public void callback() {
        
    }
}
