package com.saaavsaaa.client.action;

import com.saaavsaaa.client.zookeeper.section.Listener;
import com.saaavsaaa.client.utility.constant.StrategyType;
import com.saaavsaaa.client.zookeeper.transaction.ZKTransaction;

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
    void registerWatch(String key, Listener listener);
    
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
     * @return ZKTransaction
     */
    ZKTransaction transaction();
    /*
    void createNamespace();
    void deleteNamespace();
    
    Watcher registerWatch(Listener listener);
    void setRootNode(String namespace);
    
    void setAuthorities(String scheme, byte[] auth);
    ZooKeeper getZooKeeper();
    */
}
