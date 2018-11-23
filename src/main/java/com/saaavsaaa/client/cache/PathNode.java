package com.saaavsaaa.client.cache;

import com.saaavsaaa.client.utility.PathUtil;
import com.saaavsaaa.client.utility.constant.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Created by aaa
 */
public class PathNode {
    private final Map<String, PathNode> children = new ConcurrentHashMap<>();

    private final String nodeKey;

    private String path;

    private byte[] value;

    PathNode(final String key) {
        this(key, Constants.RELEASE_VALUE);
    }

    PathNode(final String key, final byte[] value) {
        this.nodeKey = key;
        this.value = value;
        this.path = key;
    }

    void attachChild(final PathNode node) {
        children.put(node.nodeKey, node);
        node.setPath(PathUtil.getRealPath(path, node.getNodeKey()));
    }

    PathNode set(final PathResolve pathResolve, final String value) {
        if (pathResolve.isEnd()) {
            setValue(value.getBytes(Constants.UTF_8));
            return this;
        }
        pathResolve.next();
        if (children.containsKey(pathResolve.getCurrent())) {
            return children.get(pathResolve.getCurrent()).set(pathResolve, value);
        }
        PathNode result = new PathNode(pathResolve.getCurrent(), Constants.NOTHING_DATA);
        this.attachChild(result);
        result.set(pathResolve, value);
        return result;
    }

    PathNode get(final PathResolve pathResolve) {
        pathResolve.next();
        if (children.containsKey(pathResolve.getCurrent())) {
            if (pathResolve.isEnd()) {
                return children.get(pathResolve.getCurrent());
            }
            return children.get(pathResolve.getCurrent()).get(pathResolve);
        }
        return null;
    }

    void delete(final PathResolve pathResolve) {
        pathResolve.next();
        if (children.containsKey(pathResolve.getCurrent())) {
            if (pathResolve.isEnd()) {
                children.remove(pathResolve.getCurrent());
            } else {
                children.get(pathResolve.getCurrent()).delete(pathResolve);
            }
        }
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(final byte[] value) {
        this.value = value;
    }

    String getNodeKey() {
        return nodeKey;
    }

    void setPath(final String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public Map<String, PathNode> getChildren() {
        return children;
    }
}
