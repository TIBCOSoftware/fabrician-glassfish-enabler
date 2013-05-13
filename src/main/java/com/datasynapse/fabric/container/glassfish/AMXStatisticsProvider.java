/*
 * Copyright (c) 2013 TIBCO Software Inc. All Rights Reserved.
 * 
 * Use is subject to the terms of the TIBCO license terms accompanying the download of this code. 
 * In most instances, the license terms are contained in a file named license.txt.
 */
package com.datasynapse.fabric.container.glassfish;

import java.util.logging.Level;

import com.datasynapse.commons.util.StringUtils;
import com.datasynapse.fabric.common.RuntimeContext;
import com.datasynapse.fabric.common.plugins.FabricLogRepository;
import com.datasynapse.fabric.container.Container;
import com.datasynapse.fabric.container.ProcessWrapper;
import com.datasynapse.fabric.domain.Domain;
import com.datasynapse.fabric.stats.DefaultStatistic;
import com.datasynapse.fabric.stats.MBeanServerRef;
import com.datasynapse.fabric.stats.MBeanStatisticsMetadata;
import com.datasynapse.fabric.stats.Statistic;
import com.datasynapse.fabric.stats.StatisticsMetadata;
import javax.management.openmbean.CompositeDataSupport;
import com.datasynapse.fabric.stats.provider.AbstractStatisticsProvider;

/**
 * <code>MBeanStatisticsProvider</code> is an implementation of the
 * <code>StatisticsProvider</code> interface that takes advantage of the JMX
 * protocol.
 * <p>
 * Since JMX exposes an API for accessing MBeans and their properties, the
 * statistics collection can be generalized. The provider looks up the MBean
 * using the object name specified in the statistic metadata and then queries
 * the value of the attribute based on the attribute name of the statistic.
 * <p>
 * Implementations wishing to utilize this simplified JMX stat retrieval
 * mechanism need only provide an implementation of the
 * {@link com.datasynapse.fabric.stats.MBeanServerRef} interface to provide access to the MBean
 * server.
 * <p>
 * For example, the XML definition of the MBeanStatisticsProvider used by JBossContainer is shown below:
 * <br>
 * <pre>
 * &lt;statisticsProvider class="com.datasynapse.fabric.stats.provider.MBeanStatisticsProvider"&gt;
 *    &lt;mBeanServerRef class="JBossMBeanServerRef"/&gt;
 *    &lt;supportedStatistic class="com.datasynapse.fabric.stats.MBeanStatisticsMetadata"&gt; 
 *       &lt;property name="name" value="JBoss Thread Count"/&gt;
 *       &lt;property name="description" value="The number of busy threads."/&gt;
 *       &lt;property name="objectName" value="jboss.web:name=http-0.0.0.0-${container.getPort()},type=ThreadPool"/&gt;
 *       &lt;property name="attributeName" value="currentThreadsBusy"/&gt; 
 *       &lt;property name="default" value="true"/&gt; 
 *       &lt;property name="units" value="threads"/&gt;
 *       &lt;aggregator class="SourceAveragedAggregator"/&gt;
 *    &lt;/supportedStatistic&gt;
 *    ...
 * &lt;/statisticsProvider&gt;
 * </pre>
 * 
 * @see com.datasynapse.fabric.stats.MBeanStatisticsMetadata
 * @see com.datasynapse.fabric.stats.MBeanServerRef
 */
public class AMXStatisticsProvider extends AbstractStatisticsProvider {

    private MBeanServerRef mBeanServerRef;

    public AMXStatisticsProvider() {}

    public void init(Container container, Domain domain, ProcessWrapper process, RuntimeContext runtimeContext) {
        setContainer(container);
        
        if (mBeanServerRef != null) {
            ClassLoader clSaved = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(getContainer().getRuntimeContextTemplate().getClassLoader());
                mBeanServerRef.init(container, domain, process, runtimeContext);
            } finally {
                Thread.currentThread().setContextClassLoader(clSaved);
            }
        }
    }

    public Statistic getStatistic(StatisticsMetadata stat) {
        if (stat instanceof MBeanStatisticsMetadata) {
            return getMBeanStatistic((MBeanStatisticsMetadata) stat);
        }
        return null;
    }

    private DefaultStatistic getMBeanStatistic(MBeanStatisticsMetadata stat) {
        DefaultStatistic s = null;
        // HITESH, 1.1 Update 1: Bug#885
        // We need to wrap getValue() method with the gridlib context
        // classloader.
        ClassLoader clSaved = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getContainer().getRuntimeContextTemplate().getClassLoader());
            double value = getValue((MBeanStatisticsMetadata) stat);
            s = new DefaultStatistic(stat.getName(), value);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "while getting statistic " + stat + " in " + getContainer().getCurrentDomain().getName(), e);
        } finally {
            Thread.currentThread().setContextClassLoader(clSaved);
        }
        return s;
    }

    private double getValue(MBeanStatisticsMetadata stat) throws Exception {
        String attr = stat.getAttributeName();
        if (StringUtils.isEmpty(attr)) {
            attr = stat.getName();
        }
        // Allow variables in stats path due to dynamic nature of JMX/Clustering
        String statsName = getContainer().resolveVariables(stat.getObjectName());
        // Break the CompositeData value key name from the end of provided attribute string
        String[] attrArray = attr.split("\\.");
        if (attrArray.length != 2){
            throw new Exception("Attribute " + attr + " in " + getContainer().getName() + " is not a two part value separated with a \".\" ");
        }
        FabricLogRepository.STATISTICS.fine("Looking up " + stat.getAttributeName() + " in " + statsName);
        
        CompositeDataSupport valueCDS = null;
        try{
            valueCDS = (CompositeDataSupport) getMBeanServerRef().getAttribute(statsName, attrArray[0]);
        } catch(javax.management.InstanceNotFoundException infe){
            // not necessarily an error, AMX beans are dynamically created so if, for example, no web 
            // apps are deployed there may not be a web-request-mon bean or an http-listener-1 etc.
            // So log it and return 0.0
            FabricLogRepository.STATISTICS.info("InstanceNotFoundException looking up " + stat.getAttributeName() + " in " + statsName);
            return 0.0;
        } catch (java.io.IOException ioe){
            // If the appserver is shutdown from the console this will result in an IOException
            // So log it and return 0.0 while we wait for the running condition to notice the server is gone.
            // This just avoids excessive confusion in the logs caused by thousands of lines of stack traces while
            // getting statistics.
            FabricLogRepository.STATISTICS.info("InstanceNotFoundException looking up " + stat.getAttributeName() + " in " + statsName);
            return 0.0;
        }
        Object value = valueCDS.get(attrArray[1].trim());
        if (!(value instanceof Number)) {
            throw new Exception("Statistic " + stat + " in " + getContainer().getName() + " is not a number ");
        }
        return (((Number) value).doubleValue());
    }

    /**
     * Retrieve the <code>MBeanServerRef</code> used to communicate with the
     * MBean server
     * 
     * @return the <code>MBeanServerRef</code>
     */
    public MBeanServerRef getMBeanServerRef() {
        return mBeanServerRef;
    }

    /**
     * Set the <code>MBeanServerRef</code> used to communicate with the MBean
     * server
     * 
     * @param mBeanServerRef
     *            the <code>MBeanServerRef</code>
     */
    public void setMBeanServerRef(MBeanServerRef mBeanServerRef) {
        this.mBeanServerRef = mBeanServerRef;
    }

}
