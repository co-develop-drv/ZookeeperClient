package com.saaavsaaa.client.cache;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PathResolveTest {
    
    @Test
    public void assertNext() {
        final String path = "/test/a/bb/ccc/ddd";
        PathResolve pathResolve = new PathResolve(path);
        pathResolve.next();
        assertThat(pathResolve.getCurrent(), is("test"));
        pathResolve.next();
        assertThat(pathResolve.getCurrent(), is("a"));
        pathResolve.next();
        assertThat(pathResolve.getCurrent(), is("bb"));
        pathResolve.next();
        assertThat(pathResolve.getCurrent(), is("ccc"));
        pathResolve.next();
        assertThat(pathResolve.getCurrent(), is("ddd"));
    }
    
    @Test
    public void assertTrickEnd() {
        final String path = "/test/";
        PathResolve pathResolve = new PathResolve(path);
        pathResolve.next();
        assertThat(pathResolve.isEnd(), is(true));
    }
    
    @Test
    public void assertEnd() {
        final String path = "/test/a/bb";
        PathResolve pathResolve = new PathResolve(path);
        pathResolve.next();
        pathResolve.next();
        pathResolve.next();
        assertThat(pathResolve.isEnd(), is(true));
    }
}
