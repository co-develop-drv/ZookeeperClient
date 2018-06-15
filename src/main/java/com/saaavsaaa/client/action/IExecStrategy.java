package com.saaavsaaa.client.action;

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
}
