// Copyright 2013 TIBCO Software Inc. All rights reserved.
package com.datasynapse.fabric.container.glassfish.condition.util;

import com.datasynapse.fabric.common.RunningCondition;
import com.datasynapse.fabric.common.StartCondition;
import com.datasynapse.fabric.container.ProcessWrapper;

public class ConditionalWait {

    private static final long DEFAULT_POLL_PERIOD = 5000;
    
    private long pollPeriod = DEFAULT_POLL_PERIOD;
    private long timeout = 60000;
    
    private boolean fail = false;
    private boolean abort = false;
    private Object lock;
    
    /** timeout < 0 means never timeout;
     *  pollPeriod == 0 means use default poll period  
     */
    public ConditionalWait(long timeout, long pollPeriod) {
        this.timeout = timeout;
        // if specified poll period is 0, just use the default
        if (pollPeriod > 0) {
            this.pollPeriod = pollPeriod;
        }
        lock = new Object();
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public long getPollPeriod() {
        return pollPeriod;
    }

    public void setPollPeriod(long pollPeriod) {
        this.pollPeriod = pollPeriod;
    }

    public boolean waitForCondition(final RunningCondition runningCondition) throws Exception {
        return waitForCondition(new Condition() {
            public boolean isTrue() throws Exception {
                return !runningCondition.isRunning();
            }
        });
    }

    public boolean waitForCondition(StartCondition startCondition) throws Exception {
        return waitForCondition(startCondition, null);
    }
    
    public boolean waitForCondition(final StartCondition startCondition, final ProcessWrapper process) throws Exception {
        return waitForCondition(new Condition() {
            public boolean isTrue() throws Exception {
                if (process != null && !process.isRunning()) {
                    throw new Exception("Blocking process exited during startup");
                }
                return startCondition.hasStarted();
            }
        });
    }
    
    public boolean waitForCondition(Condition condition) throws Exception {
        boolean result = false;
        long timeout = getTimeout();
        long startTime = System.currentTimeMillis();

        while (!abort && (timeout < 0 || (System.currentTimeMillis() - startTime) <= timeout)) {
            synchronized (lock) {
                try {
                    lock.wait(getPollPeriod());
                } catch (InterruptedException e) {
                    break;
                }
            }

            // don't bother calling isTrue() if abort() was called while we were waiting
            if (!abort && (condition.isTrue())) {
                result = true;
                break;
            }
        }
        return (fail ? false : result);
    }
    
    public void fail() {
        synchronized (lock) {
            this.abort = true;
            this.fail = true;
            lock.notify();
        }
    }
     
    public void abort() {
        synchronized (lock) {
            this.abort = true;
            lock.notify();
        }
    }
}
