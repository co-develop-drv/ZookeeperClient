package com.saaavsaaa.client.action;

import com.saaavsaaa.client.zookeeper.transaction.ZKTransaction;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

/*
 * Created by aaa
 */
public interface ITransactionProvider extends IProvider {
    /**
     * only create target node.
     *
     * @param key key
     * @param value value
     * @param createMode createMode
     * @param transaction transaction
     * @throws KeeperException Zookeeper Exception
     * @throws InterruptedException InterruptedException
     */
    void createInTransaction(String key, String value, CreateMode createMode, ZKTransaction transaction) throws KeeperException, InterruptedException;
}
