package com.coolchoice.monumentphoto.photomanager;

import android.content.Context;

import com.coolchoice.monumentphoto.data.ComplexGrave;
import com.coolchoice.monumentphoto.data.Photo;

public class PhotoTask {
    
    private int mStatus = ThreadManager.STATUS_INITIAL;
    
    private int mTaskType;
        
    private Thread mCurrentThread;
    
    private ThreadManager mThreadManager;
            
    private Photo mPhoto;
    
    private ComplexGrave mComplexGrave;
    
    private Context mContext;
    
    private String mResultUriString = null;
    
    public PhotoTask(Context context, Photo photo, ComplexGrave complexGrave, int taskType){
        this.mContext = context;
        this.mPhoto = photo;
        this.mComplexGrave = complexGrave;
        this.mTaskType = taskType;
    }
    
    public Context getContext() {
        return mContext;
    }
    
    public int getTaskType() {
        return mTaskType;
    }    
    
    public String getResultUriString() {
        return mResultUriString;
    }

    public void setResultUriString(String resultUriString) {
        this.mResultUriString = resultUriString;
    }
    
    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int mStatus) {
        this.mStatus = mStatus;
    }

        
    public ComplexGrave getComplexGrave() {
        return mComplexGrave;
    }

    public void setComplexGrave(ComplexGrave mComplexGrave) {
        this.mComplexGrave = mComplexGrave;
    }

    public Photo getPhoto() {
        return mPhoto;
    }

    public void setPhoto(Photo mPhoto) {
        this.mPhoto = mPhoto;
    }

    public Thread getCurrentThread() {
        return mCurrentThread;
    }

    public void setCurrentThread(Thread mCurrentThread) {
        this.mCurrentThread = mCurrentThread;
    }

    public ThreadManager getThreadManager() {
        return mThreadManager;
    }

    public void setThreadManager(ThreadManager mThreadManager) {
        this.mThreadManager = mThreadManager;
    }
}
