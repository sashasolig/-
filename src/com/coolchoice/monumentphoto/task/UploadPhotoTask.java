package com.coolchoice.monumentphoto.task;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

import android.content.Context;
import android.net.Uri;

import com.coolchoice.monumentphoto.Settings;
import com.coolchoice.monumentphoto.dal.DB;
import com.coolchoice.monumentphoto.data.Photo;
import com.coolchoice.monumentphoto.data.Place;
import com.coolchoice.monumentphoto.data.PlacePhoto;

public class UploadPhotoTask extends BaseTask {

    public UploadPhotoTask(AsyncTaskProgressListener pl, AsyncTaskCompleteListener<TaskResult> cb, Context context) {
        super(pl, cb, context);
        this.mTaskName = Settings.TASK_POSTPHOTO;
    }

    @Override
    public void init() {

    }

    @Override
    protected TaskResult doInBackground(String... params) {
        TaskResult result = new TaskResult();
        result.setTaskName(Settings.TASK_POSTPHOTO);
        result.setStatus(TaskResult.Status.OK);
        List<PlacePhoto> placePhotos = this.getPlacePhotoForUpload();
        int successCount = 0;
        int processedCount = 0;
        result.setUploadCount(placePhotos.size());
        for (PlacePhoto placePhoto : placePhotos) {
            try {
                checkIsCancelTask();
                if (placePhoto.Place == null) {
                    continue;
                }
                processedCount++;
                StringBuilder outJSONResponseSB = new StringBuilder();
                boolean isUpload = false;
                isUpload = this.uploadPlacePhoto(this.mainContext, placePhoto, outJSONResponseSB);

                if (isUpload) {
                    if (outJSONResponseSB != null) {
                        ArrayList<PlacePhoto> listPlacePhoto = parsePlacePhotoJSON(outJSONResponseSB.toString());
                        if (listPlacePhoto.size() > 0) {
                            PlacePhoto serverPlacePhoto = listPlacePhoto.get(0);
                            placePhoto.ServerId = serverPlacePhoto.ServerId;
                            placePhoto.Status = Photo.STATUS_SEND;
                            placePhoto.FileName = serverPlacePhoto.FileName;
                            placePhoto.ServerFileName = serverPlacePhoto.ServerFileName;
                            DB.dao(PlacePhoto.class).update(placePhoto);
                        }
                    }
                    successCount++;
                }
            } catch (AuthorizationException e) {
                result.setError(true);
                result.setStatus(TaskResult.Status.LOGIN_FAILED);
            } catch (CancelTaskException cte) {
                result.setError(true);
                result.setStatus(TaskResult.Status.CANCEL_TASK);
            } catch (Exception e) {
                result.setError(true);
                result.setStatus(TaskResult.Status.HANDLE_ERROR);
            }
            result.setUploadCountSuccess(successCount);
            result.setUploadCountError(processedCount - successCount);
            publishUploadProgress("Отправлено фотографий: %d  из %d...", result);
        }

        return result;
    }
    
    private boolean uploadPlacePhoto(Context context, PlacePhoto placePhoto, StringBuilder outResponseSB) throws AuthorizationException, ServerException {
        MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        Uri uri = Uri.parse(placePhoto.UriString);
        String path = uri.getPath();
        File file = new File(path);
        FileBody fileBody = new FileBody(file);
        multipartEntity.addPart("photo", fileBody);
        try {
            multipartEntity.addPart("place", new StringBody(Integer.toString(placePhoto.Place.ServerId), Charset.forName(Settings.DEFAULT_ENCODING)));
            multipartEntity.addPart("lat", new StringBody(Double.toString(placePhoto.Latitude), Charset.forName(Settings.DEFAULT_ENCODING)));
            multipartEntity.addPart("lng", new StringBody(Double.toString(placePhoto.Longitude), Charset.forName(Settings.DEFAULT_ENCODING)));
        } catch (UnsupportedEncodingException e) {
            return false;
        }
        return uploadFile(Settings.getUploadPlacePhotoUrl(context), multipartEntity, context, outResponseSB);
    }

    public List<PlacePhoto> getPlacePhotoForUpload() {
        List<PlacePhoto> placePhotos = null;
        ArrayList<Integer> statusPhotoForSend = new ArrayList<Integer>();
        statusPhotoForSend.add(Photo.STATUS_FORMATE);
        statusPhotoForSend.add(Photo.STATUS_WAIT_SEND);
        try {
            placePhotos = DB.q(PlacePhoto.class).orderBy("CreateDate", true).where().in(Photo.STATUS_FIELD_NAME, statusPhotoForSend).query();
            for (PlacePhoto placePhoto : placePhotos) {
                DB.dao(Place.class).refresh(placePhoto.Place);
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return placePhotos;
    }

}
