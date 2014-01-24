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
import com.coolchoice.monumentphoto.data.BaseDTO;
import com.coolchoice.monumentphoto.data.DeletedObject;
import com.coolchoice.monumentphoto.data.DeletedObjectType;
import com.coolchoice.monumentphoto.data.Grave;
import com.coolchoice.monumentphoto.data.GravePhoto;
import com.j256.ormlite.stmt.UpdateBuilder;

import android.content.Context;
import android.net.Uri;


public class RemovePhotoTask extends BaseTask {
		   
    public RemovePhotoTask(AsyncTaskProgressListener pl, AsyncTaskCompleteListener<TaskResult> cb, Context context) {
		super(pl, cb, context);
		this.mTaskName = Settings.TASK_REMOVEPHOTOGRAVE;
	}

    @Override
    public void init() {

    }

    @Override
    protected TaskResult doInBackground(String... params) {
    	TaskResult result = new TaskResult();
    	result.setTaskName(Settings.TASK_REMOVEPHOTOGRAVE);
    	String url = null;
    	if (params.length == 1) {            
        	url = params[0];
        	List<DeletedObject> gravePhotoInfoList = this.getGravePhotoForRemove();
        	int successCount = 0;
        	int processedCount = 0;
        	result.setUploadCount(gravePhotoInfoList.size());
        	for(DeletedObject deleteObj : gravePhotoInfoList){        		
        		try {
            		if(deleteObj.ServerId == BaseDTO.INT_NULL_VALUE) continue;
            		processedCount++;
            		StringBuilder outJSONResponseSB = new StringBuilder();
            		boolean isUpload = this.removePhoto(this.mainContext, deleteObj, outJSONResponseSB);
            		if(isUpload){
            			DB.dao(DeletedObject.class).delete(deleteObj);
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
	            publishUploadProgress("Удалено фотографий: %d  из %d...", result);
        	}
        }
    	return result;
    }
    
	private boolean removePhoto(Context context, DeletedObject gravePhotoDeleteOdj, StringBuilder outResponseSB) throws AuthorizationException, ServerException{		
		MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		try {
			multipartEntity.addPart("gravePhotoId", new StringBody(Integer.toString(gravePhotoDeleteOdj.ServerId), Charset.forName("UTF-8")));			
		} catch (UnsupportedEncodingException e) {
			return false;
		}
		return uploadFile(Settings.getRemovePhotoUrl(context), multipartEntity, context, outResponseSB);                     
	}
	
	public List<DeletedObject> getGravePhotoForRemove(){
		List<DeletedObject> gravePhotoInfoList = null;
		try {
			gravePhotoInfoList = DB.q(DeletedObject.class).where().eq("TypeId", DeletedObjectType.GRAVEPHOTO).query();			
		} catch (SQLException e) {
			e.printStackTrace();
		}					
		return gravePhotoInfoList;
	}
	
}
