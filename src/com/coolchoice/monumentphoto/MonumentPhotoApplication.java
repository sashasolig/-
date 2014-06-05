package com.coolchoice.monumentphoto;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

import com.coolchoice.monumentphoto.dal.DB;


@ReportsCrashes(formKey = "", mailTo = "health.developer.by@gmail.com", 
mode = ReportingInteractionMode.DIALOG,
resToastText = R.string.crash_toast_text,
resDialogText = R.string.crash_dialog_text,
resDialogIcon = android.R.drawable.ic_dialog_info,
resDialogTitle = R.string.crash_dialog_title,
resDialogCommentPrompt = R.string.crash_dialog_comment_prompt,
resDialogOkToast = R.string.crash_dialog_ok_toast,
customReportContent = { ReportField.USER_COMMENT, ReportField.APP_VERSION_CODE, ReportField.APP_VERSION_NAME, ReportField.DEVICE_ID, ReportField.DEVICE_FEATURES,
    ReportField.ANDROID_VERSION, ReportField.PHONE_MODEL, ReportField.CUSTOM_DATA, ReportField.STACK_TRACE, ReportField.LOGCAT, 
    ReportField.APPLICATION_LOG, ReportField.SHARED_PREFERENCES},
    applicationLogFileLines = 1000 )
public class MonumentPhotoApplication extends Application {

    @Override
    public void onCreate() {
        ConfigureLog4J.configure();
        ACRA.init(this);
        String applicationLogFilePath = Settings.getLogFilePath();
        ACRA.getErrorReporter().checkReportsOnApplicationStart();        
        ACRA.getConfig().setApplicationLogFile(applicationLogFilePath);
    	Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());    	
        super.onCreate();
        DB.setContext(getApplicationContext());
    }

    @Override
    public void onTerminate() {
    	DB.release();
        super.onTerminate();
    }
}

