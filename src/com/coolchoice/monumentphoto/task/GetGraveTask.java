package com.coolchoice.monumentphoto.task;

import com.coolchoice.monumentphoto.Settings;
import android.content.Context;
import android.util.Log;

public class GetGraveTask extends BaseTask {
		   
    public GetGraveTask(AsyncTaskProgressListener pl, AsyncTaskCompleteListener<TaskResult> cb, Context context) {
		super(pl, cb, context);
		this.mTaskName = Settings.TASK_GETGRAVE;
	}

    @Override
    public void init() {

    }

    @Override
    protected TaskResult doInBackground(String... params) {
    	TaskResult result = new TaskResult();
    	result.setTaskName(Settings.TASK_GETGRAVE);
    	String resultJSON = null;    	
        try {
        	initGETQueryParameters(params[0]);
        	resultJSON = getJSON(params[0]);            	
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
            	this.mFileLog.info(resultJSON);
            	handleResponseGetGraveJSON(resultJSON, this.mCemeteryServerId, this.mRegionServerId, this.mLastQueryServerDate);	                
            } catch (CancelTaskException cte){
            	result.setError(true);
                result.setStatus(TaskResult.Status.CANCEL_TASK);
            } catch (Exception e) {                
                result.setError(true);
                result.setStatus(TaskResult.Status.HANDLE_ERROR);
                this.mFileLog.error(resultJSON, e);
            }
        }    	
        return result;
    }   
    
}