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
import com.coolchoice.monumentphoto.data.Grave;
import com.coolchoice.monumentphoto.data.Place;
import com.coolchoice.monumentphoto.data.Region;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;

import android.content.Context;
import android.os.SystemClock;


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
        if (params.length == 1) {        	
        	List<Grave> graveList = DB.dao(Grave.class).queryForEq("IsChanged", 1);
    		for(Grave grave : graveList){
    			if(grave.Place == null) continue;
    			DB.dao(Place.class).refresh(grave.Place);
    			try {
    				Dictionary<String, String> dictPostData = new Hashtable<String, String>();
	            	dictPostData.put("placeId", Integer.toString(grave.Place.ServerId));
	            	dictPostData.put("graveId", Integer.toString(grave.ServerId));
	            	dictPostData.put("graveName", grave.Name);
	            	String responseString = postData(params[0], dictPostData);
	            	if(responseString != null){
	            		handleResponseUploadGraveJSON(responseString);	            		
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
    			DB.dao(Grave.class).refresh(grave);
    			grave.IsChanged = 0;
    			DB.dao(Grave.class).update(grave);
    		}
    		if(!result.isError()){
    			DB.db().execManualSQL("update grave set IsChanged = 0;");
    		}
        }else{
        	throw new IllegalArgumentException("Needs 1 param: url");
        }
        return result;
    }
    
}