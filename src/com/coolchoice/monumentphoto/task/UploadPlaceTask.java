package com.coolchoice.monumentphoto.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.coolchoice.monumentphoto.Settings;
import com.coolchoice.monumentphoto.dal.DB;
import com.coolchoice.monumentphoto.data.Cemetery;
import com.coolchoice.monumentphoto.data.Place;
import com.coolchoice.monumentphoto.data.Region;
import com.coolchoice.monumentphoto.data.Row;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;

import android.content.Context;
import android.os.SystemClock;


public class UploadPlaceTask extends BaseTask {
		   
    public UploadPlaceTask(AsyncTaskProgressListener pl, AsyncTaskCompleteListener<TaskResult> cb, Context context) {
		super(pl, cb, context);
		this.mTaskName = Settings.TASK_POSTPLACE;
	}

    @Override
    public void init() {

    }

    @Override
    protected TaskResult doInBackground(String... params) {
    	TaskResult result = new TaskResult();
    	result.setTaskName(Settings.TASK_POSTPLACE);
    	if (params.length == 1) {        	
        	List<Place> placeList = DB.dao(Place.class).queryForEq("IsChanged", 1);        	
    		for(Place place : placeList){
    			if(place.Row == null && place.Region == null) continue;
    			String rowName = "";
    			int regionServerId;
    			if(place.Row != null){
    				DB.dao(Row.class).refresh(place.Row);
    				if(place.Row.Region == null) continue;
    				DB.dao(Region.class).refresh(place.Row.Region);    				
    				rowName = place.Row.Name;
    				regionServerId = place.Row.Region.ServerId;
    			} else {
    				DB.dao(Region.class).refresh(place.Region);    				
    				regionServerId = place.Region.ServerId;   				
    			}
    			try {
    				Dictionary<String, String> dictPostData = new Hashtable<String, String>();
	            	dictPostData.put("areaId", Integer.toString(regionServerId));
	            	dictPostData.put("placeId", Integer.toString(place.ServerId));
	            	dictPostData.put("rowName", rowName);
	            	dictPostData.put("placeName", place.Name);
	            	int psFoundUnowned;
	            	if(place.IsOwnerLess){
	            		psFoundUnowned = 1;
	            	} else {
	            		psFoundUnowned = 0;
	            	}
	            	dictPostData.put("psFoundUnowned", Integer.toString(psFoundUnowned));
	            	String responseString = postData(params[0], dictPostData);
                	if(responseString != null){
                		handleResponseUploadPlaceJSON(responseString);                		
                	} else {
                		result.setError(true);
                		result.setStatus(TaskResult.Status.HANDLE_ERROR);
                	}
                } catch (AuthorizationException e) {                
                    result.setError(true);
                    result.setStatus(TaskResult.Status.LOGIN_FAILED);
                }
                catch (Exception e) {                
                    result.setError(true);
                    result.setStatus(TaskResult.Status.HANDLE_ERROR);
                }
    			DB.dao(Place.class).refresh(place);
    			if(place.Row != null){
    				DB.dao(Row.class).refresh(place.Row);
    				place.Row.IsChanged = 0;
    				DB.dao(Row.class).update(place.Row);
    			}
    			place.IsChanged = 0;
    			DB.dao(Place.class).update(place);
    		}       	
        	if(!result.isError()){
        		DB.db().execManualSQL("update row set IsChanged = 0;");
				DB.db().execManualSQL("update place set IsChanged = 0;");
        	}
        }else{
        	throw new IllegalArgumentException("Needs 1 param: url");
        }
        return result;
    }
    
}
