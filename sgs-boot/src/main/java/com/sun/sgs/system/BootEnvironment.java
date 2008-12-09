/*
 * Copyright 2007-2008 Sun Microsystems, Inc.
 *
 * This file is part of Project Darkstar Server.
 *
 * Project Darkstar Server is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation and
 * distributed hereunder to you.
 *
 * Project Darkstar Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.sun.sgs.system;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URISyntaxException;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Defines environment variables used by the bootstrapper
 * to locate and configure the necessary environment pieces
 * to launch a Project Darkstar Server.
 * <p>
 * This class also provides utility methods to load configuration properties
 * from a config file into a {@link SubstitutionProperties} object.
 */
final class BootEnvironment {
    private static final Logger logger = Logger.getLogger(
            BootEnvironment.class.getName());
    
    /**
     * This class should not be instantiated.
     */
    private BootEnvironment() {
        
    }
    
    /**
     * Default location of the bootstrapper jar relative to {@code SGS_HOME}.
     */
    static final String SGS_JAR = 
            "bin" + File.separator + "sgs-boot.jar";
    
    /**
     * Name of the properties file to locate and retrieve properties
     * for the environment.
     */
    static final String SGS_BOOT = "sgs-boot.properties";

    /**
     * Denotes the installation directory for the Project Darkstar server.
     */
    static final String SGS_HOME = "SGS_HOME";
    
    /**
     * The directory where deployed applications should place jar files
     * and application properties files.
     */
    static final String SGS_DEPLOY = "SGS_DEPLOY";
    
    /**
     * The properties file used to configure the Project Darkstar kernel.
     * This file should be fed to the Project Darkstar Kernel.
     */
    static final String SGS_PROPERTIES = "SGS_PROPERTIES";
    
    /**
     * The logging properties file for the Project Darkstar server.
     */
    static final String SGS_LOGGING = "SGS_LOGGING";
    
    /**
     * The name of the log file to send output to.
     */
    static final String SGS_LOGFILE = "SGS_LOGFILE";
    
    /**
     * A toggle used to specify which flavor of Berkeley DB is to be
     * used by the application.
     */
    static final String BDB_TYPE = "BDB_TYPE";
    
    /**
     * The location of the Berkeley DB natives to include as part
     * of the {@code java.library.path}.
     */
    static final String BDB_NATIVES = "BDB_NATIVES";
    
    /**
     * A custom set of native library directories to include as part of the
     * {@code java.library.path}.
     */
    static final String CUSTOM_NATIVES = "CUSTOM_NATIVES";
    
    /**
     * A custom set of additional jar files to include on the classpath.
     */
    static final String CUSTOM_CLASSPATH_ADD = "CUSTOM_CLASSPATH_ADD";
    
    /**
     * Port to listen for SHUTDOWN command.
     */
    static final String SHUTDOWN_PORT = "SHUTDOWN_PORT";
    
    /**
     * Location of the JDK to use when booting up the Kernel.
     */
    static final String JAVA_HOME = "JAVA_HOME";
    
    /**
     * Command line arguments for the JVM.
     */
    static final String JAVA_OPTS = "JAVA_OPTS";
    
    /**
     * The default value for the {@code SGS_DEPLOY} property.
     */
    static final String DEFAULT_SGS_DEPLOY = 
            "${SGS_HOME}" + File.separator + "deploy";
    /**
     * The default value for the {@code SGS_PROPERTIES} property.
     */
    static final String DEFAULT_SGS_PROPERTIES = 
            "${SGS_HOME}" + File.separator + "conf" + File.separator +
            "sgs-server.properties";
    /**
     * The default value for the {@code SGS_LOGGING} property.
     */
    static final String DEFAULT_SGS_LOGGING = 
            "${SGS_HOME}" + File.separator + "conf" + File.separator +
            "sgs-logging.properties";
    /**
     * The default value for the {@code BDB_TYPE} property.
     */
    static final String DEFAULT_BDB_TYPE = "db";
    /**
     * The standard location to look for application properties config
     * file in jars from the {@code SGS_DEPLOY} directory.
     */
    static final String DEFAULT_APP_PROPERTIES = 
            "META-INF/app.properties";
    /**
     * The default value for the {@code DEFAULT_SHUTDOWN_PORT} property.
     */
    static final String DEFAULT_SHUTDOWN_PORT = "1138";
    
