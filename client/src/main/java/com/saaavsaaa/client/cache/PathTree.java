package com.saaavsaaa.client.cache;

import com.saaavsaaa.client.action.IClient;
import com.saaavsaaa.client.action.IProvider;
import com.saaavsaaa.client.utility.PathUtil;
import com.saaavsaaa.client.utility.Properties;
import com.saaavsaaa.client.utility.constant.ZookeeperConstants;
import com.saaavsaaa.client.zookeeper.core.BaseClient;
import com.saaavsaaa.client.zookeeper.section.ZookeeperEventListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.common.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * zookeeper cache tree
 *
 * Created by aaa
 * todo provider
 */
public final class PathTree {
    private static final Logger logger = LoggerFactory.getLogger(PathTree.class);
    private final transient ReentrantLock lock = new ReentrantLock();
    private final AtomicReference<PathNode> rootNode = new AtomicReference<>();
    private boolean executorStart = false;
    private ScheduledExecutorService cacheService;
    private final IClient client;
    private final IProvider provider;
    private PathStatus status;
    private boolean closed = false;
    
    public PathTree(final String root, final IClient client) {
        this.rootNode.set(new PathNode(root));
        this.status = PathStatus.RELEASE;
        this.client = client;
        this.provider = ((BaseClient)client).getStrategy().getProvider();
    }
    
