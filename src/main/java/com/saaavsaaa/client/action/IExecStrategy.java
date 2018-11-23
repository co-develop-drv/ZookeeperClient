package com.saaavsaaa.client.action;

import com.saaavsaaa.client.zookeeper.transaction.BaseTransaction;

/*
 * Created by aaa
 */
public interface IExecStrategy extends IAction, IGroupAction {
    
    /**
     * get provider.
     *
     * @return provider
     */
    IProvider getProvider();
    
    /**
     * create transaction.
     *
     * @return zookeeper transaction
     */
    BaseTransaction transaction();
}
