/*
 * Copyright (c) 2013 TIBCO Software Inc. All Rights Reserved.
 * 
 * Use is subject to the terms of the TIBCO license terms accompanying the download of this code. 
 * In most instances, the license terms are contained in a file named license.txt.
 */
package com.datasynapse.fabric.container.glassfish;

import java.util.logging.Logger;

import javax.management.ObjectName;

import com.datasynapse.fabric.common.RuntimeContext;
import com.datasynapse.fabric.common.StartCondition;
import com.datasynapse.fabric.container.Container;
import com.datasynapse.fabric.container.ProcessWrapper;
import com.datasynapse.fabric.domain.Domain;
import com.datasynapse.fabric.util.ContainerUtils;

public class GlassfishStartCondition implements StartCondition {

    public void init(Container c, Domain d, ProcessWrapper p, RuntimeContext ctx) {
        container = (GlassfishContainer) c;
    }

    public boolean hasStarted() {
        boolean result = false;
        try {
            String serverMBeanStr = "amx-support:type=boot-amx";
            ObjectName domainRoot = (ObjectName) container.getMBeanServerConnection().invoke(new ObjectName(serverMBeanStr),"bootAMX" ,null, null);
            //wait for AMX to start
            container.getMBeanServerConnection().invoke(domainRoot,"waitAMXReady" ,null, null);
            String domainName =(String) container.getMBeanServerConnection().getAttribute(domainRoot,"AppserverDomainName");
            logger.fine("AMX returned a Domain Name of: "+domainName);
            if (domainName != null) {
                result = true;
            }
            // save the domain root for the running condition
            container.setDomainRoot(domainRoot);
        } catch (Exception e) {
            // Exceptions would be thrown if jmx service is yet NOT available, keep trying...
            logger.finer("While polling to check if " + container.getName() + " has started: " + e);
        }
        return result;
    }

    public long getPollPeriod() {
        return pollPeriod;
    }
    
    public void setPollPeriod(long pollPeriod) {
        this.pollPeriod = pollPeriod;
    }
    
    private GlassfishContainer container;
    private transient Logger logger = ContainerUtils.getLogger(this);
    
    private long pollPeriod = DEFAULT_POLL_PERIOD;
    private static final long DEFAULT_POLL_PERIOD = 5000;
    
}
