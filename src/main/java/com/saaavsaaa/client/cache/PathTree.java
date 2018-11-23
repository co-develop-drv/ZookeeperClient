package com.saaavsaaa.client.cache;

import com.saaavsaaa.client.action.IClient;
import com.saaavsaaa.client.action.IProvider;
import com.saaavsaaa.client.utility.PathUtil;
import com.saaavsaaa.client.utility.Properties;
import com.saaavsaaa.client.utility.StringUtil;
import com.saaavsaaa.client.utility.constant.Constants;
import com.saaavsaaa.client.zookeeper.section.ZookeeperListener;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

/*
 * zookeeper cache tree
 *
 * Created by aaa
 * todo provider
 */
public final class PathTree implements AutoCloseable{
    private static final Logger logger = LoggerFactory.getLogger(PathTree.class);
    private final IClient client;
    private final IProvider provider;
    private final AtomicReference<PathNode> rootNode = new AtomicReference<>();
    private final List<String> watcherKeys = new ArrayList<>();
    private final transient ReentrantLock lock = new ReentrantLock();
    private boolean executorStart;
    private ScheduledExecutorService cacheService;
    private PathStatus status;

    private boolean closed;

    public PathTree(final String root, final IClient client) {
        rootNode.set(new PathNode(root));
        status = PathStatus.RELEASE;
        // TODO consider whether to use a new client alternative to the current
        this.client = client;
        provider = client.getExecStrategy().getProvider();
    }

    /**
     * Load data.
     *
     * @throws KeeperException Zookeeper Exception
     * @throws InterruptedException InterruptedException
     */
    public void load() throws KeeperException, InterruptedException {
        ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        if (closed) {
            return;
        }
        try {
            if (status == PathStatus.RELEASE) {
                setStatus(PathStatus.CHANGING);
                logger.debug("loading Status:{}", status);
                PathNode newRoot = new PathNode(rootNode.get().getNodeKey());
                List<String> children = provider.getChildren(PathUtil.checkPath(rootNode.get().getNodeKey()));
                children.remove(Constants.CHANGING_KEY);
                attachIntoNode(children, newRoot);
                rootNode.set(newRoot);
                setStatus(PathStatus.RELEASE);
                logger.debug("loading release:{}", status);
            } else {
                logger.info("loading but cache status not release");
                try {
                    Thread.sleep(10L);
                } catch (final InterruptedException ex) {
                    logger.error("loading sleep error: {}", ex.getMessage(), ex);
                }
                load();
            }
        } finally {
            lock.unlock();
        }
    }

    private void attachIntoNode(final List<String> children, final PathNode pathNode) throws KeeperException, InterruptedException {
        if (closed) {
            return;
        }
        logger.debug("attachIntoNode children:{}", children);
        if (children.isEmpty()) {
            logger.info("attachIntoNode there are no children");
            return;
        }
        for (String each : children) {
            String childPath = PathUtil.getRealPath(pathNode.getPath(), each);
            PathNode current = new PathNode(each, provider.getData(childPath));
            pathNode.attachChild(current);
            List<String> subs = provider.getChildren(childPath);
            attachIntoNode(subs, current);
        }
    }

