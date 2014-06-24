package com.coolchoice.monumentphoto.task;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import android.content.Context;

import com.coolchoice.monumentphoto.Settings;
import com.coolchoice.monumentphoto.dal.DB;
import com.coolchoice.monumentphoto.data.BaseDTO;
import com.coolchoice.monumentphoto.data.Cemetery;
import com.coolchoice.monumentphoto.data.GPSRegion;
import com.coolchoice.monumentphoto.data.Region;


public class UploadRegionTask extends BaseTask {
		   
    public UploadRegionTask(AsyncTaskProgressListener pl, AsyncTaskCompleteListener<TaskResult> cb, Context context) {
		super(pl, cb, context);
		this.mTaskName = Settings.TASK_POSTREGION;
	}

    @Override
    public void init() {

    }

    @Override
    protected TaskResult doInBackground(String... params) {
    	TaskResult result = new TaskResult();
    	result.setTaskName(Settings.TASK_POSTREGION);
    	result.setStatus(TaskResult.Status.OK);
    	List<Region> regionList = DB.dao(Region.class).queryForEq(BaseDTO.COLUMN_IS_CHANGED, 1);
    	for(Region region : regionList){
			DB.dao(Cemetery.class).refresh(region.Cemetery);
		}
    	int successCount = 0;
    	int processedCount = 0;
    	result.setUploadCount(regionList.size());
    	boolean isSuccessUpload = false;
    	for(Region region : regionList){
    		if(region.Cemetery == null) continue;
    		processedCount++;
    		isSuccessUpload = false;
            try {
            	checkIsCancelTask();
            	String gpsJSON = "";
                if(region.IsGPSChanged != 0) {
                    for(GPSRegion gps : region.GPSRegionList){
                        DB.dao(GPSRegion.class).refresh(gps);
                    }
                    gpsJSON = createGPSRegionJSON(region);
                }  
            	Dictionary<String, String> dictPostData = new Hashtable<String, String>();
            	dictPostData.put("cemeteryId", Integer.toString(region.Cemetery.ServerId));
            	dictPostData.put("areaId", Integer.toString(region.ServerId));
            	dictPostData.put("areaName", region.Name);
            	dictPostData.put("square", region.Square != null ? Double.toString(region.Square) : "");
            	dictPostData.put("gps", gpsJSON);
            	String responseString = postData(params[0], dictPostData);
            	if(responseString != null){
            		handleResponseUploadRegionJSON(responseString);       		
            	} else{
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
	            DB.dao(Region.class).refresh(region);
	            region.IsChanged = 0;
	            region.IsGPSChanged = 0;
	            DB.dao(Region.class).update(region);
            }
            result.setUploadCountSuccess(successCount);
            result.setUploadCountError(processedCount - successCount);
            publishUploadProgress("Отправлено участков: %d  из %d...", result);
    	}        
        return result;
    }
    
}