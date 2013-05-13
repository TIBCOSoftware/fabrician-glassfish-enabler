/*
 * Copyright (c) 2013 TIBCO Software Inc. All Rights Reserved.
 * 
 * Use is subject to the terms of the TIBCO license terms accompanying the download of this code. 
 * In most instances, the license terms are contained in a file named license.txt.
 */
package com.datasynapse.fabric.container.glassfish;

import com.datasynapse.fabric.domain.featureinfo.ApplicationLoggingInfo;

public class GlassfishApplicationLoggingInfo extends ApplicationLoggingInfo {

    public static final String[] DEFAULT_PATTERNS = { "${DOMAIN_HOME}/logs/.*\\.log" };

    private static final long serialVersionUID = 601005930088469292L;
    
    protected String[] getDefaultPatterns() {
        return DEFAULT_PATTERNS;
    }

}
