package com.saaavsaaa.client.action;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

/*
 * Created by aaa
 */
interface IGroupAction {
    
    /**
     * create target node and all need created.
     *
     * @param key key
     * @param value value
     * @param createMode createMode
     * @throws KeeperException Zookeeper Exception
     * @throws InterruptedException InterruptedException
     */
    void createAllNeedPath(String key, String value, CreateMode createMode) throws KeeperException, InterruptedException;
    
    /**
     * delete target node and children nodes.
     *
     * @param key key
     * @throws KeeperException Zookeeper Exception
     * @throws InterruptedException InterruptedException
     */
    void deleteAllChildren(String key) throws KeeperException, InterruptedException;
    
    /**
     * delete the current node with force and delete the super node whose only child node is current node recursively.
     *
     * @param key key
     * @throws KeeperException Zookeeper Exception
     * @throws InterruptedException InterruptedException
     */
    void deleteCurrentBranch(String key) throws KeeperException, InterruptedException;
}
