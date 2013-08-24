package com.coolchoice.monumentphoto.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.coolchoice.monumentphoto.*;
import com.coolchoice.monumentphoto.dal.*;
import com.coolchoice.monumentphoto.data.*;
import com.j256.ormlite.android.apptools.OrmLiteBaseService;

public class PhotoUploadService extends OrmLiteBaseService<DatabaseHelper> {
	
    private static PendingIntent pi;
    
    private final String LOG_NAME = getClass().getName();
            
    private PhotoUploadTask photoUploadTask;
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
        
    @Override
    public void onCreate() {    	
        super.onCreate();        
    }
    
    @Override
    public int onStartCommand(Intent i, int flags, int startId) {
    	if(photoUploadTask == null){    		
    		photoUploadTask = new PhotoUploadTask(getApplicationContext());
    		photoUploadTask.execute("");
    	}else{
    		if(photoUploadTask.getStatus() == AsyncTask.Status.FINISHED){
    			photoUploadTask = new PhotoUploadTask(getApplicationContext());
    			photoUploadTask.execute("");
    		}
    	}        
        setAlarm(30);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (pi != null) {
            try {
                mgr.cancel(pi);
            } catch (Exception e) {                
                Log.e(LOG_NAME, "unable to cancel pending intent");
            }
        }
    }
    
    public void setAlarm(int seconds) {
        AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (pi != null) {
            try {
                mgr.cancel(pi);
            } catch (Exception e) {
                Log.e(LOG_NAME, "unable to cancel pending intent");
            }
        }
        Intent intent = new Intent(this, PhotoUploadService.class);
        pi = PendingIntent.getService(this, 0, intent, 0);
        mgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + seconds * 1000, pi);
    }
     
	private boolean uploadPhoto(Context context, GravePhoto gravePhoto){
		try{
			MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			//multipartEntity.addPart("serverFileName", new StringBody(photo.getServerFileName()));
			Uri uri = Uri.parse(gravePhoto.UriString);
        	String path = uri.getPath();
			File file = new File(path);			
			FileBody fileBody = new FileBody(file);                
			multipartEntity.addPart("photo", fileBody);
			return uploadFile(Settings.getUploadPhotoUrl(context), multipartEntity, context);
        }
        catch (Exception e){
        	Toast.makeText(getApplicationContext(), "UploadPhoto", Toast.LENGTH_LONG).show();
        }
        return false;              
	}		
	
	private boolean uploadFile(String url, MultipartEntity multipartEntity, Context context){
		HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);
        try {
        	httpPost.setEntity(multipartEntity);
        	HttpResponse httpResponse = null;
        	httpResponse = httpClient.execute(httpPost);        	  	
            if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
    		{  
            	/*InputStream io = httpResponse.getEntity().getContent();
    			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(io));
    			StringBuilder sb = new StringBuilder();
    			String line;    			
    			while ((line = bufferedReader.readLine()) != null) {
    			    sb.append(line);
    			}
    			return sb.toString().equalsIgnoreCase("OK");*/
            	return true;
    		}
        }
        catch(FileNotFoundException e){
        	// файл по указанному пути не найден, будем считать что мы его передали, чтобы не стопить завершение рейса.
            return true;
        }
        catch (IOException e) {
            Log.e("e", "IOException: " + e);
        }
        catch (Exception e) {
        	Log.e("e", "Exception: " + e);
		}
        return false;        
	}    
    
    class PhotoUploadTask extends AsyncTask<String, String, String> {
    	
        private Context photoUploadTaskContext;
                
        public PhotoUploadTask(Context cx) {
            this.photoUploadTaskContext = cx;            
        }       

        @Override
        protected String doInBackground(String... params) {
        	List<GravePhoto> gravePhotos = MonumentDB.getGravePhotoForUpload();
        	for(GravePhoto gravePhoto : gravePhotos){
        		boolean result = uploadPhoto(photoUploadTaskContext, gravePhoto);
        		if(result){
        			MonumentDB.markGravePhotoAsSended(gravePhoto);        			
        		}
        		
        	}
        	return "";			
        }

        @Override
        protected void onProgressUpdate(String... messages) {
            //do nothing
        }

        @Override
        protected void onPostExecute(String result) {
        	//do nothing
        }
    }
    
    
}
