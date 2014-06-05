package com.coolchoice.monumentphoto;

import org.apache.log4j.Level;
import de.mindpipe.android.logging.log4j.LogConfigurator;

public class ConfigureLog4J {
    public static void configure() {
        final LogConfigurator logConfigurator = new LogConfigurator();                
        logConfigurator.setFileName(Settings.getLogFilePath());
        logConfigurator.setRootLevel(Level.DEBUG);
        logConfigurator.setLevel("org.apache", Level.ERROR);
        logConfigurator.setMaxFileSize(5 * 1024 * 1024);
        logConfigurator.setMaxBackupSize(1);
        logConfigurator.setUseFileAppender(true);
        logConfigurator.configure();
    }
}

