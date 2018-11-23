package com.saaavsaaa.client.action;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;

import java.util.List;

/*
 * Created by aaa
 */
interface IAction {

    // TODO: check children exists

    /**
     * Get string type data.
     *
     * @param key key
     * @return data String
     * @throws KeeperException zookeeper exception
     * @throws InterruptedException interrupted exception
     */
    String getDataString(String key) throws KeeperException, InterruptedException;

    /**
     * Get string type data.
     *
     * @param key key
     * @return data
     * @throws KeeperException zookeeper exception
     * @throws InterruptedException interrupted exception
     */
    byte[] getData(String key) throws KeeperException, InterruptedException;

    /**
     * Get string type data.
     *
     * @param key key
     * @param callback callback
     * @param ctx context
     * @throws KeeperException zookeeper exception
     * @throws InterruptedException interrupted exception
     */
    void getData(String key, AsyncCallback.DataCallback callback, Object ctx) throws KeeperException, InterruptedException;

    /**
     * Check exist.
     *
     * @param key key
     * @return exist or not
     * @throws KeeperException zookeeper exception
     * @throws InterruptedException interrupted exception
     */
    boolean checkExists(String key) throws KeeperException, InterruptedException;

    /**
     * Check exist.
     *
     * @param key key
     * @param watcher watcher
     * @return exist or not
     * @throws KeeperException zookeeper exception
     * @throws InterruptedException interrupted exception
     */
    boolean checkExists(String key, Watcher watcher) throws KeeperException, InterruptedException;

    /**
     * Get children's keys.
     *
     * @param key key
     * @return children keys
     * @throws KeeperException zookeeper exception
     * @throws InterruptedException interrupted exception
     */
    List<String> getChildren(String key) throws KeeperException, InterruptedException;

    /**
     * Only create target node.
     *
     * @param key key
     * @param value value
     * @param createMode createMode
     * @throws KeeperException zookeeper exception
     * @throws InterruptedException interrupted exception
     */
    void createCurrentOnly(String key, String value, CreateMode createMode) throws KeeperException, InterruptedException;

    /**
     * Update.
     *
     * @param key key
     * @param value value
     * @throws KeeperException zookeeper exception
     * @throws InterruptedException interrupted exception
     */
    void update(String key, String value) throws KeeperException, InterruptedException;

    /**
     * Only delete target node..
     *
     * @param key key
     * @throws KeeperException zookeeper exception
     * @throws InterruptedException interrupted exception
     */
    void deleteOnlyCurrent(String key) throws KeeperException, InterruptedException;

    /**
     * Only delete target node..
     *
     * @param key key
     * @param callback callback
     * @param ctx context
     * @throws KeeperException zookeeper exception
     * @throws InterruptedException interrupted exception
     */
    void deleteOnlyCurrent(String key, AsyncCallback.VoidCallback callback, Object ctx) throws KeeperException, InterruptedException;
}
