package com.saaavsaaa.client.action;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

/*
 * Created by aaa
 */
interface IGroupAction {

    /**
     * Create target node and all need created.
     *
     * @param key key
     * @param value value
     * @param createMode create mode
     * @throws KeeperException zookeeper exception
     * @throws InterruptedException interrupted exception
     */
    void createAllNeedPath(String key, String value, CreateMode createMode) throws KeeperException, InterruptedException;

    /**
     * Delete target node and children nodes.
     *
     * @param key key
     * @throws KeeperException zookeeper exception
     * @throws InterruptedException interrupted exception
     */
    void deleteAllChildren(String key) throws KeeperException, InterruptedException;

    /**
     * Delete the current node with force and delete the super node whose only child node is current node recursively.
     *
     * @param key key
     * @throws KeeperException zookeeper exception
     * @throws InterruptedException interrupted exception
     */
    void deleteCurrentBranch(String key) throws KeeperException, InterruptedException;
}
