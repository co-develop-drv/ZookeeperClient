package com.saaavsaaa.client.zookeeper.core;

import com.saaavsaaa.client.action.IClient;
import com.saaavsaaa.client.utility.PathUtil;
import com.saaavsaaa.client.utility.StringUtil;
import com.saaavsaaa.client.utility.constant.Constants;
import com.saaavsaaa.client.utility.constant.StrategyType;
import com.saaavsaaa.client.zookeeper.section.WatcherCreator;
import com.saaavsaaa.client.zookeeper.section.ZookeeperListener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.data.ACL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/*
 * Created by aaa
 */
public abstract class BaseClient implements IClient {
    private static final Logger logger = LoggerFactory.getLogger(BaseClient.class);

    private static final int CIRCLE_WAIT = 30;
    protected List<ACL> authorities;
    protected boolean rootExist = false;
    protected Holder holder;
    protected String rootNode = Constants.ROOT_INIT_PATH;
    protected BaseContext context;
    protected final boolean watched = true; //false

    protected BaseClient(final BaseContext context) {
        this.context = context;
    }
    
    @Override
    public void start() throws IOException, InterruptedException {
        prepareStart();
        holder.start();
    }
    
    @Override
    public synchronized boolean start(final int wait, final TimeUnit units) throws InterruptedException, IOException {
        prepareStart();
        holder.start(wait, units);
        return holder.isConnected();
    }

    private void prepareStart() {
        holder = new Holder(getContext());
        useExecStrategy(StrategyType.USUAL);
    }

    @Override
    public synchronized boolean blockUntilConnected(final int waitingTime, final TimeUnit timeUnit) throws InterruptedException {
        long maxWait = timeUnit != null ? TimeUnit.MILLISECONDS.convert(waitingTime, timeUnit) : 0;
        while (!holder.isConnected()) {
            long waitTime = maxWait - CIRCLE_WAIT;
            if (waitTime <= 0) {
                return holder.isConnected();
            }
            wait(CIRCLE_WAIT);
        }
        return true;
    }

    @Override
    public void close() {
        context.close();
        try {
            if (rootExist) {
                this.deleteNamespace();
            }
        } catch (final KeeperException | InterruptedException ex) {
            logger.error("zk client close delete root error:{}", ex.getMessage(), ex);
        }
        holder.close();
    }
    
    void registerWatch(final ZookeeperListener globalListener) {
        if (context.globalListener != null) {
            logger.warn("global listener can only register one");
            return;
        }
        context.globalListener = globalListener;
        logger.debug("globalListenerRegistered:{}", globalListener.getKey());
    }
    
    @Override
    public void registerWatch(final String key, final ZookeeperListener listener) {
        String path = PathUtil.getRealPath(rootNode, key);
        listener.setPath(path);
        context.getWatchers().put(listener.getKey(), listener);
        logger.debug("register watcher:{}", path);
    }
    
    @Override
    public void unregisterWatch(final String key) {
        if (StringUtil.isNullOrBlank(key)) {
            throw new IllegalArgumentException("key should not be blank");
        }
//        String path = PathUtil.getRealPath(rootNode, key);
        if (context.getWatchers().containsKey(key)) {
            context.getWatchers().remove(key);
            logger.debug("unregisterWatch:{}", key);
        }
    }
    
    protected void createNamespace() throws KeeperException, InterruptedException {
        createNamespace(Constants.NOTHING_DATA);
    }
    
   private void createNamespace(final byte[] date) throws KeeperException, InterruptedException {
        if (rootExist) {
            logger.debug("root exist");
            return;
        }
        try {
            if (null == holder.getZooKeeper().exists(rootNode, false)) {
                holder.zooKeeper.create(rootNode, date, authorities, CreateMode.PERSISTENT);
            }
            rootExist = true;
            logger.debug("creating root:{}", rootNode);
        } catch (KeeperException.NodeExistsException ee) {
            logger.warn("root create:{}", ee.getMessage());
            rootExist = true;
            return;
        }
        holder.zooKeeper.exists(rootNode, WatcherCreator.deleteWatcher(new ZookeeperListener(rootNode) {
            @Override
            public void process(final WatchedEvent event) {
                rootExist = false;
            }
        }));
        logger.debug("created root:{}", rootNode);
    }
    
    protected void deleteNamespace() throws KeeperException, InterruptedException {
        try {
            holder.getZooKeeper().delete(rootNode, Constants.VERSION);
        } catch (final KeeperException.NodeExistsException | KeeperException.NotEmptyException ex) {
            logger.info("delete root :{}", ex.getMessage());
        }
        rootExist = false;
        logger.debug("delete root:{},rootExist:{}", rootNode, rootExist);
    }
    
    void setRootNode(final String rootNode) {
        this.rootNode = rootNode;
    }
    
    void setAuthorities(final String scheme, final byte[] auth, final List<ACL> authorities) {
        context.scheme = scheme;
        context.auth = auth;
        this.authorities = authorities;
    }
    
    public BaseContext getContext(){
        return context;
    }
}
