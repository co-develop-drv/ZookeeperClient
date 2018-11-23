package com.saaavsaaa.client.action;

import com.saaavsaaa.client.zookeeper.transaction.BaseTransaction;
import org.apache.zookeeper.CreateMode;

/*
 * Created by aaa
 */
public interface ITransactionProvider extends IProvider {
    /**
     * Only create target node in transaction.
     *
     * @param key key
     * @param value value
     * @param createMode create mode
     * @param transaction zookeeper transaction
     */
    void createInTransaction(String key, String value, CreateMode createMode, BaseTransaction transaction);
}
