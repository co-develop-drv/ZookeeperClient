package com.saaavsaaa.client.action;

import com.saaavsaaa.client.zookeeper.transaction.BaseTransaction;

/*
 * Created by aaa
 */
public interface IExecStrategy extends IAction, IGroupAction {
    
    /**
     * get provider.
     *
     * @return IProvider
     */
    IProvider getProvider();
    
    /**
     * create transaction.
     *
     * @return BaseTransaction
     */
    BaseTransaction transaction();
}
