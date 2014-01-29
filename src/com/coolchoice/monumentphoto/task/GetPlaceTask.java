package com.coolchoice.monumentphoto.task;

import com.coolchoice.monumentphoto.Settings;
import android.content.Context;

public class GetPlaceTask extends BaseTask {
		   
    public GetPlaceTask(AsyncTaskProgressListener pl, AsyncTaskCompleteListener<TaskResult> cb, Context context) {
		super(pl, cb, context);
		this.mTaskName = Settings.TASK_GETPLACE;
	}

    @Override
    public void init() {

    }

    @Override
    protected TaskResult doInBackground(String... params) {
    	TaskResult result = new TaskResult();
    	result.setTaskName(Settings.TASK_GETPLACE);
    	result.setStatus(TaskResult.Status.OK);
    	String resultJSON = null;
    	String url = params[0];
        try {
        	initGETQueryParameters(url);
        	resultJSON = getJSON(url);            	
        } catch (AuthorizationException e) {                
            result.setError(true);
            result.setStatus(TaskResult.Status.LOGIN_FAILED);
        } catch (CancelTaskException cte){
        	result.setError(true);
            result.setStatus(TaskResult.Status.CANCEL_TASK);
        }
        catch (Exception e) {                
            result.setError(true);
            result.setStatus(TaskResult.Status.SERVER_UNAVALAIBLE);
        }
        
        if(resultJSON != null && !result.isError()){
            try{	            	
            	handleResponseGetPlaceJSON(resultJSON, this.mCemeteryServerId, this.mRegionServerId, this.mLastQueryServerDate);  
            } catch (CancelTaskException cte){
            	result.setError(true);
                result.setStatus(TaskResult.Status.CANCEL_TASK);
            } catch (Exception e) {                
                result.setError(true);
                result.setStatus(TaskResult.Status.HANDLE_ERROR);
                this.mFileLog.error(url, e);
            }
        }        
        return result;
    }
    
}