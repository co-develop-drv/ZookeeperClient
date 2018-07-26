package com.saaavsaaa.client.zookeeper.core;

import com.saaavsaaa.client.utility.constant.ZookeeperConstants;
import com.saaavsaaa.client.zookeeper.section.ClientContext;
import com.saaavsaaa.client.zookeeper.section.WatchedDataEvent;
import com.saaavsaaa.client.zookeeper.section.WatcherCreator;
import com.saaavsaaa.client.zookeeper.section.ZookeeperEventListener;
import com.saaavsaaa.client.zookeeper.strategy.AllAsyncRetryStrategy;
import com.saaavsaaa.client.zookeeper.strategy.AsyncRetryStrategy;
import com.saaavsaaa.client.zookeeper.strategy.ContentionStrategy;
import com.saaavsaaa.client.zookeeper.strategy.SyncRetryStrategy;
import com.saaavsaaa.client.zookeeper.strategy.TransactionContendStrategy;
import com.saaavsaaa.client.action.IClient;
import com.saaavsaaa.client.action.IExecStrategy;
import com.saaavsaaa.client.action.ITransactionProvider;
import com.saaavsaaa.client.utility.PathUtil;
import com.saaavsaaa.client.utility.StringUtil;
import com.saaavsaaa.client.zookeeper.provider.TransactionProvider;
import com.saaavsaaa.client.utility.constant.StrategyType;
import com.saaavsaaa.client.zookeeper.strategy.UsualStrategy;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.ACL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/*
 * Created by aaa
 */
public abstract class BaseClient implements IClient {
    private static final Logger logger = LoggerFactory.getLogger(BaseClient.class);
    
    protected final boolean watched = true; //false
    protected final Map<StrategyType, IExecStrategy> strategies = new ConcurrentHashMap<>();
    
    protected IExecStrategy strategy;
    protected BaseContext context;
    protected List<ACL> authorities;
    protected Holder holder;
    
    protected String rootNode = "/InitValue";
    protected boolean rootExist = false;
    
    protected BaseClient(final BaseContext context) {
        this.context = context;
    }
    
    @Override
    public void start() throws IOException, InterruptedException {
        holder = new Holder(getContext());
        holder.start();
    }
    
    @Override
    public synchronized boolean start(final int wait, final TimeUnit units) throws InterruptedException, IOException {
        holder = new Holder(getContext());
        holder.start(wait, units);
        return holder.isConnected();
    }
    
    @Override
    public void close() {
        holder.close();
        context.close();
        this.strategies.clear();
    }
    
    @Override
    public synchronized void useExecStrategy(final StrategyType strategyType) {
        logger.debug("useExecStrategy:{}", strategyType);
        if (strategies.containsKey(strategyType)) {
            strategy = strategies.get(strategyType);
            return;
        }
        
        ITransactionProvider provider = new TransactionProvider(rootNode, holder, watched, authorities);
        switch (strategyType) {
            case USUAL: {
                strategy = new UsualStrategy(provider);
                break;
            }
            case CONTEND: {
                strategy = new ContentionStrategy(provider);
                break;
            }
            case TRANSACTION_CONTEND: {
                strategy = new TransactionContendStrategy(provider);
                break;
            }
            case SYNC_RETRY: {
                strategy = new SyncRetryStrategy(provider, ((ClientContext)context).getDelayRetryPolicy());
                break;
            }
            case ASYNC_RETRY: {
                strategy = new AsyncRetryStrategy(provider, ((ClientContext)context).getDelayRetryPolicy());
                break;
            }
            case ALL_ASYNC_RETRY: {
                strategy = new AllAsyncRetryStrategy(provider, ((ClientContext)context).getDelayRetryPolicy());
                break;
            }
            default: {
                strategy = new UsualStrategy(provider);
                break;
            }
        }
        
        strategies.put(strategyType, strategy);
    }
    
    void registerWatch(final ZookeeperEventListener globalListener) {
        if (context.globalListener != null) {
            logger.warn("global listener can only register one");
            return;
        }
        context.globalListener = globalListener;
        logger.debug("globalListenerRegistered:{}", globalListener.getKey());
    }
    
    @Override
    public void registerWatch(final String key, final ZookeeperEventListener listener) {
        String path = PathUtil.getRealPath(rootNode, key);
        listener.setPath(path);
        context.getWatchers().put(listener.getKey(), listener);
        checkWatcher(path);
        logger.debug("register watcher:{}", path);
    }
    
    private void checkWatcher(final String path) {
        if (holder.isConnected()) {
            try {
                this.checkExists(path);
            } catch (final KeeperException | InterruptedException ex) {
                // ignore
                logger.warn("check watcher:{}", ex.getMessage());
            }
        }
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
        createNamespace(ZookeeperConstants.NOTHING_DATA);
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
        holder.zooKeeper.exists(rootNode, WatcherCreator.deleteWatcher(new ZookeeperEventListener(rootNode) {
            @Override
            public void process(final WatchedDataEvent event) {
                rootExist = false;
            }
        }));
        logger.debug("created root:{}", rootNode);
    }
    
    protected void deleteNamespace() throws KeeperException, InterruptedException {
        holder.zooKeeper.delete(rootNode, ZookeeperConstants.VERSION);
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
    
    public IExecStrategy getStrategy() {
        return strategy;
    }
}
