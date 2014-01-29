package com.coolchoice.monumentphoto.task;

import com.coolchoice.monumentphoto.Settings;
import android.content.Context;

public class GetCemeteryTask extends BaseTask {
		   
    public GetCemeteryTask(AsyncTaskProgressListener pl, AsyncTaskCompleteListener<TaskResult> cb, Context context) {
		super(pl, cb, context);
		this.mTaskName = Settings.TASK_GETCEMETERY;
	}

    @Override
    public void init() {

    }

    @Override
    protected TaskResult doInBackground(String... params) {
    	TaskResult result = new TaskResult();
    	result.setTaskName(Settings.TASK_GETCEMETERY);
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
            	handleResponseGetCemeteryJSON(resultJSON);  
            }catch (CancelTaskException cte){
            	result.setError(true);
                result.setStatus(TaskResult.Status.CANCEL_TASK);
            }catch (Exception e) {                
                result.setError(true);
                result.setStatus(TaskResult.Status.HANDLE_ERROR);
                this.mFileLog.error(url, e);
            }
        }
                
        return result;
    }    
}