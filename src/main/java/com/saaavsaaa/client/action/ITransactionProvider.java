package com.saaavsaaa.client.action;

import com.saaavsaaa.client.zookeeper.transaction.ZKTransaction;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

/**
 * Created by aaa
 */
public interface ITransactionProvider extends IProvider {
    void createInTransaction(String key, String value, CreateMode createMode, ZKTransaction transaction) throws KeeperException, InterruptedException;
}
