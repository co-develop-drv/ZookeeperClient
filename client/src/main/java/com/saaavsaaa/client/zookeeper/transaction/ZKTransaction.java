package com.saaavsaaa.client.zookeeper.transaction;

import com.saaavsaaa.client.utility.constant.ZookeeperConstants;
import com.saaavsaaa.client.zookeeper.core.Holder;
import com.saaavsaaa.client.utility.PathUtil;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/*
 * Created by aaa
 */
public class ZKTransaction extends BaseTransaction {
    private static final Logger logger = LoggerFactory.getLogger(ZKTransaction.class);
    private final Holder holder;
    private final String rootNode;

    public ZKTransaction(final String root, final Holder holder) {
        this.holder = holder;
        rootNode = root;
        logger.debug("ZKTransaction root:{}", rootNode);
    }
    
    private Transaction getTransaction() {
        try {
            return holder.getZooKeeper().transaction();
        } catch (Exception e) {
            logger.error("getTransaction:{}", e.getMessage(), e);
            throw new UnsupportedClassVersionError("check zk version");
        }
    }
    
    @Override
    public ZKTransaction create(final String path, final byte[] data, final List<ACL> acl, final CreateMode createMode) {
        this.getTransaction().create(PathUtil.getRealPath(rootNode, path), data, acl, createMode);
        logger.debug("wait create:{},data:{},acl:{},createMode:{}", new Object[]{path, data, acl, createMode});
        return this;
    }
    
    @Override
    public ZKTransaction delete(final String path){
        return delete(path, ZookeeperConstants.VERSION);
    }
    
    @Override
    public ZKTransaction delete(final String path, final int version) {
        this.getTransaction().delete(PathUtil.getRealPath(rootNode, path), version);
        logger.debug("wait delete:{}", path);
        return this;
    }
    
    @Override
    public ZKTransaction check(final String path){
        return check(path, ZookeeperConstants.VERSION);
    }
    
    @Override
    public ZKTransaction check(final String path, final int version) {
        this.getTransaction().check(PathUtil.getRealPath(rootNode, path), version);
        logger.debug("wait check:{}", path);
        return this;
    }
    
    @Override
    public ZKTransaction setData(final String path, final byte[] data) {
        return setData(path, data, ZookeeperConstants.VERSION);
    }
    
    @Override
    public ZKTransaction setData(final String path, final byte[] data, final int version) {
        this.getTransaction().setData(PathUtil.getRealPath(rootNode, path), data, version);
        logger.debug("wait setData:{},data:{}", path, data);
        return this;
    }
    
    @Override
    public List<OpResult> commit() throws InterruptedException, KeeperException {
        logger.debug("ZKTransaction commit");
        return this.getTransaction().commit();
    }
    
    @Override
    public void commit(final AsyncCallback.MultiCallback cb, final Object ctx) {
        this.getTransaction().commit(cb, ctx);
        logger.debug("ZKTransaction commit ctx:{}", ctx);
    }
}
