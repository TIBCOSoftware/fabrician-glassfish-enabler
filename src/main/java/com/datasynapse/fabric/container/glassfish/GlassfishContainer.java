package com.datasynapse.fabric.container.glassfish;

import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.InitialContext;

import com.datasynapse.commons.util.CalendarUtils;
import com.datasynapse.commons.util.FileUtils;
import com.datasynapse.commons.util.HostUtils;
import com.datasynapse.commons.util.StringUtils;
import com.datasynapse.fabric.common.RuntimeContextVariable;
import com.datasynapse.fabric.container.ExecContainer;
import com.datasynapse.fabric.domain.featureinfo.HttpFeatureInfo;
import com.datasynapse.fabric.util.DomainUtils;
import com.datasynapse.fabric.util.FeatureUtils;
import com.datasynapse.fabric.util.condition.Condition;
import com.datasynapse.fabric.util.condition.ConditionalWait;
import com.sun.net.ssl.HostnameVerifier;
import com.sun.net.ssl.HttpsURLConnection;

public class GlassfishContainer extends ExecContainer
{
  private static final long serialVersionUID = 4555259893922948569L;
 // protected static final String CLUSTER_NAME = "CLUSTER_NAME";
  private static final String GLASSFISH_HOME_VAR = "GLASSFISH_HOME";
  private static final String GLASSFISH_SERVER_BASE_DIR_VAR = "GLASSFISH_SERVER_BASE_DIR";
  private static final String ADMIN_USERID = "ADMIN_USERID";
  private static final String ADMIN_PASSWORD = "ADMIN_PASSWORD";
  private static final String JMX_SERVICE_URL = "JMX_SERVICE_URL";
  private static final String GLASSFISH_SERVER_ROOT_DIR_NAME = "glassfish";
  protected static final String GLASSFISH_SERVER_CONFIG_DIR_NAME_VAR = "GLASSFISH_SERVER_CONFIG_DIR_NAME";
  protected static final String TWO_WAY_SSL_ENABLED_VAR = "TWO_WAY_SSL_ENABLED";
  protected static final String CLIENT_KEY_STORE_FILE_VAR = "CLIENT_KEY_STORE_FILE";
  protected static final String CLIENT_KEY_STORE_PASSWORD_VAR = "CLIENT_KEY_STORE_PASSWORD";
  protected static final String CLIENT_TRUST_STORE_FILE_VAR = "CLIENT_TRUST_STORE_FILE";
  protected static final String CLIENT_TRUST_STORE_PASSWORD_VAR = "CLIENT_TRUST_STORE_PASSWORD";
  protected static final String IGNORE_HOSTNAME_VERIFICATION_VAR = "IGNORE_HOSTNAME_VERIFICATION";
  private static final String ARCHIVE_DEPLOYMENT_TIMEOUT_VAR = "ARCHIVE_DEPLOYMENT_TIMEOUT";
  private static final String VERIFY_ARCHIVE_DEPLOYMENT_SUCCESS = "VERIFY_ARCHIVE_DEPLOYMENT_SUCCESS";
  protected static final String IGNORE_HOSTNAME_VERIFICATION_SYS_PROPERTY = "org.glassfish.security.ignoreHttpsHost";
  private static final String GLASSFISHHA_JAR_VAR = "GLASSFISHHA_JAR";
  private static final String JGROUPS_JAR_VAR = "JGROUPS_JAR";
  private MBeanServerConnection mBeanServer = null;
  private File GlassfishServerRuntimeDir;
  protected HttpFeatureInfo httpFeatureInfo = null;
//  protected GlassfishClusteringInfo GlassfishClusteringInfo = null;
  private boolean ignoreHostnameVerification;
  private HostnameVerifier hostnameVerifierSaved = null;
  private ObjectName domainRoot;
  
  public void setDomainRoot(ObjectName suppliedDomainRoot){
	  domainRoot = suppliedDomainRoot;
  }

