/*
 * Created on Dec 4, 2006
 *
 * Copyright DataSynapse, Inc. 2000 - 2007
 * All rights reserved.
 */
package com.datasynapse.fabric.container.glassfish;

import com.datasynapse.commons.beans.AbstractBean;
import com.datasynapse.fabric.domain.Domain;
import com.datasynapse.fabric.domain.featureinfo.FeatureInfo;

/**
 * This <code>FeatureInfo</code> implementation indicates that the Domain supports
 * Glassfish-style clustering.  The only required parameter is the <code>clusterName</code>.
 */
public class GlassfishClusteringInfo extends AbstractBean implements FeatureInfo {

    public static final String CLUSTER_NAME_PROPERTY = "clusterName";
    
    private static final long serialVersionUID = -4204564813621650432L;
    
    private String clusterName;

    
    public void init(Domain domain) {}
    
    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        String old = this.clusterName;
        this.clusterName = clusterName;
        propertyChange(CLUSTER_NAME_PROPERTY, old, clusterName);
    }
}
