/*
 * Copyright (c) 2013 TIBCO Software Inc. All Rights Reserved.
 * 
 * Use is subject to the terms of the TIBCO license terms accompanying the download of this code. 
 * In most instances, the license terms are contained in a file named license.txt.
 */
package com.datasynapse.fabric.container.glassfish;

import java.util.logging.Level;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import com.datasynapse.commons.util.LogUtils;
import com.datasynapse.fabric.common.RuntimeContext;
import com.datasynapse.fabric.container.Container;
import com.datasynapse.fabric.container.ProcessWrapper;
import com.datasynapse.fabric.domain.Domain;
import com.datasynapse.fabric.stats.MBeanServerRef;

public class GlassfishMBeanServerRef implements MBeanServerRef {

    private MBeanServerConnection mBeanServerConnection;
    
    public void init(Container container, Domain domain, ProcessWrapper process, RuntimeContext runtimeContext) {
        try {
            this.mBeanServerConnection = ((GlassfishContainer) container).getMBeanServerConnection();
        } catch (Exception e) {
            LogUtils.forObject(this).log(Level.SEVERE, "Failed to initialize the GlassfishMBeanServerRef", e);
        }
    }

    public Object getAttribute(String bean, String attr) throws Exception {
        if (mBeanServerConnection != null) {
            return mBeanServerConnection.getAttribute(new ObjectName(bean), attr);
        } else {
            throw new IllegalStateException("MBeanServerRef has not been initialized");
        }
    }

}