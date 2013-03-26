/*
 * Created on Aug 24, 2007
 *
 * Copyright DataSynapse, Inc. 2000 - 2007
 * All rights reserved.
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