    /**
     * load data.
     *
     * @throws KeeperException Zookeeper Exception
     * @throws InterruptedException InterruptedException
     */
    public void load() throws KeeperException, InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        if (closed) {
            return;
        }
        try {
            if (status == PathStatus.RELEASE) {
                logger.debug("loading Status:{}", status);
                this.setStatus(PathStatus.CHANGING);
        
                PathNode newRoot = new PathNode(rootNode.get().getKey());
                List<String> children = provider.getChildren(rootNode.get().getKey());
                children.remove(PathUtil.getRealPath(rootNode.get().getKey(), ZookeeperConstants.CHANGING_KEY));
                this.attechIntoNode(children, newRoot);
                rootNode.set(newRoot);
        
                this.setStatus(PathStatus.RELEASE);
//                watch();
                logger.debug("loading release:{}", status);
            } else {
                logger.info("loading but cache status not release");
                try {
                    Thread.sleep(10L);
                } catch (InterruptedException e) {
                    logger.error("loading sleep error:{}", e.getMessage(), e);
                }
                load();
            }
        } finally {
            lock.unlock();
        }
    }
    
    private void attechIntoNode(final List<String> children, final PathNode pathNode) throws KeeperException, InterruptedException {
        if (closed) {
            return;
        }
        logger.debug("attechIntoNode children:{}", children);
        if (children.isEmpty()) {
            logger.info("attechIntoNode there are no children");
            return;
        }
        for (String child : children) {
            String childPath = PathUtil.getRealPath(pathNode.getKey(), child);
            PathNode current = new PathNode(PathUtil.checkPath(child), provider.getData(childPath));
            pathNode.attachChild(current);
            List<String> subs = provider.getChildren(childPath);
            this.attechIntoNode(subs, current);
        }
    }
    
    /**
     * start thread pool period load data.
     *
     * @param period period
     */
    public void refreshPeriodic(final long period) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        if (closed) {
            return;
        }
        try {
            if (executorStart) {
                throw new IllegalArgumentException("period already set");
            }
            long threadPeriod = period;
            if (threadPeriod < 1) {
                threadPeriod = Properties.INSTANCE.getThreadPeriod();
            }
            logger.debug("refreshPeriodic:{}", period);
            cacheService = Executors.newSingleThreadScheduledExecutor();
            cacheService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    logger.debug("cacheService run:{}", getStatus());
                    if (PathStatus.RELEASE == getStatus()) {
                        try {
                            load();
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            }, Properties.INSTANCE.getThreadInitialDelay(), threadPeriod, TimeUnit.MILLISECONDS);
            executorStart = true;
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    stopRefresh();
                }
            }));
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * stop thread pool period load data.
     */
    public void stopRefresh() {
        cacheService.shutdown();
        executorStart = false;
        logger.debug("stopRefresh");
    }
    
    /**
     * watch data change.
     */
    public void watch() {
        watch(new ZookeeperEventListener(rootNode.get().getKey()) {
            @Override
            public void process(final WatchedEvent event) {
                String path = event.getPath();
                logger.debug("PathTree Watch event:{}", event.toString());
                switch (event.getType()) {
                    case NodeCreated:
                    case NodeDataChanged:
                    case NodeChildrenChanged: {
                        processNodeChange(event.getPath());
                        break;
                    }
                    case NodeDeleted: {
                        delete(path);
                        break;
                    }
                    default:
                        break;
                }
            }
        });
    }
    
    private void processNodeChange(final String path) {
        try {
            String value = ZookeeperConstants.NOTHING_VALUE;
            if (!path.equals(getRootNode().getKey())) {
                value = provider.getDataString(path);
            }
            put(path, value);
            // CHECKSTYLE:OFF
        } catch (Exception e) {
            // CHECKSTYLE:ON
            logger.error("PathTree put error : " + e.getMessage());
        }
    }
    
    /**
     * watch data change.
     *
     * @param listener listener
     */
    public void watch(final ZookeeperEventListener listener) {
        if (closed) {
            return;
        }
        final String key = listener.getKey();
        logger.debug("PathTree Watch:{}", key);
        client.registerWatch(rootNode.get().getKey(), listener);
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                logger.debug("PathTree Unregister Watch:{}", key);
                client.unregisterWatch(key);
            }
        }));
    }
    
    public PathStatus getStatus() {
        return status;
    }
    
    public void setStatus(final PathStatus status) {
        this.status = status;
    }
    
    /**
     * get root node.
     *
     * @return root node
     */
    public PathNode getRootNode() {
        return rootNode.get();
    }
    
    /**
     * get node value.
     *
     * @param path path
     * @return node data
     */
    public byte[] getValue(final String path) {
        if (closed) {
            return null;
        }
        PathNode node = get(path);
        return null == node ? null : node.getValue();
    }
    
    private Iterator<String> keyIterator(final String path) {
        List<String> nodes = PathUtil.getShortPathNodes(path);
        logger.debug("keyIterator path{},nodes:{}", path, nodes);
        Iterator<String> iterator = nodes.iterator();
        iterator.next(); // root
        return iterator;
    }
    
    /**
     * get children.
     *
     * @param path path
     * @return children
     */
    public List<String> getChildren(final String path) {
        if (closed) {
            return null;
        }
        PathNode node = get(path);
        List<String> result = new ArrayList<>();
        if (node == null) {
            logger.info("getChildren null");
            return result;
        }
        if (node.getChildren().isEmpty()) {
            logger.info("getChildren no child");
            return result;
        }
        Iterator<PathNode> children = node.getChildren().values().iterator();
        while (children.hasNext()) {
            result.add(new String(children.next().getValue()));
        }
        return result;
    }
    
    private PathNode get(final String path) {
        logger.debug("PathTree get:{}", path);
        PathUtils.validatePath(path);
        if (path.equals(rootNode.get().getKey())) {
            return rootNode.get();
        }
        Iterator<String> iterator = keyIterator(path);
        if (iterator.hasNext()) {
            return rootNode.get().get(iterator); //rootNode.get(1, path);
        }
        logger.debug("{} not exist", path);
        return null;
    }
    
    /**
     * put node.
     *
     * @param path path
     * @param value value
     */
    public void put(final String path, final String value) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        if (closed) {
            return;
        }
        try {
            logger.debug("cache put:{},value:{}", path, value);
            PathUtils.validatePath(path);
            logger.debug("put Status:{}", status);
            if (status == PathStatus.RELEASE) {
                if (path.equals(rootNode.get().getKey())) {
                    rootNode.set(new PathNode(rootNode.get().getKey(), value.getBytes(ZookeeperConstants.UTF_8)));
                    return;
                }
                this.setStatus(PathStatus.CHANGING);
                rootNode.get().set(keyIterator(path), value);
                this.setStatus(PathStatus.RELEASE);
            } else {
                try {
                    logger.debug("put but cache status not release");
                    Thread.sleep(10L);
                } catch (InterruptedException e) {
                    logger.error("put sleep error:{}", e.getMessage(), e);
                }
                put(path, value);
            }
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * delete node.
     *
     * @param path path
     */
    public void delete(final String path) {
        logger.debug("PathTree begin delete:{}", path);
        final ReentrantLock lock = this.lock;
        lock.lock();
        if (closed) {
            return;
        }
        try {
            PathUtils.validatePath(path);
//            String prxpath = path.substring(0, path.lastIndexOf(ZookeeperConstants.PATH_SEPARATOR));
            PathNode node = get(path);
            node.getChildren().remove(path);
            logger.debug("PathTree end delete:{}", path);
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * close.
     */
    public void close() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        this.closed = true;
        try {
            if (executorStart) {
                stopRefresh();
            }
            deleteAllChildren(rootNode.get());
        } catch (Exception ee) {
            logger.warn("PathTree close:{}", ee.getMessage());
        } finally {
            lock.unlock();
        }
    }
    
    private void deleteAllChildren(final PathNode node) {
        if (node.getChildren().isEmpty()) {
            return;
        }
        for (String one : node.getChildren().keySet()) {
            deleteAllChildren(node.getChildren().get(one));
            node.getChildren().remove(one);
        }
    }
}