    /**
     * The default remote command used to initiate a shutdown
     */
    static final String SHUTDOWN_COMMAND = "SHUTDOWN";
    
    
    static final String DEFAULT_BDB_ROOT = 
            "${SGS_HOME}" + File.separator + 
            "lib" + File.separator + "natives";
    static final String DEFAULT_BDB_LINUX_X86 = 
            DEFAULT_BDB_ROOT + File.separator + "linux-x86";
    static final String DEFAULT_BDB_LINUX_X86_64 = 
            DEFAULT_BDB_ROOT + File.separator + "linux-x86_64";
    static final String DEFAULT_BDB_MACOSX_X86 =
            DEFAULT_BDB_ROOT + File.separator + "macosx-x86";
    static final String DEFAULT_BDB_SOLARIS_SPARC = 
            DEFAULT_BDB_ROOT + File.separator + "solaris-sparc";
    static final String DEFAULT_BDB_SOLARIS_X86 = 
            DEFAULT_BDB_ROOT + File.separator + "solaris-x86";
    static final String DEFAULT_BDB_WIN32_X86 = 
            DEFAULT_BDB_ROOT + File.separator + "win32-x86";
    
    
    static final String KERNEL_CLASS = "com.sun.sgs.impl.kernel.Kernel";
    
    /**
     * Loads configuration properties from a file into a
     * {@link SubstitutionProperties} object.  If the given filename is not
     * {@code null}, it is used as the configuration file.  Otherwise, the
     * resource located at {@link #SGS_BOOT} is used as the configuration file.
     * <p>
     * The properties included in the configuration file must conform to
     * the rules allowed by {@link SubstitutionProperties}.
     * 
     * @param filename name of the config file or {@code null} for default
     * @return a {@code SubstitutionProperties} object representing the
     *         configuration parameters in the file
     */
    static SubstitutionProperties loadProperties(String filename) 
            throws IOException, URISyntaxException {
        
        //load properties from configuration file
        SubstitutionProperties properties = new SubstitutionProperties();
        URL sgsBoot = null;
        InputStream is = null;
        try {
            if (filename == null) {
                sgsBoot = ClassLoader.getSystemResource(
                        BootEnvironment.SGS_BOOT);
            } else {
                sgsBoot = new File(filename).toURI().toURL();
            }
            is = sgsBoot.openStream();
            properties.load(is);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Unable to load initial configuration", e);
            throw e;
        } finally {
            try {
                is.close();
            } catch (IOException ignore) {
                logger.log(Level.FINEST, "Unable to close stream", ignore);
            }
        }
        
        //determine SGS_HOME
        String sgsHome = properties.getProperty(BootEnvironment.SGS_HOME);
        if (sgsHome == null) {
            properties.clear();
            URL jarLocation = BootEnvironment.class.
                    getProtectionDomain().getCodeSource().getLocation();
            
            //get a File from the URL to convert URL escaped characters
            File jarFile = new File(jarLocation.toURI());
            String jarPath = jarFile.getPath();
            int jarFileIndex = jarPath.indexOf(BootEnvironment.SGS_JAR);
            if (jarFileIndex == -1) {
                logger.log(Level.SEVERE, "Unable to determine SGS_HOME");
                throw new IllegalStateException("Unable to determine SGS_HOME");
            } else {
                sgsHome = jarPath.substring(0, jarFileIndex - 1);
                properties.setProperty(BootEnvironment.SGS_HOME, sgsHome);
                //reload the properties so that the value for SGS_HOME
                //is interpolated correctly in any other variables
                try {
                    is = sgsBoot.openStream();
                    properties.load(is);
                } catch (IOException e) {
                    logger.log(Level.SEVERE, 
                               "Unable to load initial configuration", e);
                    throw e;
                } finally {
                    try {
                        is.close();
                    } catch (IOException ignore) {
                        logger.log(Level.FINEST, "Unable to close stream", ignore);
                    }
                }
            }
        }
        logger.log(Level.CONFIG, "SGS_HOME = " + sgsHome);
        
        //load defaults for missing properties
        configureDefaultProperties(properties);
        
        return properties;
    }
    