    /**
     * Start thread pool period load data.
     *
     * @param period period
     */
    public void refreshPeriodic(final long period) {
        ReentrantLock lock = this.lock;
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
            logger.debug("refreshPeriodic:{}", threadPeriod);
            cacheService = Executors.newSingleThreadScheduledExecutor();
            cacheService.scheduleAtFixedRate(new Runnable() {

                @Override
                public void run() {
                    logger.debug("cacheService run:{}", getStatus());
                    if (PathStatus.RELEASE == status) {
                        try {
                            load();
                        } catch (final KeeperException | InterruptedException ex) {
                            logger.error(ex.getMessage(), ex);
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
     * Stop thread pool period load data.
     */
    public void stopRefresh() {
        cacheService.shutdown();
        executorStart = false;
    }

    /**
     * Watch data change.
     */
    public void watch() {
        watch(new ZookeeperListener(rootNode.get().getNodeKey()) {

            @Override
            public void process(final WatchedEvent event) {
                String path = event.getPath();
                logger.debug("PathTree Watch event:{}", event.toString());
                switch (event.getType()) {
                    case NodeCreated:
                    case NodeDataChanged:
                    case NodeChildrenChanged:
                        processNodeChange(path);
                        break;
                    case NodeDeleted:
                        delete(path);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    /**
     * Watch data change.
     *
     * @param eventListener listener
     */
    public void watch(final ZookeeperListener eventListener) {
        if (closed) {
            return;
        }
        String key = eventListener.getKey();
        logger.debug("PathTree Watch:{}", key);
        client.registerWatch(rootNode.get().getNodeKey(), eventListener);
        watcherKeys.add(key);
    }

    private void processNodeChange(final String path) {
        try {
            String value = provider.getDataString(path);
            put(path, value);
        } catch (final KeeperException | InterruptedException ex) {
            if (ex instanceof KeeperException.NoNodeException || ex instanceof KeeperException.ConnectionLossException) {
                logger.debug(ex.getMessage());
                return;
            }
            logger.error("PathTree put error : " + ex.getMessage());
        }
    }

    /**
     * Get node value.
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

    /**
     * Get children.
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
        for (final PathNode pathNode : node.getChildren().values()) {
            result.add(new String(pathNode.getValue()));
        }
        return result;
    }

    private PathNode get(final String path) {
        logger.debug("PathTree get:{}", path);
        if (StringUtil.isNullOrBlank(path) || path.equals(Constants.PATH_SEPARATOR)) {
            return rootNode.get();
        }
        String realPath = provider.getRealPath(path);
        PathResolve pathResolve = new PathResolve(realPath);
        pathResolve.next();
        if (pathResolve.isEnd()) {
            return rootNode.get();
        }
        return rootNode.get().get(pathResolve);
    }

    /**
     * Put node.
     *
     * @param path path
     * @param value value
     */
    public void put(final String path, final String value) {
        ReentrantLock lock = this.lock;
        lock.lock();
        if (closed) {
            return;
        }
        try {
            if (status == PathStatus.RELEASE) {
                setStatus(PathStatus.CHANGING);
                logger.debug("put Status:{}", status);
                String realPath = provider.getRealPath(path);
                PathResolve pathResolve = new PathResolve(realPath);
                pathResolve.next();
                rootNode.get().set(pathResolve, value);
                setStatus(PathStatus.RELEASE);
                logger.debug("put Status:{}", status);
            } else {
                try {
                    Thread.sleep(10L);
                } catch (final InterruptedException ex) {
                    logger.error("put sleep error:{}", ex.getMessage(), ex);
                }
                put(path, value);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Delete node.
     *
     * @param path path
     */
    public void delete(final String path) {
        ReentrantLock lock = this.lock;
        lock.lock();
        if (closed) {
            return;
        }
        try {
            if (rootNode.get().getChildren().containsKey(path)) {
                rootNode.get().getChildren().remove(path);
                return;
            }
            String realPath = provider.getRealPath(path);
            PathResolve pathResolve = new PathResolve(realPath);
            pathResolve.next();
            rootNode.get().delete(pathResolve);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() {
        ReentrantLock lock = this.lock;
        lock.lock();
        closed = true;
        try {
            if (executorStart) {
                stopRefresh();
            }
            deleteAllChildren(rootNode.get());
            if (!watcherKeys.isEmpty()) {
                for (String each : watcherKeys) {
                    client.unregisterWatch(each);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    private void deleteAllChildren(final PathNode node) {
        if (node.getChildren().isEmpty()) {
            return;
        }
        for (String each : node.getChildren().keySet()) {
            deleteAllChildren(node.getChildren().get(each));
            node.getChildren().remove(each);
        }
    }

    private PathStatus getStatus() {
        return status;
    }

    private void setStatus(final PathStatus status) {
        this.status = status;
    }
}
