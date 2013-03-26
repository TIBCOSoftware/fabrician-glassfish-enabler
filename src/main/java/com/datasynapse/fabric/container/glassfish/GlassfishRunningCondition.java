/*
 * Created on Jul 9, 2009
 *
 * Copyright DataSynapse, Inc. 2000 - 2009
 * All rights reserved.
 */
package com.datasynapse.fabric.container.glassfish;

import com.datasynapse.commons.util.CalendarUtils;
import com.datasynapse.fabric.common.RunningCondition;
import com.datasynapse.fabric.common.RuntimeContext;
import com.datasynapse.fabric.container.Container;
import com.datasynapse.fabric.container.ProcessWrapper;
import com.datasynapse.fabric.domain.Domain;

public class GlassfishRunningCondition implements RunningCondition {

    private GlassfishContainer container;
    private String errorMessage;
    private long pollPeriod = CalendarUtils.SECOND * 10;
    
    private static final String CRASHED_MSG = "Glassfish is not running: ";
    
    public void init(Container container, Domain domain, ProcessWrapper process, RuntimeContext runtimeContext) {
        this.container = (GlassfishContainer) container;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setPollPeriod(long pollPeriod) {
        this.pollPeriod = pollPeriod;
    }

    public long getPollPeriod() {
        return pollPeriod;
    }

    public boolean isRunning() {
        boolean running = false;
        try {
            running = container.isRunning();
            if (!running) {
                errorMessage = CRASHED_MSG;
            }
        } catch (Exception e) {
            errorMessage = CRASHED_MSG + ", cause: " + e.toString();
        }
        return running;
    }

}
