package com.saaavsaaa.client.retry.nothing;

import com.saaavsaaa.client.zookeeper.operation.BaseOperation;

/**
 * Created by aaa
 */
public abstract class RetryOperation extends BaseOperation {
    protected RetryOperation() {
        super(null);
    }
    
    @Override
    protected abstract void execute();
    
    @Override
    public boolean executeOperation() {
        try {
            execute();
            return true;
        } catch (Exception ee) {
            System.out.println(ee.getMessage());
            return false;
        }
    }
}
