package com.saaavsaaa.client.action;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;

import java.util.List;

/**
 * Created by aaa
 */
public interface IAction {
    /**
     * get string type data.
     *
     * @param key key
     * @return data String
     * @throws KeeperException Zookeeper Exception
     * @throws InterruptedException InterruptedException
     */
    String getDataString(String key) throws KeeperException, InterruptedException;
    
    /**
     * get string type data.
     *
     * @param key key
     * @return data
     * @throws KeeperException Zookeeper Exception
     * @throws InterruptedException InterruptedException
     */
    byte[] getData(String key) throws KeeperException, InterruptedException;
    
    /**
     * get string type data.
     *
     * @param key key
     * @param callback callback
     * @param ctx ctx
     * @throws KeeperException Zookeeper Exception
     * @throws InterruptedException InterruptedException
     */
    void getData(String key, AsyncCallback.DataCallback callback, Object ctx) throws KeeperException, InterruptedException;
    
    /**
     * check exist.
     *
     * @param key key
     * @return exist
     * @throws KeeperException Zookeeper Exception
     * @throws InterruptedException InterruptedException
     */
    boolean checkExists(String key) throws KeeperException, InterruptedException;
    
    /**
     * check exist.
     *
     * @param key key
     * @param watcher watcher
     * @return exist
     * @throws KeeperException Zookeeper Exception
     * @throws InterruptedException InterruptedException
     */
    boolean checkExists(String key, Watcher watcher) throws KeeperException, InterruptedException;
    
    /**
     * get children's keys.
     *
     * @param key key
     * @return children keys
     * @throws KeeperException Zookeeper Exception
     * @throws InterruptedException InterruptedException
     */
    List<String> getChildren(String key) throws KeeperException, InterruptedException;
    
    /**
     * only create target node.
     *
     * @param key key
     * @param value value
     * @param createMode createMode
     * @throws KeeperException Zookeeper Exception
     * @throws InterruptedException InterruptedException
     */
    void createCurrentOnly(String key, String value, CreateMode createMode) throws KeeperException, InterruptedException;
    
    /**
     * update.
     *
     * @param key key
     * @param value value
     * @throws KeeperException Zookeeper Exception
     * @throws InterruptedException InterruptedException
     */
    void update(String key, String value) throws KeeperException, InterruptedException;
    
    /**
     * only delete target node..
     *
     * @param key key
     * @throws KeeperException Zookeeper Exception
     * @throws InterruptedException InterruptedException
     */
    void deleteOnlyCurrent(String key) throws KeeperException, InterruptedException;
    
    /**
     * only delete target node..
     *
     * @param key key
     * @param callback callback
     * @param ctx ctx
     * @throws KeeperException Zookeeper Exception
     * @throws InterruptedException InterruptedException
     */
    void deleteOnlyCurrent(final String key, final AsyncCallback.VoidCallback callback, final Object ctx) throws KeeperException, InterruptedException;
}
