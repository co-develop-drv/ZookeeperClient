package com.saaavsaaa.client.action;

import com.saaavsaaa.client.election.LeaderElection;
import com.saaavsaaa.client.zookeeper.transaction.BaseTransaction;
import com.saaavsaaa.client.zookeeper.transaction.ZKTransaction;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;

import java.util.List;
import java.util.Stack;

/*
 * Created by aaa
 */
public interface IProvider {
    
    /**
     * get string type data.
     *
     * @param key key
     * @return data String
     * @throws KeeperException Zookeeper Exception
     * @throws InterruptedException InterruptedException
     */
    String getDataString(String key) throws KeeperException, InterruptedException;
    
    /**
     * get string type data.
     *
     * @param key key
     * @return data
     * @throws KeeperException Zookeeper Exception
     * @throws InterruptedException InterruptedException
     */
    byte[] getData(String key) throws KeeperException, InterruptedException;
    
    /**
     * get string type data.
     *
     * @param key key
     * @param callback callback
     * @param ctx ctx
     * @throws KeeperException Zookeeper Exception
     * @throws InterruptedException InterruptedException
     */
    void getData(String key, AsyncCallback.DataCallback callback, Object ctx) throws KeeperException, InterruptedException;
    
    /**
     * check exist.
     *
     * @param key key
     * @return exist
     * @throws KeeperException Zookeeper Exception
     * @throws InterruptedException InterruptedException
     */
    boolean exists(String key) throws KeeperException, InterruptedException;
    
    /**
     * check exist.
     *
     * @param key key
     * @param watcher watcher
     * @return exist
     * @throws KeeperException Zookeeper Exception
     * @throws InterruptedException InterruptedException
     */
    boolean exists(String key, Watcher watcher) throws KeeperException, InterruptedException;
    
    /**
     * get children's keys.
     *
     * @param key key
     * @return exist
     * @throws KeeperException Zookeeper Exception
     * @throws InterruptedException InterruptedException
     */
    List<String> getChildren(String key) throws KeeperException, InterruptedException;
    
    /**
     * only create target node.
     *
     * @param key key
     * @param value value
     * @param createMode createMode
     * @throws KeeperException Zookeeper Exception
     * @throws InterruptedException InterruptedException
     */
    void create(String key, String value, CreateMode createMode) throws KeeperException, InterruptedException;
    
    /**
     * update.
     *
     * @param key key
     * @param value value
     * @throws KeeperException Zookeeper Exception
     * @throws InterruptedException InterruptedException
     */
    void update(String key, String value) throws KeeperException, InterruptedException;
    
    /**
     * only delete target node..
     *
     * @param key key
     * @throws KeeperException Zookeeper Exception
     * @throws InterruptedException InterruptedException
     */
    void delete(String key) throws KeeperException, InterruptedException;
    
    /**
     * only delete target node..
     *
     * @param key key
     * @param callback callback
     * @param ctx ctx
     * @throws KeeperException Zookeeper Exception
     * @throws InterruptedException InterruptedException
     */
    void delete(String key, AsyncCallback.VoidCallback callback, Object ctx) throws KeeperException, InterruptedException;
    
    /**
     * get real path with root.
     *
     * @param path path
     * @return real path
     */
    String getRealPath(String path);
    
    /**
     * get path nodes that needed create.
     *
     * @param key key
     * @return all path nodes
     */
    List<String> getNecessaryPaths(String key);
    
    /**
     * get path nodes that needed delete.
     *
     * @param key key
     * @return all path nodes
     */
    Stack<String> getDeletingPaths(String key);
    
    /**
     * contention exec.
     *
     * @param election election
     * @throws KeeperException Zookeeper Exception
     * @throws InterruptedException InterruptedException
     */
    void executeContention(LeaderElection election) throws KeeperException, InterruptedException;
    
    /**
     * reset connection.
     */
    void resetConnection();
    
    /**
     * create transaction.
     *
     * @return BaseTransaction
     */
    BaseTransaction transaction();
}