  public MBeanServerConnection getMBeanServerConnection() throws Exception
  {
    if (this.mBeanServer == null) {
	    HashMap   environment = new HashMap();
	    String user = getStringVariableValue(ADMIN_USERID,"");
	    String pwd = getStringVariableValue(ADMIN_PASSWORD,"");
	    String jmxurl = getStringVariableValue(JMX_SERVICE_URL,"");
	    String[]  credentials = new String[] {user, pwd};
	    
	    environment.put (JMXConnector.CREDENTIALS, credentials);

      try {
        if (this.ignoreHostnameVerification) {
          HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            public boolean verify(String arg0, String arg1) {
              return true;
            }
          });
        }

		//  Get JMXServiceURL of JMX Connector (must be known in advance)
		    JMXServiceURL  url  = new JMXServiceURL(jmxurl);
		
		//  Get JMX connector
		    JMXConnector  jmxc = JMXConnectorFactory.connect(url, environment);
		
		//  Get MBean server connection
		    this.mBeanServer = jmxc.getMBeanServerConnection();
		   
      } finally {
        if (this.hostnameVerifierSaved != null) {
          HttpsURLConnection.setDefaultHostnameVerifier(this.hostnameVerifierSaved);
        }
      }
    }
    return this.mBeanServer;
  }

  protected void doInit(List<RuntimeContextVariable> additionalVariables) throws Exception {
		getEngineLogger().fine("doInit invoked");
  	super.doInit(additionalVariables);
//		deleteRuntimeDir(".");

    this.httpFeatureInfo = ((HttpFeatureInfo)FeatureUtils.getFeatureInfo("Http Support", this, getCurrentDomain()));

    boolean httpEnabled = (this.httpFeatureInfo != null) && (this.httpFeatureInfo.isHttpEnabled());
    boolean httpsEnabled = (this.httpFeatureInfo != null) && (this.httpFeatureInfo.isHttpsEnabled());

    if ((!httpEnabled) && (!httpsEnabled)) {
      throw new Exception("HTTP or HTTPS must be enabled in the Domain");
    }

    if (!DomainUtils.validateIntegerVariable(this, "HTTP_PORT")) {
      throw new Exception("HTTP_PORT runtime context variable is not set");
    }

    if (!DomainUtils.validateIntegerVariable(this, "HTTPS_PORT")) {
      throw new Exception("HTTPS_PORT runtime context variable is not set");
    }

    String GlassfishServerBaseDir = getStringVariableValue("GLASSFISH_SERVER_BASE_DIR");
    if (StringUtils.isEmptyOrBlank(GlassfishServerBaseDir)) {
      throw new Exception("GLASSFISH_SERVER_BASE_DIR variable must be set in the Container");
    }

    this.ignoreHostnameVerification = Boolean.parseBoolean(getStringVariableValue("IGNORE_HOSTNAME_VERIFICATION", "false"));
    this.hostnameVerifierSaved = HttpsURLConnection.getDefaultHostnameVerifier();
  }
   protected void renameServerConfigDir() throws Exception {
  	// change of plan so this routine isn't being used at the moment
  	// this is kind of complicated so pay attention..!
  	// The default container domain directory is named domain but needs to be named domain<engineID>. After a capture it is named 
  	// domain<oldEngineID> and will need renaming to domain<newEngineID> but the default domain directory will also be copied in(?) and will need to be deleted.
  	
    String domainRoot = getStringVariableValue("DOMAIN_ROOT", "");
    String domainCapture = getStringVariableValue("CAPTURED_DOMAIN_NAME", "notset");
    File domainDir = new File(domainRoot,"domain");
    File domainDirCapture = new File(domainRoot,domainCapture);
    String newDomainDir = getStringVariableValue("DOMAIN_NAME", "domain1");
    File newDir = new File(domainDir.getParentFile(), newDomainDir);
    if (domainDirCapture.exists()) {
    	// 
	  	getEngineLogger().fine("domainDirCapture: "+domainDirCapture.getCanonicalPath()+" exists!");
   	  if (domainDir.exists()){
   	  	// Tidy up default 
		  	getEngineLogger().fine("domainDir: "+domainDir.getCanonicalPath()+" exists after capture!");
	   	  domainDir.delete();
	    }
   	  domainDirCapture.renameTo(newDir);
    } else if (domainDir.exists() && domainCapture.equalsIgnoreCase("notset")){
    	// first deployment i.e. never captured
	  	getEngineLogger().fine("domainDir: "+domainDir.getCanonicalPath()+" exists!");
   	  domainDir.renameTo(newDir);
    } else {
	  	getEngineLogger().fine("domainDir: "+domainDir.getCanonicalPath()+" does not exist!");
    }
  }

  protected void doStart() throws Exception {
    getEngineLogger().fine("doStart invoked");
    super.doStart();
  }

  protected void doShutdown() throws Exception {
    getEngineLogger().fine("doShutdown invoked");
    long shutdownStart = System.currentTimeMillis();
    if ((getProcess() != null) && (getProcess().isRunning())) {
      super.doShutdown();
      waitForShutdown(shutdownStart);
    }

    this.GlassfishServerRuntimeDir = null;
  }

