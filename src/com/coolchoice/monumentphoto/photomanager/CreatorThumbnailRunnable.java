package com.coolchoice.monumentphoto.photomanager;

import java.io.File;
import java.io.FileOutputStream;

import com.coolchoice.monumentphoto.Settings;
import com.coolchoice.monumentphoto.data.Photo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.util.Log;

public class CreatorThumbnailRunnable implements Runnable {   

    private PhotoTask mTask;

    public CreatorThumbnailRunnable(PhotoTask task) {
        this.mTask = task;
    }

    @Override
    public void run() {
        setStatus(ThreadManager.STATUS_CREATE_THUMBNAIL_START);
        String uriString = this.mTask.getPhoto().UriString;
        String thumbnailUriString = Photo.generateThumbnailUriString(uriString);
        Uri uri = Uri.parse(uriString);        
        Uri thumbnailUri = Uri.parse(thumbnailUriString);                
        Bitmap thumbnailImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(uri.getPath()), Settings.THUMBNAIL_SIZE, Settings.THUMBNAIL_SIZE);        
        File file = new File(thumbnailUri.getPath());
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            thumbnailImage.compress(Bitmap.CompressFormat.JPEG, 100, out); 
            out.flush();
            out.close();
            this.mTask.setResultUriString(thumbnailUriString);
            setStatus(ThreadManager.STATUS_CREATE_THUMBNAIL_COMPLETE);
        } catch (Exception e) {
            Log.e("CreatorThumbnailRunnable", "CreatorThumbnailRunnable", e);
            setStatus(ThreadManager.STATUS_CREATE_THUMBNAIL_COMPLETE);
        }
    }
    
    private void setStatus(int status){
        this.mTask.setStatus(status);
        ThreadManager.getInstance().handleStatus(this.mTask, this.mTask.getStatus());
    }

}
