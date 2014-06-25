package com.coolchoice.monumentphoto.task;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import android.content.Context;

import com.coolchoice.monumentphoto.Settings;
import com.coolchoice.monumentphoto.dal.DB;
import com.coolchoice.monumentphoto.data.BaseDTO;
import com.coolchoice.monumentphoto.data.Burial;
import com.coolchoice.monumentphoto.data.Grave;
import com.coolchoice.monumentphoto.data.Place;


public class UploadBurialTask extends BaseTask {
		   
    public UploadBurialTask(AsyncTaskProgressListener pl, AsyncTaskCompleteListener<TaskResult> cb, Context context) {
		super(pl, cb, context);
		this.mTaskName = Settings.TASK_POSTBURIAL;
	}

    @Override
    public void init() {

    }

    @Override
    protected TaskResult doInBackground(String... params) {
    	TaskResult result = new TaskResult();
    	result.setTaskName(Settings.TASK_POSTBURIAL);
    	result.setStatus(TaskResult.Status.OK);
    	List<Burial> burialList = DB.dao(Burial.class).queryForEq(BaseDTO.COLUMN_IS_CHANGED, 1);
    	int successCount = 0;
    	int processedCount = 0;
    	result.setUploadCount(burialList.size());
    	boolean isSuccessUpload = false;
		for(Burial burial : burialList){
			if(burial.Grave == null) continue;
			isSuccessUpload = false;
			processedCount++;
			DB.dao(Grave.class).refresh(burial.Grave);
			try {
				checkIsCancelTask();
				Dictionary<String, String> dictPostData = new Hashtable<String, String>();
            	dictPostData.put("burialId", Integer.toString(burial.ServerId));
            	dictPostData.put("graveId", Integer.toString(burial.Grave.ServerId));
            	dictPostData.put("factDate", serializeDate(burial.FactDate));
            	String responseString = postData(params[0], dictPostData);
            	if(responseString != null){
            		//ok	            		
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
    			burial.IsChanged = 0;
    			DB.dao(Burial.class).update(burial);
			}
			result.setUploadCountSuccess(successCount);
            result.setUploadCountError(processedCount - successCount);
            publishUploadProgress("Отправлено захоронений: %d из %d...", result);
		}
        return result;
    }
    
}