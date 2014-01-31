package com.coolchoice.monumentphoto;

import org.apache.log4j.Logger;

public class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    Thread.UncaughtExceptionHandler oldHandler;
    
    protected final Logger mFileLog = Logger.getLogger(UncaughtExceptionHandler.class);

    public UncaughtExceptionHandler() {
        oldHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        this.mFileLog.error(Settings.UNEXPECTED_ERROR_MESSAGE, throwable);
        if(oldHandler != null)
            oldHandler.uncaughtException(thread, throwable);
    }
}
