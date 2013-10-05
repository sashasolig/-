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


import com.coolchoice.monumentphoto.Settings;
import com.coolchoice.monumentphoto.dal.DB;
import com.coolchoice.monumentphoto.data.Grave;
import com.coolchoice.monumentphoto.data.GravePhoto;
import com.coolchoice.monumentphoto.data.Monument;
import com.j256.ormlite.stmt.UpdateBuilder;

import android.content.Context;
import android.net.Uri;


public class UploadPhotoTask extends BaseTask {
		   
    public UploadPhotoTask(AsyncTaskProgressListener pl, AsyncTaskCompleteListener<TaskResult> cb, Context context) {
		super(pl, cb, context);
		this.mTaskName = Settings.TASK_POSTPHOTOGRAVE;
	}

    @Override
    public void init() {

    }

    @Override
    protected TaskResult doInBackground(String... params) {
    	TaskResult result = new TaskResult();
    	result.setTaskName(Settings.TASK_POSTPHOTOGRAVE);
    	String url = null;
    	if (params.length == 1) {            
        	url = params[0];
        	List<GravePhoto> gravePhotos = this.getGravePhotoForUpload();
        	int successCount = 0;
        	int processedCount = 0;
        	result.setUploadCount(gravePhotos.size());
        	for(GravePhoto gravePhoto : gravePhotos){        		
        		try {
            		if(gravePhoto.Grave == null) continue;
            		processedCount++;
            		DB.dao(Grave.class).refresh(gravePhoto.Grave);
            		StringBuilder outJSONResponseSB = new StringBuilder();
            		boolean isUpload = this.uploadPhoto(this.mainContext, gravePhoto, outJSONResponseSB);
            		if(isUpload){
            			if(outJSONResponseSB != null){
            				ArrayList<GravePhoto> listGravePhoto = parseGravePhotoJSON(outJSONResponseSB.toString());
            				if(listGravePhoto.size() > 0){
            					GravePhoto serverGravePhoto = listGravePhoto.get(0);
            					gravePhoto.ServerId = serverGravePhoto.ServerId;
            					gravePhoto.Status = GravePhoto.STATUS_SEND;
            					DB.dao(GravePhoto.class).update(gravePhoto);
            				}
            			}
            			successCount++;
            		}
        		} catch (AuthorizationException e) {                
                    result.setError(true);
                    result.setStatus(TaskResult.Status.LOGIN_FAILED);
                }
                catch (Exception e) {                
                    result.setError(true);
                    result.setStatus(TaskResult.Status.HANDLE_ERROR);
                }
        		result.setUploadCountSuccess(successCount);
	            result.setUploadCountError(processedCount - successCount);
	            publishUploadProgress("Отправлено фотографий: %d  из %d...", result);
        	}
        }
    	return result;
    }
    
	private boolean uploadPhoto(Context context, GravePhoto gravePhoto, StringBuilder outResponseSB) throws AuthorizationException, ServerException{		
		MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		Uri uri = Uri.parse(gravePhoto.UriString);
    	String path = uri.getPath();
		File file = new File(path);			
		FileBody fileBody = new FileBody(file);              
		multipartEntity.addPart("photo", fileBody);
		try {
			multipartEntity.addPart("grave", new StringBody(Integer.toString(gravePhoto.Grave.ServerId), Charset.forName("UTF-8")));
			multipartEntity.addPart("lat", new StringBody(Double.toString(gravePhoto.Latitude), Charset.forName("UTF-8")));
			multipartEntity.addPart("lng", new StringBody(Double.toString(gravePhoto.Longitude), Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			return false;
		}
		return uploadFile(Settings.getUploadPhotoUrl(context), multipartEntity, context, outResponseSB);                     
	}
	
	public List<GravePhoto> getGravePhotoForUpload(){
		List<GravePhoto> gravePhotos = null;
		ArrayList<Integer> statusPhotoForSend = new ArrayList<Integer>();
		statusPhotoForSend.add(GravePhoto.STATUS_FORMATE);
		statusPhotoForSend.add(GravePhoto.STATUS_WAIT_SEND);
		try {
			gravePhotos = DB.q(GravePhoto.class).orderBy("CreateDate", true).where().in(GravePhoto.STATUS_FIELD_NAME, statusPhotoForSend).query();
			for(GravePhoto gravePhoto : gravePhotos) {
				DB.dao(Grave.class).refresh(gravePhoto.Grave);
			}	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}					
		return gravePhotos;
	}
	
	public static void markGravePhotoAsSended(GravePhoto gravePhoto){
		UpdateBuilder<GravePhoto, Integer> updateBuilder = DB.dao(GravePhoto.class).updateBuilder();
		try {
			updateBuilder.updateColumnValue(Monument.STATUS_FIELD_NAME, Monument.STATUS_SEND);
			updateBuilder.where().idEq(gravePhoto.Id);
			DB.dao(GravePhoto.class).update(updateBuilder.prepare());
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}
    
    
    
    
}
