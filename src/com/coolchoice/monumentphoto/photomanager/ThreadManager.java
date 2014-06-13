package com.coolchoice.monumentphoto.photomanager;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.coolchoice.monumentphoto.data.ComplexGrave;
import com.coolchoice.monumentphoto.data.PlacePhoto;

public final class ThreadManager {
    
    public interface OnChangeStatus{
        void onChangeDownloadStatus(PlacePhoto placePhoto, int status);
        
        void onChangeCreateThumbnailStatus(PlacePhoto placePhoto, int status);
    }
    
    public static final int STATUS_INITIAL = 0;
    public static final int STATUS_DOWNLOAD_START = 1;
    public static final int STATUS_DOWNLOAD_COMPLETE = 2;
    public static final int STATUS_DOWNLOAD_ERROR = 3;
    
    public static final int STATUS_CREATE_THUMBNAIL_START = 101;
    public static final int STATUS_CREATE_THUMBNAIL_ERROR = 102;
    public static final int STATUS_CREATE_THUMBNAIL_COMPLETE = 103;
    
    
    public static final int TASK_DOWNLOAD_THUMBNAIL = 1;
    public static final int TASK_DOWNLOAD_IMAGE = 2;
    public static final int TASK_CREATE_THUMBNAIL = 3;
    
    private static ThreadManager sInstance = null;
    
    private final BlockingQueue<Runnable> mDownloadWorkQueue;

    private final ThreadPoolExecutor mThreadPool;
            
    private Handler mHandlerUI;
    
    private HashMap<Integer, PhotoTask> mTaskQueue;
    
    private OnChangeStatus mOnChangeStatus;
            
    static {
        sInstance = new ThreadManager();
    }
    
    private ThreadManager() {
        this.mTaskQueue = new HashMap<Integer, PhotoTask>();
        this.mDownloadWorkQueue = new LinkedBlockingQueue<Runnable>();
        this.mThreadPool = new ThreadPoolExecutor(1, 1, 1, TimeUnit.MINUTES, mDownloadWorkQueue);
        mHandlerUI = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                PhotoTask task = (PhotoTask) inputMessage.obj;
                int status = inputMessage.what;
                sInstance.invokeChangeStatusFromUI(task, status);
            }
        };
    }
    
    public static ThreadManager getInstance() {
        return sInstance;
    }
    
    public void setOnChangeDownloadStatus(OnChangeStatus onChangeDownloadStatus){
        this.mOnChangeStatus = onChangeDownloadStatus;
    }
        
    public void downloadThumbnail (PlacePhoto placePhoto, ComplexGrave complexGrave) {            
        synchronized (sInstance) {
            if(this.mTaskQueue.get(placePhoto.Id) == null){
                PhotoTask task = new PhotoTask(placePhoto, complexGrave, ThreadManager.TASK_DOWNLOAD_THUMBNAIL);
                this.mTaskQueue.put(placePhoto.Id, task);
                DownloadPhotoRunnable runnable = new DownloadPhotoRunnable(task);
                this.mThreadPool.execute(runnable);
            }
        }
    }
    
    public void createThumbnail (PlacePhoto placePhoto, ComplexGrave complexGrave) {            
        synchronized (sInstance) {
            if(this.mTaskQueue.get(placePhoto.Id) == null){
                PhotoTask task = new PhotoTask(placePhoto, complexGrave, ThreadManager.TASK_CREATE_THUMBNAIL);
                this.mTaskQueue.put(placePhoto.Id, task);
                CreatorThumbnailRunnable runnable = new CreatorThumbnailRunnable(task);
                this.mThreadPool.execute(runnable);
            }
        }
    }
    
    //from background thread
    public void handleStatus(PhotoTask task, int status) {     
        Message completeMessage = mHandlerUI.obtainMessage(status, task);
        completeMessage.sendToTarget();             
    }
    
    //from UI thread
    public void invokeChangeStatusFromUI(PhotoTask task, int status){
        if(this.mOnChangeStatus != null){            
            PlacePhoto placePhoto = (PlacePhoto) task.getPhoto();            
            if(task.getTaskType() == ThreadManager.TASK_CREATE_THUMBNAIL){
                placePhoto.ThumbnailUriString = task.getResultUriString();
                this.mOnChangeStatus.onChangeCreateThumbnailStatus(placePhoto, status);
            }
            if(task.getTaskType() == ThreadManager.TASK_DOWNLOAD_IMAGE){
                placePhoto.UriString = task.getResultUriString();
                this.mOnChangeStatus.onChangeDownloadStatus(placePhoto, status);
            }
            if(task.getTaskType() == ThreadManager.TASK_DOWNLOAD_THUMBNAIL){
                placePhoto.ThumbnailUriString = task.getResultUriString();
                this.mOnChangeStatus.onChangeDownloadStatus(placePhoto, status);
            }            
            this.mTaskQueue.remove(task.getPhoto().Id);
        }                  
    }
    
}