/*  public void cleanup() throws Exception {
    deleteRuntimeDir(".");
  }
*/
  protected void deployArchive(File archive, File deploymentRoot) throws Exception {
      final String fullFilename = archive.getName();
      FileUtils.copyFile(archive, new File(deploymentRoot, fullFilename));
      final String filename = FileUtils.stripExtension(fullFilename);
      
      if (getStringVariableValue(VERIFY_ARCHIVE_DEPLOYMENT_SUCCESS, "true").equalsIgnoreCase("true")) {
          long timeout = CalendarUtils.MINUTE * 2;
          String timeoutStr = getStringVariableValue(ARCHIVE_DEPLOYMENT_TIMEOUT_VAR);
          if (!StringUtils.isEmpty(timeoutStr)) {
              timeout = Long.parseLong(timeoutStr) * 1000;
          }

          ConditionalWait condWait = new ConditionalWait(timeout, 5 * CalendarUtils.SECOND);
          Condition condition = new Condition() {
              public boolean isTrue() throws Exception {
                  getEngineLogger().info("Checking if " + fullFilename + " has successfully been deployed.");
                  boolean result = false;
                  try {
                      String mBeanString = "amx:pp=/domain/applications,type=application,name=" + filename;
                      String serverStarted = (String) getMBeanServerConnection().getAttribute(new ObjectName(mBeanString), "Enabled");
                      if ("true".equals(serverStarted)) {
                          result = true;
                      }
                  } catch (Exception e) {
                      getEngineLogger().fine("While checking for successful deployment of " + fullFilename + ": " + e.toString());
                  }
                  return result;
              }
          };
          boolean result = condWait.waitForCondition(condition);
          if (result) {
              getEngineLogger().info("Archive " + fullFilename + " was successfully deployed");
          } else {
              throw new Exception("Deployment of " + fullFilename + " failed");
          }
      }
  }

  protected boolean shouldCopyConfigFile(File configFile)
  {
    boolean ret = true;

    return ret;
  }

  protected InitialContext createInitialContext() throws Exception {
    ClassLoader clSaved = Thread.currentThread().getContextClassLoader();
    Properties envSaved = new Properties();
    try
    {
      Thread.currentThread().setContextClassLoader(getRuntimeContext().getClassLoader());

      Hashtable jndiProperties = new Hashtable();
      jndiProperties.put("java.naming.factory.initial", "com.sun.enterprise.naming.SerialInitContextFactory");
      jndiProperties.put("java.naming.factory.url.pkgs", "com.sun.enterprise.naming");
      jndiProperties.put("java.naming.factory.state", "com.sun.corba.ee.impl.presentation.rmi.JNDIStateFactoryImpl");      
      jndiProperties.put("org.omg.CORBA.ORBInitialHost", HostUtils.getHostname());

      if ((this.httpFeatureInfo != null) && (this.httpFeatureInfo.isHttpsEnabled())) {
        checkAndSaveProperty(envSaved, "javax.net.ssl.trustStore");
        checkAndSaveProperty(envSaved, "javax.net.ssl.trustStorePassword");
        checkAndSaveProperty(envSaved, "java.protocol.handler.pkgs");
        checkAndSaveProperty(envSaved, "org.glassfish.security.ignoreHttpsHost");

        if (Boolean.valueOf(getStringVariableValue("TWO_WAY_SSL_ENABLED")).booleanValue()) {
          checkAndSaveProperty(envSaved, "javax.net.ssl.keyStore");
          checkAndSaveProperty(envSaved, "javax.net.ssl.keyStorePassword");
          System.setProperty("javax.net.ssl.keyStore", getStringVariableValue("CLIENT_KEY_STORE_FILE", null));
          System.setProperty("javax.net.ssl.keyStorePassword", getStringVariableValue("CLIENT_KEY_STORE_PASSWORD", null));
        }

        System.setProperty("javax.net.ssl.trustStore", getStringVariableValue("CLIENT_TRUST_STORE_FILE", null));
        System.setProperty("javax.net.ssl.trustStorePassword", getStringVariableValue("CLIENT_TRUST_STORE_PASSWORD", null));
        System.setProperty("java.protocol.handler.pkgs", "javax.net.ssl");
        ignoreHostnameVerification = Boolean.valueOf(getStringVariableValue("IGNORE_HOSTNAME_VERIFICATION", "false")).booleanValue();
        System.setProperty("org.glassfish.security.ignoreHttpsHost", String.valueOf(ignoreHostnameVerification));
  			jndiProperties.put("org.omg.CORBA.ORBInitialPort", getSSLPort());;
      } else {
 				jndiProperties.put("org.omg.CORBA.ORBInitialPort", getHTTPPort());;
      }

      return new InitialContext(jndiProperties);
    }
    finally
    {
      if ((this.httpFeatureInfo != null) && (this.httpFeatureInfo.isHttpsEnabled())) {
        revertEnvironment(envSaved);
      }
      Thread.currentThread().setContextClassLoader(clSaved);
    }
  }

  private void deleteRuntimeDir(String pathRelativeToServerDir) throws Exception {
    File dir = pathRelativeToServerDir.equals(".") ? getGlassfishServerRuntimeDir() : new File(getGlassfishServerRuntimeDir(), pathRelativeToServerDir);
    if (dir.exists()) {
      getEngineLogger().info("Deleting directory: " + dir.getAbsolutePath());
      FileUtils.deleteDirectory(dir);
    }
  }

  private File getGlassfishServerRuntimeDir() throws Exception {
    if (this.GlassfishServerRuntimeDir == null) {
      String GlassfishServerBaseDir = getStringVariableValue("GLASSFISH_SERVER_BASE_DIR");
      if (StringUtils.isEmptyOrBlank(GlassfishServerBaseDir)) {
        throw new Exception("GLASSFISH_SERVER_BASE_DIR variable must be set in the Container");
      }
      this.GlassfishServerRuntimeDir = new File(GlassfishServerBaseDir);
    }
    return this.GlassfishServerRuntimeDir;
  }

  public int getPort() throws Exception
  {
    return (this.httpFeatureInfo != null) && (this.httpFeatureInfo.isHttpsEnabled()) ? getSSLPort() : getHTTPPort();
  }

  protected int getHTTPPort() throws Exception {
    return Integer.parseInt(getStringVariableValue("HTTP_PORT", null));
  }

  protected int getSSLPort() throws Exception {
    return Integer.parseInt(getStringVariableValue("HTTPS_PORT", null));
  }

  protected void checkAndSaveProperty(Properties toBeSavedProps, String name)
  {
    String value = System.getProperty(name);
    if (!StringUtils.isEmpty(value))
      toBeSavedProps.put(name, value);
  }

  protected void revertEnvironment(Properties envSaved)
  {
    for (Iterator itr = envSaved.keySet().iterator(); itr.hasNext(); ) {
      String key = (String)itr.next();
      String val = envSaved.getProperty(key);
      if (val != null)
        System.setProperty(key, val);
    }
  }
  public String getPkgs() {
  	return "extra-system-packages=${jre-1.6} ${internal-jdk-pkgs-for-gf}";
  }
  public String getDtrace(){
  	return "jre-1.7=${jre-1.6},com.sun.tracing";
  }
  protected boolean isRunning() {
      boolean running = false;
      try {
          String domainName =(String) this.getMBeanServerConnection().getAttribute(domainRoot,"AppserverDomainName");
          if (domainName != null) {
        	  running = true;
          }
      } catch (Exception e) {
          getEngineLogger().severe("While checking if " + this.domainRoot.getDomain() + " is running: " + e);
      }
      return running;
  }

}