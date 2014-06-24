package com.coolchoice.monumentphoto.task;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import android.content.Context;

import com.coolchoice.monumentphoto.Settings;
import com.coolchoice.monumentphoto.dal.DB;
import com.coolchoice.monumentphoto.data.BaseDTO;
import com.coolchoice.monumentphoto.data.Grave;
import com.coolchoice.monumentphoto.data.Place;


public class UploadGraveTask extends BaseTask {
		   
    public UploadGraveTask(AsyncTaskProgressListener pl, AsyncTaskCompleteListener<TaskResult> cb, Context context) {
		super(pl, cb, context);
		this.mTaskName = Settings.TASK_POSTGRAVE;
	}

    @Override
    public void init() {

    }

    @Override
    protected TaskResult doInBackground(String... params) {
    	TaskResult result = new TaskResult();
    	result.setTaskName(Settings.TASK_POSTGRAVE);
    	result.setStatus(TaskResult.Status.OK);
    	List<Grave> graveList = DB.dao(Grave.class).queryForEq(BaseDTO.COLUMN_IS_CHANGED, 1);
    	int successCount = 0;
    	int processedCount = 0;
    	result.setUploadCount(graveList.size());
    	boolean isSuccessUpload = false;
		for(Grave grave : graveList){
			if(grave.Place == null) continue;
			isSuccessUpload = false;
			processedCount++;
			DB.dao(Place.class).refresh(grave.Place);
			try {
				checkIsCancelTask();
				Dictionary<String, String> dictPostData = new Hashtable<String, String>();
            	dictPostData.put("placeId", Integer.toString(grave.Place.ServerId));
            	dictPostData.put("graveId", Integer.toString(grave.ServerId));
            	dictPostData.put("graveName", grave.Name != null ? grave.Name : "");
            	int intWrongFIO = 0, intMilitary = 0;
            	if(grave.Place.isWrongFIO()){
            		intWrongFIO = 1; 
            	}
            	if(grave.Place.isMilitary()){
            		intMilitary = 1;
            	}	            	
            	dictPostData.put("isWrongFIO", Integer.toString(intWrongFIO));
            	dictPostData.put("isMilitary", Integer.toString(intMilitary));
            	String responseString = postData(params[0], dictPostData);
            	if(responseString != null){
            		handleResponseUploadGraveJSON(grave, responseString);	            		
            	} else {
            		result.setError(true);
            		result.setStatus(TaskResult.Status.HANDLE_ERROR);
            	}
            	successCount++;
            	isSuccessUpload = true;
            } catch (AuthorizationException e) {                
                result.setError(true);
                result.setStatus(TaskResult.Status.LOGIN_FAILED);
            } catch (CancelTaskException cte){
            	result.setError(true);
                result.setStatus(TaskResult.Status.CANCEL_TASK);
            } catch (Exception e) {                
                result.setError(true);
                result.setStatus(TaskResult.Status.HANDLE_ERROR);
            }
			if(isSuccessUpload){
    			DB.dao(Grave.class).refresh(grave);
    			grave.IsChanged = 0;
    			DB.dao(Grave.class).update(grave);
			}
			result.setUploadCountSuccess(successCount);
            result.setUploadCountError(processedCount - successCount);
            publishUploadProgress("Отправлено могил: %d из %d...", result);
		}
        return result;
    }
    
}