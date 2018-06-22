package com.saaavsaaa.client.zookeeper.transaction;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.OpResult;
import org.apache.zookeeper.data.ACL;

import java.util.List;

/*
 * Created by aaa
 */
public class BaseTransaction {
    public ZKTransaction create(final String path, final byte[] data, final List<ACL> acl, final CreateMode createMode) {
        throw new UnsupportedOperationException("check zk version!");
    }
    
    public ZKTransaction delete(final String path){
        throw new UnsupportedOperationException("check zk version!");
    }
    public ZKTransaction delete(final String path, final int version) {
        throw new UnsupportedOperationException("check zk version!");
    }
    
    public ZKTransaction check(final String path){
        throw new UnsupportedOperationException("check zk version!");
    }
    public ZKTransaction check(final String path, final int version) {
        throw new UnsupportedOperationException("check zk version!");
    }
    
    public ZKTransaction setData(final String path, final byte[] data){
        throw new UnsupportedOperationException("check zk version!");
    }
    public ZKTransaction setData(final String path, final byte[] data, final int version) {
        throw new UnsupportedOperationException("check zk version!");
    }
    
    public List<OpResult> commit() throws InterruptedException, KeeperException {
        throw new UnsupportedOperationException("check zk version!");
    }
    
    public void commit(final AsyncCallback.MultiCallback cb, final Object ctx) {
        throw new UnsupportedOperationException("check zk version!");
    }
}
