[fabrician.org](http://fabrician.org/)
==========================================================================
Glassfish J2EE Server Enabler Guide
==========================================================================

Introduction
--------------------------------------
A Silver Fabric Enabler allows an external application or application platform, such as a 
J2EE application server to run in a TIBCO Silver Fabric software environment. The Glassfish J2EE 
Server Enabler for Silver Fabric provides integration between Silver Fabric and Glassfish. 
The Enabler automatically provisions, orchestrates, controls and manages a Glassfish environment. 

Supported Platforms
--------------------------------------
* Silver Fabric 5
* Windows, Linux

Installation
--------------------------------------
The Glassfish Server Enabler consists of an Enabler Runtime Grid Library and a Distribution Grid 
Library. The Enabler Runtime contains information specific to a Silver Fabric version that is 
used to integrate the Enabler, and the Distribution contains the application server or program 
used for the Enabler. Installation of the Apache HTTP Server Enabler involves copying these Grid 
Libraries to the SF_HOME/webapps/livecluster/deploy/resources/gridlib directory on the Silver Fabric Broker. 

Runtime Grid Library
--------------------------------------
The Enabler Runtime Grid Library is created by building the maven project:
```bash
mvn package
```
The version of the distribution can be optionally overridden:
```bash
mvn package -Ddistribution.version=3.1.2.2
```

Distribution Grid Library
--------------------------------------
The Distribution Grid Library is created by performing the following steps:
* Download the glassfish-3.1.2.2.zip from https://glassfish.java.net/downloads/3.1.2.2-final.html
* Build the maven project with the location of the archive, operating system target and optionally the version.  Operating system is typically 'all', 'win32,win64' or 'linux,linux64'.

```bash
mvn package -Ddistribution.location=/home/you/Downloads/glassfish-3.1.2.2.zip -Ddistribution.version=3.1.2.2 -Ddistribution.os=all
```
Statistics
--------------------------------------
* **GLASSFISH HTTP Thread Pool Count** - The number of threads in the http pool. 
* **GLASSFISH HTTPS Thread Pool Count** - The number of threads in the https pool. 
* **GLASSFISH Server JVM Thread Count** - The number of threads Server JVM is using. 
* **GLASSFISH Thread Count** - The number of active threads.. 
* **GLASSFISH Peak Thread Count** - The peak number of active threads. 
* **GLASSFISH Throughput** - The number of HTTP requests processed per second. 
* **GLASSFISH HTTPS Throughput** - The number of HTTPS requests processed per second.
* **GLASSFISH HTTP queued requests** - The number of queued requests.
* **GLASSFISH HTTPS queued requests** - The number of queued requests. 
* **GLASSFISH Web Requests** - The number of web requests.
* **GLASSFISH Active Web Sessions** - The number of active sessions. 
* **GLASSFISH HTTP Cache Size** - The size of the cache.
* **GLASSFISH HTTP Cache Hit Frequency** - The number of cache hits per second.
* **GLASSFISH HTTPS Cache Size** - The size of the cache.
* **GLASSFISH HTTPS Cache Hit Frequency** - The number of cache hits per second. 
* **GLASSFISH Used Heap Memory** - Used heap memory.
* **GLASSFISH Used NonHeap Memory** - Used heap memory.
* **GLASSFISH Max Memory** - Maximum heap memory.

Runtime Context Variables
--------------------------------------
* **CAPTURE_INCLUDES** - Directories to capture.  
    * Type: String
    * Default value: glassfish/domains/domain/config/.*,glassfish/domains/domain/applications/.*/.*,glassfish/domains/domain/generated/.*/.*,glassfish/domains/domain/docroot/.*/.*
* **CAPTURE_EXCLUDES** - Web Server listening port.  
    * Type: String
    * Default value: glassfish/domains/domain/osgi-cache/.* 
* **JDK_NAME** - The name of the required JDK.
    * Type: String
    * Default value: j2sdk
* **JDK_VERSION** - The version of the required JDK. 
    * Type: String
    * Default value: 1.7
* **DELETE_RUNTIME_DIR_ON_SHUTDOWN** - Whether to delete the runtime directory on shutdown.  
    * Type: Environment
    * Default value: true
* **GLASSFISH_HOME_URI** - The GLASSFISH home directory.  
    * Type: Environment
    * Default value: file:/${DynamicVarsUtils.toCanonicalJavaPath("GLASSFISH_SERVER_BASE_DIR")}
* **GLASSFISH_SERVER_BASE_DIR** - The GLASSFISH server base directory.  
    * Type: String
    * Default value: ${ENGINE_WORK_DIR}/glassfishv3_Domain
* **FELIX_HOME** - The Felix container base directory.  
    * Type: Environment
    * Default value: ${GLASSFISH_SERVER_BASE_DIR}/glassfish/osgi/felix
* **GLASSFISH_SERVER_BASE_URL** - The GLASSFISH server base URL.  
    * Type: Environment
    * Default value: file:${GLASSFISH_SERVER_BASE_DIR}
* **J2EE_ARCHIVE_DEPLOY_DIRECTORY** - The GLASSFISH archive deployment directory.  
    * Type: String
    * Default value: ${DOMAIN_HOME}/autodeploy
* **JAVA_MEMORY_OPTS** - Additional Java process memory options.  
    * Type: Environment
    * Default value: -Xmx512m
* **GLASSFISH_SERVER_CONFIG_DIR_NAME** - The GLASSFISH configuration directory name.  
    * Type: String
    * Default value: glassfishv3_Domain
* **SERVER_ID** - The unique name assigned to a GLASSFISH server instance for lookup purposes.  
    * Type: Environment
    * Default value: server
* **CONFIG_MODEL_NAME** - The unique name assigned to a GLASSFISH server instance for lookup purposes.  
    * Type: Environment
    * Default value: server-config
* **ARCHIVE_DEPLOYMENT_TIMEOUT** - The amount of time in seconds to allow for each archive deployment to finish in standalone mode.  Only valid if VERIFY_ARCHIVE_DEPLOYMENT_SUCCESS is true..  
    * Type: String
    * Default value: 120
* **VERIFY_ARCHIVE_DEPLOYMENT_SUCCESS** - Whether or not to verify successful deployment of J2EE archives.  This must be set to true for archive ordering..  
    * Type: String
    * Default value: true
* **IGNORE_HOSTNAME_VERIFICATION** - Ignore hostname verification when connecting to the GLASSFISH MBean server.  
    * Type: Environment
    * Default value: true
* **CLIENT_AUTH_ENABLED** - SSL/TLS Connector port configuration.  
    * Type: Environment
    * Default value: true
* **SERVER_KEY_STORE_FILE_NAME** - Server key store file name for incoming SSL connections.  
    * Type: String
    * Default value: keystore.jks
* **SERVER_KEY_STORE_FILE** - Server key store file location for incoming SSL connections.  
    * Type: String
    * Default value: ${DOMAIN_HOME}/config/${SERVER_KEY_STORE_FILE_NAME}
* **SERVER_KEY_STORE_PASSWORD** - Password for the server key store.  
    * Type: String
    * Default value: changeit
* **SERVER_TRUST_STORE_FILE_NAME** - Server trust store file name  for outgoing SSL connections.  
    * Type: String
    * Default value: cacerts.jks
* **SERVER_TRUST_STORE_FILE** - Server trust store file location for outgoing SSL connections.  
    * Type: String
    * Default value: ${DOMAIN_HOME}/config/${SERVER_TRUST_STORE_FILE_NAME}
* **SERVER_TRUST_STORE_PASSWORD** - Password for the server trust store.  
    * Type: String
    * Default value: changeit
* **DOMAIN_ROOT** - Location of the Domain configuration.  
    * Type: String
    * Default value: ${GLASSFISH_SERVER_BASE_DIR}/glassfish/domains
* **DOMAIN_HOME** - Location of the Domain configuration.  
    * Type: String
    * Default value: ${DOMAIN_ROOT}/domain
* **CLIENT_KEY_STORE_FILE** - Client key store file for connecting to the GLASSFISH MBean server when TWO_WAY_SSL_ENABLED is true.  
    * Type: String
    * Default value: ${DOMAIN_HOME}/config/keystore.jks
* **CLIENT_KEY_STORE_PASSWORD** - Client trust store password used when connecting to the GLASSFISH MBean server.  
    * Type: String
    * Default value: changeit
* **ORB_LISTENER_PORT** - ORB listen port.  
    * Type: Environment
    * Default value: 3700
* **ORB_SSL_PORT** - ORB SSL listen port.  
    * Type: Environment
    * Default value: 3820
* **ORB_MUTUALAUTH_PORT** - ORB MutualAuth listen port.  
    * Type: Environment
    * Default value: 3920
* **JMX_SYSTEM_CONNECTOR_PORT** - Jmx System Connector Port.  
    * Type: Environment
    * Default value: 8686
* **JMX_SERVICE_URL** - Jmx Service URL.  
    * Type: Environment
    * Default value: service:jmx:rmi://${HostUtils.getFQHostname()}:${JMX_SYSTEM_CONNECTOR_PORT}/jndi/rmi://${HostUtils.getFQHostname()}:${JMX_SYSTEM_CONNECTOR_PORT}/jmxrmi
* **JMS_PROVIDER_PORT** - Jms Provider Port.  
    * Type: Environment
    * Default value: 6767
* **ADMIN_USERID** - Admin Userid.  
    * Type: String
    * Default value: admin
* **ADMIN_PASSWORD** - Admin Password.  
    * Type: String
    * Default value: adminadmin
* **OSGI_SHELL_TELNET_PORT** - OSGI Shell Telnet Port.  
    * Type: Environment
    * Default value: 6666
* **HTTP_PORT** - HTTP listen port.  
    * Type: Environment
    * Default value: 9090
* **HTTPS_PORT** - HTTPS listen port.  
    * Type: Environment
    * Default value: 9190
* **ADMIN_PORT** - Admin port.  
    * Type: Environment
    * Default value: 4848
* **DOMAIN_NAME** - The unique name assigned to a GLASSFISH server domain for lookup purposes.  
    * Type: Environment
    * Default value: domain
