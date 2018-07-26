package com.saaavsaaa.client.action;

import com.saaavsaaa.client.zookeeper.section.ZookeeperEventListener;
import com.saaavsaaa.client.zookeeper.transaction.BaseTransaction;
import com.saaavsaaa.client.utility.constant.StrategyType;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/*
 * Created by aaa
 */
public interface IClient extends IAction, IGroupAction {
    /**
     * start.
     *
     * @throws IOException IO Exception
     * @throws InterruptedException InterruptedException
     */
    void start() throws IOException, InterruptedException;
    
    /**
     * block until connected.
     *
     * @param wait wait
     * @param units units
     * @return connected
     * @throws IOException IO Exception
     * @throws InterruptedException InterruptedException
     */
    boolean start(int wait, TimeUnit units) throws IOException, InterruptedException;
    
    /**
     * close.
     */
    void close();
    
    /**
     * register watcher.
     *
     * @param key key
     * @param listener listener
     */
    void registerWatch(String key, ZookeeperEventListener listener);
    
    /**
     * unregister watcher.
     *
     * @param key key
     */
    void unregisterWatch(String key);
    
    /**
     * choice exec strategy.
     *
     * @param strategyType strategyType
     */
    void useExecStrategy(StrategyType strategyType);
    
    /**
     * create transaction.
     *
     * @return BaseTransaction
     */
    BaseTransaction transaction();
    /*
    void createNamespace();
    void deleteNamespace();
    
    Watcher registerWatch(ZookeeperEventListener listener);
    void setRootNode(String namespace);
    
    void setAuthorities(String scheme, byte[] auth);
    ZooKeeper getZooKeeper();
    */
}
