package com.coolchoice.monumentphoto.photomanager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;

import com.coolchoice.monumentphoto.Settings;
import com.coolchoice.monumentphoto.data.BaseDTO;
import com.coolchoice.monumentphoto.data.Photo;

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
        /*Bitmap thumbnailImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(uri.getPath()), Settings.THUMBNAIL_SIZE, Settings.THUMBNAIL_SIZE);        
        File file = new File(thumbnailUri.getPath());
        if (file.exists()) {
            file.delete();
        }*/
        try {
            /*FileOutputStream out = new FileOutputStream(file);
            thumbnailImage.compress(Bitmap.CompressFormat.JPEG, 100, out); 
            out.flush();
            out.close();*/
            createThumbnail(uri, thumbnailUri);
            this.mTask.setResultUriString(thumbnailUriString);
            setStatus(ThreadManager.STATUS_CREATE_THUMBNAIL_COMPLETE);
        } catch (Exception e) {
            Log.e("CreatorThumbnailRunnable", "CreatorThumbnailRunnable", e);
            setStatus(ThreadManager.STATUS_CREATE_THUMBNAIL_COMPLETE);
        }
    }
    
    private void createThumbnail(Uri uri, Uri thumbnailUri) throws IOException {
        ExifInterface exifInterface = new ExifInterface(uri.getPath());
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, BaseDTO.INT_NULL_VALUE);
        int angle = 0;
        switch (orientation) {
        case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
        case ExifInterface.ORIENTATION_FLIP_VERTICAL:
        case ExifInterface.ORIENTATION_NORMAL:
        case ExifInterface.ORIENTATION_TRANSPOSE:
        case ExifInterface.ORIENTATION_TRANSVERSE:
        case ExifInterface.ORIENTATION_UNDEFINED:
            angle = 0;
            break;
        case ExifInterface.ORIENTATION_ROTATE_180:
            angle = 180;
            break;
        case ExifInterface.ORIENTATION_ROTATE_270:
            angle = 270;
            break;
        case ExifInterface.ORIENTATION_ROTATE_90:
            angle = 90;
            break;
        default:
            break;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);        
        int targetW = Settings.THUMBNAIL_SIZE;
        int targetH = Settings.THUMBNAIL_SIZE;        
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(uri.getPath(), bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        int scaleFactor = Math.max(photoW/targetW, photoH/targetH);
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;
        Bitmap bitmap = BitmapFactory.decodeFile(uri.getPath(), bmOptions);
        Bitmap resultBitmap = null;
        if(angle != 0){
            resultBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } else {
            resultBitmap = bitmap;
        }
        File file = new File(thumbnailUri.getPath());
        if (file.exists()) {
            file.delete();
        }
        FileOutputStream out = new FileOutputStream(file);
        resultBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out); 
        out.flush();
        out.close();
    }
    
    private void setStatus(int status){
        this.mTask.setStatus(status);
        ThreadManager.getInstance().handleStatus(this.mTask, this.mTask.getStatus());
    }

}
