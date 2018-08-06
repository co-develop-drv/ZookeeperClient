package com.saaavsaaa.client.zookeeper.provider;

import com.saaavsaaa.client.utility.constant.ZookeeperConstants;
import com.saaavsaaa.client.zookeeper.core.Holder;
import com.saaavsaaa.client.action.ITransactionProvider;
import com.saaavsaaa.client.zookeeper.transaction.BaseTransaction;
import com.saaavsaaa.client.zookeeper.transaction.ZKTransaction;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.ACL;

import java.util.List;

/*
 * Created by aaa
 */
public class TransactionProvider extends BaseProvider implements ITransactionProvider {
    public TransactionProvider(final String rootNode, final Holder holder, final boolean watched, final List<ACL> authorities) {
        super(rootNode, holder, watched, authorities);
    }
    
    @Override
    public void createInTransaction(final String key, final String value, final CreateMode createMode, final ZKTransaction transaction) throws KeeperException, InterruptedException {
        transaction.create(key, value.getBytes(ZookeeperConstants.UTF_8), authorities, createMode);
    }
    
    @Override
    public BaseTransaction transaction() {
        return new ZKTransaction(rootNode, holder);
    }
}
