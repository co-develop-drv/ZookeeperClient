package com.saaavsaaa.client.action;

import com.saaavsaaa.client.utility.constant.StrategyType;
import com.saaavsaaa.client.zookeeper.section.ZookeeperListener;
import com.saaavsaaa.client.zookeeper.transaction.BaseTransaction;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/*
 * Created by aaa
 */
public interface IClient extends IAction, IGroupAction {
    /**
     * Start.
     *
     * @throws IOException IO exception
     * @throws InterruptedException interrupted exception
     */
    void start() throws IOException, InterruptedException;

    /**
     * Start until out.
     *
     * @param waitingTime waiting time
     * @param timeUnit time unit
     * @return connected or not
     * @throws IOException IO exception
     * @throws InterruptedException interrupted exception
     */
    boolean start(int waitingTime, TimeUnit timeUnit) throws IOException, InterruptedException;

    /**
     * Block until connected.
     *
     * @param waitingTime waiting time
     * @param timeUnit time unit
     * @return connected or not
     * @throws InterruptedException interrupted exception
     */
    boolean blockUntilConnected(int waitingTime, TimeUnit timeUnit) throws InterruptedException;

    /**
     * Register watcher.
     *
     * @param key key
     * @param eventListener zookeeper event listener
     */
    void registerWatch(String key, ZookeeperListener eventListener);

    /**
     * Unregister watcher.
     *
     * @param key key
     */
    void unregisterWatch(String key);

    /**
     * Choice exec strategy.
     *
     * @param strategyType strategyType
     */
    void useExecStrategy(StrategyType strategyType);

    /**
     * Get execution strategy.
     *
     * @return execution strategy
     */
    IExecStrategy getExecStrategy();

    /**
     * Create zookeeper transaction.
     *
     * @return zookeeper transaction
     */
    BaseTransaction transaction();

    /**
     * Close.
     */
    void close();
    /*
    void createNamespace();
    void deleteNamespace();
    
    Watcher registerWatch(Listener listener);
    void setRootNode(String namespace);
    
    void setAuthorities(String scheme, byte[] auth);
    ZooKeeper getZooKeeper();
    */
}
