package com.coolchoice.monumentphoto;

import java.io.File;
import org.apache.log4j.Level;
import android.os.Environment;
import de.mindpipe.android.logging.log4j.LogConfigurator;

public class ConfigureLog4J {
    public static void configure() {
        final LogConfigurator logConfigurator = new LogConfigurator();                
        logConfigurator.setFileName(Environment.getExternalStorageDirectory() + File.separator + "monument_photo.log");
        logConfigurator.setRootLevel(Level.DEBUG);
        logConfigurator.setLevel("org.apache", Level.ERROR);
        logConfigurator.setMaxFileSize(10 * 1024 * 1024);
        logConfigurator.configure();
    }
}
