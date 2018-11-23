package com.saaavsaaa.client.cache;


/**
 * Path hierarchy resolve.
 *
 * @author lidongbo
 */
final class PathResolve {

    private final String path;

    private String current;
    
    private int position;
    
    private boolean ended;
    
    private long length = -1;

    public PathResolve(final String path) {
        this.path = path;
    }

    /**
     * Read position Whether the end position or not .
     *
     * @return isEnd boolean
     */
    public boolean isEnd() {
        return ended;
    }
    
    private void checkEnd() {
        if (length == -1) {
            if (path.charAt(path.length() - 1) == '/') {
                length = path.length() - 1;
            } else {
                length = path.length();
            }
        }
        
        ended = position >= length;
    }
    
    /**
    * Next path node.
    */
    public void next() {
        if (isEnd()) {
            return;
        }
        int nodeBegin = ++position;
        while (!isEnd() && path.charAt(position) != '/') {
            position++;
            checkEnd();
        }
        current = path.substring(nodeBegin, position);
    }

    public String getCurrent() {
        return current;
    }
}