    /**
     * Loads default values for the given set of properties if any
     * required properties are missing.
     * 
     * @param properties the set of boot configuration properties
     */
    private static void configureDefaultProperties(
            SubstitutionProperties properties) {
        
        //load defaults for any missing properties
        if (properties.getProperty(BootEnvironment.SGS_DEPLOY) == null) {
            properties.setProperty(BootEnvironment.SGS_DEPLOY,
                                   BootEnvironment.DEFAULT_SGS_DEPLOY);
        }
        if (properties.getProperty(BootEnvironment.SGS_LOGGING) == null) {
            properties.setProperty(BootEnvironment.SGS_LOGGING,
                                   BootEnvironment.DEFAULT_SGS_LOGGING);
        }
        if (properties.getProperty(BootEnvironment.SGS_PROPERTIES) == null) {
            properties.setProperty(BootEnvironment.SGS_PROPERTIES,
                                   BootEnvironment.DEFAULT_SGS_PROPERTIES);
        }
        if (properties.getProperty(BootEnvironment.BDB_TYPE) == null) {
            properties.setProperty(BootEnvironment.BDB_TYPE,
                                   BootEnvironment.DEFAULT_BDB_TYPE);
        }
        if (properties.getProperty(BootEnvironment.SHUTDOWN_PORT) == null) {
            properties.setProperty(BootEnvironment.SHUTDOWN_PORT,
                                   BootEnvironment.DEFAULT_SHUTDOWN_PORT);
        }
        
        //autodetect BDB libraries if necessary
        if (properties.getProperty(BootEnvironment.BDB_NATIVES) == null) {
            String name = System.getProperty("os.name");
            String arch = System.getProperty("os.arch");
            String version = System.getProperty("os.version");

            String bdb = null;
            if ("Linux".equals(name) && "i386".equals(arch)) {
                bdb = BootEnvironment.DEFAULT_BDB_LINUX_X86;
            } else if ("Linux".equals(name) &&
                    ("x86_64".equals(arch) || "amd64".equals(arch))) {
                bdb = BootEnvironment.DEFAULT_BDB_LINUX_X86_64;
            } else if ("Mac OS X".equals(name) &&
                    ("i386".equals(arch) || "x86_64".equals(arch))) {
                bdb = BootEnvironment.DEFAULT_BDB_MACOSX_X86;
            } else if ("SunOS".equals(name) && "sparc".equals(arch)) {
                bdb = BootEnvironment.DEFAULT_BDB_SOLARIS_SPARC;
            } else if ("SunOS".equals(name) && "x86".equals(arch)) {
                bdb = BootEnvironment.DEFAULT_BDB_SOLARIS_X86;
            } else if (name != null && name.startsWith("Windows")) {
                bdb = BootEnvironment.DEFAULT_BDB_WIN32_X86;
            } else {
                logger.log(Level.SEVERE, "Unsupported platform: \n" +
                           "Name    : " + name + "\n" +
                           "Arch    : " + arch + "\n" +
                           "Version : " + version);
                throw new IllegalStateException("Unsupported platform");
            }
            properties.setProperty(BootEnvironment.BDB_NATIVES, bdb);
        }
    }
    
}
