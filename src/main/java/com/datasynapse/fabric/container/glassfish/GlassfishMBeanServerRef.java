/*
 * Copyright (c) 2013 TIBCO Software Inc. All Rights Reserved.
 * 
 * Use is subject to the terms of the TIBCO license terms accompanying the download of this code. 
 * In most instances, the license terms are contained in a file named license.txt.
 */
package com.datasynapse.fabric.container.glassfish;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;

import com.datasynapse.fabric.common.RuntimeContext;
import com.datasynapse.fabric.container.Container;
import com.datasynapse.fabric.container.ProcessWrapper;
import com.datasynapse.fabric.domain.Domain;
import com.datasynapse.fabric.stats.MBeanServerRef;
import com.datasynapse.fabric.util.ContainerUtils;

public class GlassfishMBeanServerRef implements MBeanServerRef {

    private MBeanServerConnection mBeanServerConnection;
    private GlassfishContainer container;
    
    private transient Logger logger = ContainerUtils.getLogger(this);
    
    public void init(Container container, Domain domain, ProcessWrapper process, RuntimeContext runtimeContext) {
        try {
            this.container = (GlassfishContainer) container;
            this.mBeanServerConnection = ((GlassfishContainer) container).getMBeanServerConnection();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to initialize the GlassfishMBeanServerRef", e);
        }
    }

    public Object getAttribute(String bean, String attr) throws Exception {
        if (mBeanServerConnection != null) {
         // Break the CompositeData value key name from the end of provided attribute string
            String[] attrArray = attr.split("\\.");
            if (attrArray.length != 2){
                throw new Exception("Attribute " + attr + " in " + container.getName() + " is not a two part value separated with a \".\" ");
            }
            CompositeDataSupport valueCDS = null;
            try {                
                valueCDS = (CompositeDataSupport) mBeanServerConnection.getAttribute(new ObjectName(bean), attrArray[0]);
            } catch(javax.management.InstanceNotFoundException infe){
                // not necessarily an error, AMX beans are dynamically created so if, for example, no web 
                // apps are deployed there may not be a web-request-mon bean or an http-listener-1 etc.
                // So log it and return 0.0
                logger.log(Level.WARNING, "InstanceNotFoundException looking up " + attr + " in " + bean);
                return 0.0;
            } catch (java.io.IOException ioe){
                // If the appserver is shutdown from the console this will result in an IOException
                // So log it and return 0.0 while we wait for the running condition to notice the server is gone.
                // This just avoids excessive confusion in the logs caused by thousands of lines of stack traces while
                // getting statistics.
                logger.log(Level.WARNING, "InstanceNotFoundException looking up " + attr + " in " + bean);
                return 0.0;
            } 
            return valueCDS.get(attrArray[1].trim());
        } else {
            throw new IllegalStateException("Glassfish MBeanServerRef has not been initialized");
        }
    }

}
