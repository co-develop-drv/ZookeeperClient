package com.saaavsaaa.client.election;

import com.saaavsaaa.client.utility.Properties;
import com.saaavsaaa.client.utility.constant.ZookeeperConstants;
import com.saaavsaaa.client.zookeeper.section.WatchedDataEvent;
import com.saaavsaaa.client.zookeeper.section.WatcherCreator;
import com.saaavsaaa.client.action.IProvider;
import com.saaavsaaa.client.zookeeper.section.ZookeeperEventListener;
import com.saaavsaaa.client.utility.PathUtil;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Created by aaa
 * It is not recommended to be used as a global variable
 */
public abstract class LeaderElection {
    private static final Logger logger = LoggerFactory.getLogger(LeaderElection.class);
    private boolean done = false;
    private int retryCount;
    
    public LeaderElection(){
        retryCount = Properties.INSTANCE.getNodeElectionCount();
    }

    private boolean contend(final String node, final IProvider provider, final ZookeeperEventListener listener) throws KeeperException, InterruptedException {
        boolean success = false;
        try {
            provider.create(node, Properties.INSTANCE.getClientId(), CreateMode.EPHEMERAL); // todo EPHEMERAL_SEQUENTIAL check index value
            success = true;
        } catch (KeeperException.NodeExistsException e) {
            logger.info("contend not success");
            // TODO: or changing_key node value == current client id
            provider.exists(node, WatcherCreator.deleteWatcher(listener));
        }
        return success;
    }
    
    /**
     * listener will be register when the contention of the path is unsuccessful.
     *
     * @param nodeBeContend nodeBeContend
     * @param provider provider
     * @throws KeeperException Zookeeper Exception
     * @throws InterruptedException InterruptedException
     */
    public void executeContention(final String nodeBeContend, final IProvider provider) throws KeeperException, InterruptedException {
        boolean canBegin;
        final String realNode = provider.getRealPath(nodeBeContend);
        final String contendNode = PathUtil.getRealPath(realNode, ZookeeperConstants.CHANGING_KEY);
        canBegin = this.contend(contendNode, provider, new ZookeeperEventListener(contendNode) {
            @Override
            public void process(final WatchedDataEvent event) {
                try {
                    retryCount--;
                    if (retryCount < 0) {
                        logger.info("Election node exceed retry count");
                        return;
                    }
                    executeContention(realNode, provider);
                } catch (Exception ee) {
                    logger.error("ZookeeperEventListener Exception executeContention:{}", ee.getMessage(), ee);
                }
            }
        });
    
        if (canBegin) {
            try {
                action();
                done = true;
                callback();
            } catch (Exception ee) {
                logger.error("action Exception executeContention:{}", ee.getMessage(), ee);
            }
            provider.delete(contendNode);
        }
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
