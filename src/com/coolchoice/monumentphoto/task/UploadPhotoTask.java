package com.coolchoice.monumentphoto.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
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
import com.coolchoice.monumentphoto.dal.MonumentDB;
import com.coolchoice.monumentphoto.data.Cemetery;
import com.coolchoice.monumentphoto.data.Grave;
import com.coolchoice.monumentphoto.data.GravePhoto;
import com.coolchoice.monumentphoto.data.Monument;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;

import android.content.Context;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;


public class UploadPhotoTask extends BaseTask {
		   
    public UploadPhotoTask(AsyncTaskProgressListener pl, AsyncTaskCompleteListener<TaskResult> cb, Context context) {
		super(pl, cb, context);
		this.mTaskName = Settings.TASK_POSTPHOTOGRAVE;
	}

    @Override
    public void init() {

    }

    @Override
    protected TaskResult doInBackground(String... params) {
    	TaskResult result = new TaskResult();
    	result.setTaskName(Settings.TASK_POSTPHOTOGRAVE);
    	String url = null;
    	if (params.length == 1) {            
        	url = params[0];
        	List<GravePhoto> gravePhotos = this.getGravePhotoForUpload();
        	int successCount = 0;
        	int processedCount = 0;
        	result.setUploadCount(gravePhotos.size());
        	for(GravePhoto gravePhoto : gravePhotos){        		
        		try {
            		if(gravePhoto.Grave == null) continue;
            		processedCount++;
            		DB.dao(Grave.class).refresh(gravePhoto.Grave);
            		boolean isUpload = this.uploadPhoto(this.mainContext, gravePhoto);
            		if(isUpload){
            			this.markGravePhotoAsSended(gravePhoto);
            			successCount++;
            		}
        		} catch (AuthorizationException e) {                
                    result.setError(true);
                    result.setStatus(TaskResult.Status.LOGIN_FAILED);
                }
                catch (Exception e) {                
                    result.setError(true);
                    result.setStatus(TaskResult.Status.HANDLE_ERROR);
                }
        		result.setUploadCountSuccess(successCount);
	            result.setUploadCountError(processedCount - successCount);
	            publishUploadProgress("Отправлено фотографий: %d  из %d...", result);
        	}
        }
    	return result;
    }
    
	private boolean uploadPhoto(Context context, GravePhoto gravePhoto) throws AuthorizationException, ServerException{		
		MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		Uri uri = Uri.parse(gravePhoto.UriString);
    	String path = uri.getPath();
		File file = new File(path);			
		FileBody fileBody = new FileBody(file);              
		multipartEntity.addPart("photo", fileBody);
		try {
			multipartEntity.addPart("grave", new StringBody(Integer.toString(gravePhoto.Grave.ServerId), Charset.forName("UTF-8")));
			multipartEntity.addPart("lat", new StringBody(Double.toString(gravePhoto.Latitude), Charset.forName("UTF-8")));
			multipartEntity.addPart("lng", new StringBody(Double.toString(gravePhoto.Longitude), Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			return false;
		}
		return uploadFile(Settings.getUploadPhotoUrl(context), multipartEntity, context);                     
	}		
	
	private boolean uploadFile(String url, MultipartEntity multipartEntity, Context context) throws AuthorizationException, ServerException{
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);
		HttpParams httpParams = new BasicHttpParams();
    	httpParams.setParameter("http.protocol.handle-redirects", false);
    	httpPost.setParams(httpParams);    	
    	httpPost.addHeader(LoginTask.HEADER_COOKIE, String.format(LoginTask.KEY_PDSESSION + "=%s", Settings.getPDSession()));
		try {
        	httpPost.setEntity(multipartEntity);
        	HttpResponse httpResponse = null;
        	httpResponse = httpClient.execute(httpPost);
        	int statusCode = httpResponse.getStatusLine().getStatusCode();
            if(statusCode == HttpStatus.SC_OK)
    		{
            	return true;
    		} else if (statusCode == 302){
    			throw new AuthorizationException();
    		} else {
    			throw new ServerException();
    		}
        }
        catch(FileNotFoundException e){
        	return true;
        }
        catch(IOException exc){
        	throw new ServerException();
        }              
	}
	
	public List<GravePhoto> getGravePhotoForUpload(){
		List<GravePhoto> gravePhotos = null;
		ArrayList<Integer> statusPhotoForSend = new ArrayList<Integer>();
		statusPhotoForSend.add(GravePhoto.STATUS_FORMATE);
		statusPhotoForSend.add(GravePhoto.STATUS_WAIT_SEND);
		try {
			gravePhotos = DB.q(GravePhoto.class).orderBy("CreateDate", true).where().in(GravePhoto.STATUS_FIELD_NAME, statusPhotoForSend).query();
			for(GravePhoto gravePhoto : gravePhotos) {
				DB.dao(Grave.class).refresh(gravePhoto.Grave);
			}	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}					
		return gravePhotos;
	}
	
	public static void markGravePhotoAsSended(GravePhoto gravePhoto){
		UpdateBuilder<GravePhoto, Integer> updateBuilder = DB.dao(GravePhoto.class).updateBuilder();
		try {
			updateBuilder.updateColumnValue(Monument.STATUS_FIELD_NAME, Monument.STATUS_SEND);
			updateBuilder.where().idEq(gravePhoto.Id);
			DB.dao(GravePhoto.class).update(updateBuilder.prepare());
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}
    
    
    
    
}
