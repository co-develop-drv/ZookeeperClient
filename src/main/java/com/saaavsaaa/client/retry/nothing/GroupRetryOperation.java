package com.saaavsaaa.client.retry.nothing;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by aaa
 */
public class GroupRetryOperation extends RetryOperation {
    private final List<RetryOperation> operationList = new LinkedList<>();
    
    public void addOperation(final RetryOperation operation) {
        operationList.add(operation);
    }
    
    @Override
    protected void execute() {
        
    }
    
    @Override
    public boolean executeOperation() {
        if (operationList.isEmpty()) {
            return true;
        }
        Iterator<RetryOperation> iterator = operationList.iterator();
        while (iterator.hasNext()) {
            RetryOperation current = iterator.next();
            if (current.executeOperation()) {
                operationList.remove(current);
            } else {
                return false;
            }
        }
        return true;
    }
}
