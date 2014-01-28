package com.coolchoice.monumentphoto.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.sql.SQLException;
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
    	List<Place> placeList = DB.dao(Place.class).queryForEq("IsChanged", 1);    	
    	List<Row> rowList = DB.dao(Row.class).queryForEq("IsChanged", 1);
    	for(Row row : rowList){
    		QueryBuilder<Place, Integer> builder = DB.dao(Place.class).queryBuilder();
			try {
				builder.where().eq("Row_id", row.Id).and().eq("IsChanged", 0);
				List<Place> findedPlaces = DB.dao(Place.class).query(builder.prepare());
    			if(findedPlaces.size() > 0){
    				placeList.addAll(findedPlaces);
    			}
			} catch (SQLException e) {					
				e.printStackTrace();
			}
			
    	}
    	int successCount = 0;
    	int processedCount = 0;
    	result.setUploadCount(placeList.size());
    	boolean isSuccessUpload = false;
		for(Place place : placeList){
			if(place.Row == null && place.Region == null) continue;
			isSuccessUpload = false;
			processedCount++;
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
				checkIsCancelTask();
				Dictionary<String, String> dictPostData = new Hashtable<String, String>();
            	dictPostData.put("areaId", Integer.toString(regionServerId));
            	dictPostData.put("placeId", Integer.toString(place.ServerId));
            	dictPostData.put("rowName", rowName);
            	dictPostData.put("placeName", place.Name);
            	if(place.OldName != null){
            		dictPostData.put("oldPlaceName", place.OldName);
            	} else {
            		dictPostData.put("oldPlaceName", "");
            	}
            	if(place.Length != null){
            		dictPostData.put("placeLength", Double.toString(place.Length));
            	} else {
            		dictPostData.put("placeLength", "");
            	}
            	if(place.Width != null){
            		dictPostData.put("placeWidth", Double.toString(place.Width));
            	} else {
            		dictPostData.put("placeWidth", "");
            	}
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
    			DB.dao(Place.class).refresh(place);
    			if(place.Row != null){
    				DB.dao(Row.class).refresh(place.Row);
    				place.Row.IsChanged = 0;
    				DB.dao(Row.class).update(place.Row);
    			}
    			place.IsChanged = 0;
    			DB.dao(Place.class).update(place);
			}
			result.setUploadCountSuccess(successCount);
            result.setUploadCountError(processedCount - successCount);
            publishUploadProgress("Отправлено мест: %d из %d...", result);
		}        
        return result;
    }
    
}
