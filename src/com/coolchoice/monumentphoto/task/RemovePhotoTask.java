package com.coolchoice.monumentphoto.task;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;

import android.content.Context;

import com.coolchoice.monumentphoto.Settings;
import com.coolchoice.monumentphoto.dal.DB;
import com.coolchoice.monumentphoto.data.BaseDTO;
import com.coolchoice.monumentphoto.data.DeletedObject;
import com.coolchoice.monumentphoto.data.DeletedObjectType;


public class RemovePhotoTask extends BaseTask {
		   
    public RemovePhotoTask(AsyncTaskProgressListener pl, AsyncTaskCompleteListener<TaskResult> cb, Context context) {
		super(pl, cb, context);
		this.mTaskName = Settings.TASK_REMOVEPHOTO;
	}

    @Override
    public void init() {

    }

    @Override
    protected TaskResult doInBackground(String... params) {
    	TaskResult result = new TaskResult();
    	result.setTaskName(Settings.TASK_REMOVEPHOTO);
    	result.setStatus(TaskResult.Status.OK);    	
    	List<DeletedObject> photoInfoList = this.getPhotoForRemove();
    	int successCount = 0;
    	int processedCount = 0;
    	result.setUploadCount(photoInfoList.size());
    	for(DeletedObject deleteObj : photoInfoList){        		
    		try {
    			checkIsCancelTask();
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
            } catch (CancelTaskException cte){
            	result.setError(true);
                result.setStatus(TaskResult.Status.CANCEL_TASK);
            } 
            catch (Exception e) {                
                result.setError(true);
                result.setStatus(TaskResult.Status.HANDLE_ERROR);
            }
    		result.setUploadCountSuccess(successCount);
            result.setUploadCountError(processedCount - successCount);
            publishUploadProgress("Удалено фотографий: %d  из %d...", result);
    	}
    	return result;
    }
    
	private boolean removePhoto(Context context, DeletedObject photoDeleteOdj, StringBuilder outResponseSB) throws AuthorizationException, ServerException{
	    boolean result = false;
		MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		try {
		    if(photoDeleteOdj.TypeId == DeletedObjectType.GRAVEPHOTO){
		        multipartEntity.addPart("gravePhotoId", new StringBody(Integer.toString(photoDeleteOdj.ServerId), Charset.forName(Settings.DEFAULT_ENCODING)));
		    }
		    if(photoDeleteOdj.TypeId == DeletedObjectType.PLACEPHOTO){
		        multipartEntity.addPart("placePhotoId", new StringBody(Integer.toString(photoDeleteOdj.ServerId), Charset.forName(Settings.DEFAULT_ENCODING)));
		    }
		} catch (UnsupportedEncodingException e) {
			return false;
		}
		if(photoDeleteOdj.TypeId == DeletedObjectType.GRAVEPHOTO){
		    result = uploadFile(Settings.getRemoveGravePhotoUrl(context), multipartEntity, context, outResponseSB);
		}
		if(photoDeleteOdj.TypeId == DeletedObjectType.PLACEPHOTO){
		    result = uploadFile(Settings.getRemovePlacePhotoUrl(context), multipartEntity, context, outResponseSB);
		}
		return result;
	}
	
	public List<DeletedObject> getPhotoForRemove(){
		List<DeletedObject> photoInfoList = null;
		ArrayList<Integer> photoTypeList = new ArrayList<Integer>();
		photoTypeList.add(DeletedObjectType.GRAVEPHOTO);
		photoTypeList.add(DeletedObjectType.PLACEPHOTO);
		try {
			photoInfoList = DB.q(DeletedObject.class).where().in("TypeId", photoTypeList).query();		
		} catch (SQLException e) {
			e.printStackTrace();
		}					
		return photoInfoList;
	}
	
}
