package com.coolchoice.monumentphoto.task;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.coolchoice.monumentphoto.Settings;
import com.coolchoice.monumentphoto.dal.DB;
import com.coolchoice.monumentphoto.data.BaseDTO;
import com.coolchoice.monumentphoto.data.Burial;
import com.coolchoice.monumentphoto.data.Cemetery;
import com.coolchoice.monumentphoto.data.GPSCemetery;
import com.coolchoice.monumentphoto.data.GPSRegion;
import com.coolchoice.monumentphoto.data.Grave;
import com.coolchoice.monumentphoto.data.GravePhoto;
import com.coolchoice.monumentphoto.data.Place;
import com.coolchoice.monumentphoto.data.PlacePhoto;
import com.coolchoice.monumentphoto.data.Region;
import com.coolchoice.monumentphoto.data.ResponsibleUser;
import com.coolchoice.monumentphoto.data.Row;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

public abstract class BaseTask extends AsyncTask<String, String, TaskResult> {
	
	public final static String HEADER_REFERER = "REFERER";
	
    AsyncTaskProgressListener progressListener;
    AsyncTaskCompleteListener<TaskResult> callback;
    Context mainContext;
    TaskResult mTaskResult;
    protected String mTaskName = null;
    public static final String ARG_CEMETERY_ID = "cemeteryId";
    public static final String ARG_AREA_ID = "areaId";
    public static final String ARG_PLACE_ID = "placeId";
    public static final String ARG_GRAVE_ID = "graveId";
    public static final String ARG_SYNC_DATE = "syncDate";
    
    protected int mCemeteryServerId = BaseDTO.INT_NULL_VALUE;
    protected int mRegionServerId = BaseDTO.INT_NULL_VALUE;
    protected int mPlaceServerId = BaseDTO.INT_NULL_VALUE;
    protected int mGraveServerId = BaseDTO.INT_NULL_VALUE;
    protected Date mSyncDate = null;
    private long mSyncDateUNIX = BaseDTO.INT_NULL_VALUE; 
    
    protected Date mLastQueryServerDate = null;
    protected Date mLastResponseHeaderDate = null;
    
    protected final Logger mFileLog = Logger.getLogger(BaseTask.class);

    public BaseTask(AsyncTaskProgressListener pl, AsyncTaskCompleteListener<TaskResult> cb, Context context) {
        callback = cb;
        progressListener = pl;
        init();
    	mainContext = context;
	}
    public BaseTask(AsyncTaskProgressListener pl, AsyncTaskCompleteListener<TaskResult> cb) {
        callback = cb;
        progressListener = pl;
        init();
    }
    
    public TaskResult getTaskResult(){
    	return mTaskResult;
    }
    
    public String getTaskName(){
    	return this.mTaskName;
    }

    /**
     * Переопределяемый метод для инициализации задачи. Вызывается из конструктора после основной инициалиции.
     */
    abstract void init();

    /**
     * Получить контекст.
     * @return Контекст переданный в конструктор.
     */
    public Context getContext(){
    	return mainContext;
    }

    @Override
    protected void onProgressUpdate(String... messages) {
		if(progressListener!=null) progressListener.onProgressUpdate(messages);
    }

    @Override
    protected void onPostExecute(TaskResult result) {
    	//Log.i("West", "onPostExecute " + this.hashCode());
    	this.mTaskResult = result;
    	if(callback!=null) callback.onTaskComplete(this, result);
    }
    
    @Override
    protected void onCancelled(TaskResult result){
    	//Log.i("West", "onCancelled " + this.hashCode());    	
    	result.setError(true);
    	result.setStatus(TaskResult.Status.CANCEL_TASK);
    	this.mTaskResult = result;
    	if(callback!=null) callback.onTaskComplete(this, result);
    }
    
    public Date parseDate(String strDate){
        Date resultDate = null;
        if(!TextUtils.isEmpty(strDate)){
            try {
                resultDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(strDate);
            } catch (ParseException e) {
                resultDate = null;
            }
        }
        return resultDate;        
    }
    
    public String serializeDate(Date date){
        String resultStr = null;
        if(date != null){            
            resultStr = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").format(date);           
        }
        return resultStr;        
    }
    
    protected void initGETQueryParameters(String url){
    	List<NameValuePair> parameters;
		try {
			parameters = URLEncodedUtils.parse(new URI(url), Settings.DEFAULT_ENCODING);
			int value = BaseDTO.INT_NULL_VALUE;
			long valueLong = BaseDTO.INT_NULL_VALUE;
			for (NameValuePair p : parameters) {
	    	    if(p.getName().equalsIgnoreCase(ARG_CEMETERY_ID)){
	    	    	value = Integer.parseInt(p.getValue());
	    	    	this.mCemeteryServerId = value;
	    	    }
	    	    if(p.getName().equalsIgnoreCase(ARG_AREA_ID)){
	    	    	value = Integer.parseInt(p.getValue());
	    	    	this.mRegionServerId = value;
	    	    }
	    	    if(p.getName().equalsIgnoreCase(ARG_PLACE_ID)){
	    	    	value = Integer.parseInt(p.getValue());
	    	    	this.mPlaceServerId = value;
	    	    }
	    	    if(p.getName().equalsIgnoreCase(ARG_GRAVE_ID)){
	    	    	value = Integer.parseInt(p.getValue());
	    	    	this.mGraveServerId = value;
	    	    }
	    	    if(p.getName().equalsIgnoreCase(ARG_SYNC_DATE)){
	    	    	valueLong = Long.parseLong(p.getValue());
	    	    	this.mSyncDateUNIX = valueLong;
	    	    	this.mSyncDate = new Date(this.mSyncDateUNIX * 1000L);
	    	    }
	    	}
		} catch (URISyntaxException e) {
			this.mCemeteryServerId = BaseDTO.INT_NULL_VALUE;
		    this.mRegionServerId = BaseDTO.INT_NULL_VALUE;
		    this.mPlaceServerId = BaseDTO.INT_NULL_VALUE;
		    this.mGraveServerId = BaseDTO.INT_NULL_VALUE;
		    this.mSyncDateUNIX = BaseDTO.INT_NULL_VALUE;
		    this.mSyncDate = null;
		}
    	
    }
    
    protected void checkIsCancelTask() throws CancelTaskException{
    	if(this.isCancelled()){
    		throw new CancelTaskException();
    	}
    }
    
    protected void publishUploadProgress(String formatString, TaskResult taskResult){
    	String progress = String.format(formatString, taskResult.getUploadCountSuccess(), taskResult.getUploadCount());
    	publishProgress(progress);
    }
    
    protected boolean uploadFile(String url, MultipartEntity multipartEntity, Context context, StringBuilder outResponseSB) throws AuthorizationException, ServerException{
		HttpClient httpClient = new DefaultHttpClient();
		if(WebHttpsClient.isHttps(url)){
			httpClient = WebHttpsClient.wrapClient(httpClient);
    	}
		HttpPost httpPost = new HttpPost(url);
		HttpParams httpParams = new BasicHttpParams();
    	httpParams.setParameter("http.protocol.handle-redirects", false);
    	httpPost.setParams(httpParams);    	
    	httpPost.addHeader(LoginTask.HEADER_COOKIE, String.format(LoginTask.KEY_PDSESSION + "=%s", Settings.getPDSession()));
    	httpPost.addHeader(HEADER_REFERER, url);
		try {
        	httpPost.setEntity(multipartEntity);
        	HttpResponse httpResponse = null;
        	httpResponse = httpClient.execute(httpPost);
        	int statusCode = httpResponse.getStatusLine().getStatusCode();
            if(statusCode == HttpStatus.SC_OK)
    		{
            	String responseString = EntityUtils.toString(httpResponse.getEntity());
            	outResponseSB.append(responseString);
            	return true;
    		} else if (statusCode == 302){
    			throw new AuthorizationException();
    		} else {
    			ServerException serverException = new ServerException(null, statusCode);
    			throw serverException;
    		}
        }
        catch(FileNotFoundException e){
        	return true;
        }
        catch(IOException exc){
        	ServerException serverException = new ServerException(exc);
        	throw serverException;
        }              
	}
    
    protected String getJSON(String url) throws ClientProtocolException, IOException, AuthorizationException, CancelTaskException{
	   	Date clientTimeBeforeRequest = new Date();
    	HttpUriRequest httpGet = new HttpGet(url);
    	HttpParams httpParams = new BasicHttpParams();
    	httpParams.setParameter("http.protocol.handle-redirects", false);
    	httpGet.setParams(httpParams);
    	httpGet.addHeader(HEADER_REFERER, url);
    	HttpClient client = new DefaultHttpClient();
    	if(WebHttpsClient.isHttps(url)){
    		client = WebHttpsClient.wrapClient(client);
    	}
    	httpGet.addHeader(LoginTask.HEADER_COOKIE, String.format(LoginTask.KEY_PDSESSION + "=%s", Settings.getPDSession()));
        HttpResponse response = client.execute(httpGet);
        if(response.getStatusLine().getStatusCode() == 302){
        	throw new AuthorizationException();
        }
        Header dateHeader = response.getFirstHeader("Date");
        if(dateHeader != null){
        	try {
				this.mLastResponseHeaderDate = DateUtils.parseDate(dateHeader.getValue());
			} catch (DateParseException e) {
				this.mLastResponseHeaderDate = null;
			}
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), Settings.DEFAULT_ENCODING));
        StringBuilder sb = new StringBuilder();
        for (String line = null; (line = reader.readLine()) != null;) {
            sb.append(line);
            checkIsCancelTask();
        }
        Date clientTimeAfterRequest = new Date();
        if(this.mLastResponseHeaderDate != null){
        	this.mLastQueryServerDate = new Date(this.mLastResponseHeaderDate.getTime() - (clientTimeAfterRequest.getTime() - clientTimeBeforeRequest.getTime()));        	
        } else {
        	this.mLastQueryServerDate = null;
        }
        return sb.toString();
    }
    
    public String uploadJSON(String url, String json, String filePostName) throws AuthorizationException, ServerException{		
		MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		File tempFile = null;
		try {
		    tempFile = File.createTempFile("json", null);		    
		    tempFile.deleteOnExit();
		    BufferedWriter out = new BufferedWriter(new FileWriter(tempFile));
		    out.write(json);
		    out.close();
		} 
		catch (IOException e) {
			return null;
		}
		FileBody fileBody = new FileBody(tempFile);
		multipartEntity.addPart(filePostName, fileBody);
		return postHTTPRequest(url, multipartEntity);                     
	}		
	
	private String postHTTPRequest(String url, MultipartEntity multipartEntity) throws AuthorizationException, ServerException{
		HttpClient httpClient = new DefaultHttpClient();
		if(WebHttpsClient.isHttps(url)){
			httpClient = WebHttpsClient.wrapClient(httpClient);
    	}
		HttpPost httpPost = new HttpPost(url);
		HttpParams httpParams = new BasicHttpParams();
    	httpParams.setParameter("http.protocol.handle-redirects", false);
    	httpPost.setParams(httpParams);    	
    	httpPost.addHeader(LoginTask.HEADER_COOKIE, String.format(LoginTask.KEY_PDSESSION + "=%s", Settings.getPDSession()));
    	httpPost.addHeader(HEADER_REFERER, url);
		try {
        	httpPost.setEntity(multipartEntity);
        	HttpResponse httpResponse = null;
        	httpResponse = httpClient.execute(httpPost);
        	int statusCode = httpResponse.getStatusLine().getStatusCode();
            if(statusCode == HttpStatus.SC_OK)
    		{
            	String result = EntityUtils.toString(httpResponse.getEntity());
            	return result;
    		} else if (statusCode == 302){
    			throw new AuthorizationException();
    		} else {
    			//String errorResult = EntityUtils.toString(httpResponse.getEntity());
    			ServerException serverException = new ServerException(null, statusCode);    			
    			throw serverException;
    		}
        }
        catch(IOException exc){
        	ServerException serverException = new ServerException(exc);
        	throw serverException;
        }              
	}
	
	public String postData(String url, Dictionary<String, String> dictPostData) throws AuthorizationException, ServerException{
		MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		for (Enumeration e =  dictPostData.keys(); e.hasMoreElements();){
			Object key = e.nextElement();
			String value = dictPostData.get(key);
			try {
				multipartEntity.addPart(key.toString(), new StringBody(value, Charset.forName(Settings.DEFAULT_ENCODING)));				
			} catch (UnsupportedEncodingException exc) {				
				exc.printStackTrace();
				mFileLog.error(Settings.UNEXPECTED_ERROR_MESSAGE, exc);
			}
		}
		String responseString = null;
		try{
			responseString = postHTTPRequest(url, multipartEntity);
		} catch (ServerException exc){
			if(!exc.isIOException()){
				String errorMessage = url + " " + dictPostData.toString();
				mFileLog.error(errorMessage, exc);
			}
			throw exc;
		}
		 
		return responseString;
	}
	
	private ArrayList<Cemetery> parseCemeteryJSON(String cemeteryJSON) throws Exception{
		JSONTokener tokener = new JSONTokener(cemeteryJSON);
        JSONArray jsonArray = new JSONArray(tokener);
        ArrayList<Cemetery> cemeteryList = new ArrayList<Cemetery>();
        for(int i = 0; i < jsonArray.length(); i++){  
        	checkIsCancelTask();        	
        	JSONObject jsonObj = jsonArray.getJSONObject(i);
        	JSONArray jsonGPSArray = jsonObj.getJSONArray("coordinates");
        	Cemetery cemetery = new Cemetery();
            cemetery.ServerId = jsonObj.getInt("pk");            	
            cemetery.Name = jsonObj.getString("name");
            cemetery.GPSCemeteryList = new ArrayList<GPSCemetery>();            
            for(int j = 0; j < jsonGPSArray.length(); j++){
            	JSONObject jsonGPS = jsonGPSArray.getJSONObject(j);
            	GPSCemetery gps = new GPSCemetery();
            	gps.ServerId = jsonGPS.getInt("pk");            	
            	gps.OrdinalNumber = jsonGPS.getInt("angle_number");
            	gps.Latitude = jsonGPS.getDouble("lat");
            	gps.Longitude = jsonGPS.getDouble("lng");
            	gps.Cemetery = cemetery;
            	cemetery.GPSCemeteryList.add(gps);
            }
            cemeteryList.add(cemetery);
        }        
        return cemeteryList;
	}
	
	protected String createGPSCemeteryJSON(Cemetery cemetery){
        StringBuilder sbJSON = new StringBuilder();
        String delimeter = ", "; 
        sbJSON.append("[");
        String template = "{\"pk\": %s, \"lat\": %s, \"lng\": %s, \"angle_number\": %s}";
        for(GPSCemetery gps : cemetery.GPSCemeteryList){
            String idString, latString, lngString, angleNumberString;
            idString = latString = lngString = angleNumberString = "null";
            if(gps.ServerId > 0){
                idString = Integer.toString(gps.ServerId);
            }            
            angleNumberString = Integer.toString(gps.OrdinalNumber);
            latString = Double.toString(gps.Latitude);
            lngString = Double.toString(gps.Longitude);                        
            sbJSON.append(String.format(template, idString, latString, lngString, angleNumberString));
            sbJSON.append(delimeter);
        }
        if(cemetery.GPSCemeteryList.size() > 0){
            sbJSON.delete(sbJSON.length() - delimeter.length(), sbJSON.length());
        }
        sbJSON.append("]");
        return sbJSON.toString();        
    }
	
	public void handleResponseGetCemeteryJSON(String cemeteryJSON) throws Exception {    	
        ArrayList<Cemetery> cemeteryList = parseCemeteryJSON(cemeteryJSON);
        RuntimeExceptionDao<Cemetery, Integer> dao = DB.dao(Cemetery.class);
        RuntimeExceptionDao<GPSCemetery, Integer> gpsCemeteryDao = DB.dao(GPSCemetery.class);
        for(int i = 0; i < cemeteryList.size(); i++){
        	checkIsCancelTask();
        	Cemetery cemetery = cemeteryList.get(i);        	
			QueryBuilder<Cemetery, Integer> builder = dao.queryBuilder();
			builder.where().eq("ServerId", cemetery.ServerId); 
			List<Cemetery> findedCemeteries = dao.query(builder.prepare());
			Cemetery findedCemetery = null;
			if(findedCemeteries.size() > 0){
				findedCemetery = findedCemeteries.get(0);
				cemetery.Id = findedCemetery.Id;
				cemetery.RegionSyncDate = findedCemetery.RegionSyncDate;
				cemetery.IsGPSChanged = findedCemetery.IsGPSChanged;
				if(findedCemetery.IsChanged == 0){
					dao.createOrUpdate(cemetery);
				}
			} else {
				builder = dao.queryBuilder();
				builder.where().eq("Name", cemetery.Name);
				findedCemeteries = dao.query(builder.prepare());
				if(findedCemeteries.size() > 0){
					findedCemetery = findedCemeteries.get(0);
					cemetery.Id = findedCemetery.Id;
					cemetery.RegionSyncDate = findedCemetery.RegionSyncDate;
					cemetery.IsGPSChanged = findedCemetery.IsGPSChanged;
				}
				dao.createOrUpdate(cemetery);
			}
			
			if(cemetery.IsGPSChanged == 0 && cemetery.GPSCemeteryList != null){
				DeleteBuilder<GPSCemetery, Integer> deleteBuilderGPS = gpsCemeteryDao.deleteBuilder();					
				deleteBuilderGPS.where().eq("Cemetery_id", cemetery.Id);
				gpsCemeteryDao.delete(deleteBuilderGPS.prepare());
				for(GPSCemetery gps : cemetery.GPSCemeteryList){
					gps.Cemetery = cemetery;
					gpsCemeteryDao.create(gps);
				}
			}
			
        }    
	}
	
	public void handleResponseUploadCemeteryJSON(String cemeteryJSON) throws Exception {    	
        ArrayList<Cemetery> cemeteryList = parseCemeteryJSON(cemeteryJSON);
        for(int i = 0; i < cemeteryList.size(); i++){
        	Cemetery cemetery = cemeteryList.get(i);
        	RuntimeExceptionDao<Cemetery, Integer> dao = DB.dao(Cemetery.class);
			QueryBuilder<Cemetery, Integer> builder = dao.queryBuilder();
			builder.where().eq("Name", cemetery.Name);
			List<Cemetery> findedCemeteries = dao.query(builder.prepare());
			if(findedCemeteries.size() > 0){
				cemetery.Id = findedCemeteries.get(0).Id;
				dao.createOrUpdate(cemetery);
			}			
        }    
	}
	
	private ArrayList<Region> parseRegionJSON(String regionJSON) throws Exception {	
		JSONTokener tokener = new JSONTokener(regionJSON);
        JSONArray jsonArray = new JSONArray(tokener);
        ArrayList<Region> regionList = new ArrayList<Region>();        
        for(int i = 0; i < jsonArray.length(); i++){
        	checkIsCancelTask();        	
        	JSONObject jsonObj = jsonArray.getJSONObject(i);
        	JSONArray jsonGPSArray = jsonObj.getJSONArray("coordinates");
        	JSONObject jsonCemetery = jsonObj.getJSONObject("cemetery");        		
            Region region = new Region();
        	region.ServerId = jsonObj.getInt("pk");
        	region.Name = jsonObj.getString("name");
            region.ParentServerId = jsonCemetery.getInt("pk");
            region.Cemetery = new Cemetery();
           	region.Cemetery.ServerId = region.ParentServerId;
           	region.GPSRegionList = new ArrayList<GPSRegion>();           	
           	for(int j = 0; j < jsonGPSArray.length(); j++){
           		JSONObject jsonGPS = jsonGPSArray.getJSONObject(j);
           		GPSRegion gps = new GPSRegion();
            	gps.ServerId = jsonGPS.getInt("pk");            	
            	gps.OrdinalNumber = jsonGPS.getInt("angle_number");
            	gps.Latitude = jsonGPS.getDouble("lat");
            	gps.Longitude = jsonGPS.getDouble("lng");
            	gps.Region = region;
            	region.GPSRegionList.add(gps);            	
           	}
           	regionList.add(region);        	
        }        
        return regionList;
	}
	
	protected String createGPSRegionJSON(Region region){
        StringBuilder sbJSON = new StringBuilder();
        String delimeter = ", "; 
        sbJSON.append("[");
        String template = "{\"pk\": %s, \"lat\": %s, \"lng\": %s, \"angle_number\": %s}";        
        for(GPSRegion gps : region.GPSRegionList){
            String idString, latString, lngString, angleNumberString;
            idString = latString = lngString = angleNumberString = "null";
            if(gps.ServerId > 0){
                idString = Integer.toString(gps.ServerId);
            }            
            angleNumberString = Integer.toString(gps.OrdinalNumber);
            latString = Double.toString(gps.Latitude);
            lngString = Double.toString(gps.Longitude);                        
            sbJSON.append(String.format(template, idString, latString, lngString, angleNumberString));
            sbJSON.append(delimeter);
        }
        if(region.GPSRegionList.size() > 0){
            sbJSON.delete(sbJSON.length() - delimeter.length(), sbJSON.length());
        }
        sbJSON.append("]");
        return sbJSON.toString();        
    }
	
	public void handleResponseGetRegionJSON(String regionJSON, int cemeteryServerId, Date syncDate) throws Exception {	
		ArrayList<Region> regionList = parseRegionJSON(regionJSON);
		RuntimeExceptionDao<Region, Integer> dao = DB.dao(Region.class);
		RuntimeExceptionDao<GPSRegion, Integer> gpsRegionDao = DB.dao(GPSRegion.class);
        for(int i = 0; i < regionList.size(); i++){        	
        	checkIsCancelTask();
        	Region region = regionList.get(i);
        	region.Cemetery = null;        	
        	QueryBuilder<Region, Integer> builder = dao.queryBuilder();
			builder.where().eq("ServerId", region.ServerId); 
			List<Region> findedRegions = dao.query(builder.prepare());
			if(findedRegions.size() == 0){
				RuntimeExceptionDao<Cemetery, Integer> cemeteryDao = DB.dao(Cemetery.class);
	        	QueryBuilder<Cemetery, Integer> cemeteryBuilder = cemeteryDao.queryBuilder();
	        	cemeteryBuilder.where().eq("ServerId", region.ParentServerId);
	        	List<Cemetery> findedCemetery = cemeteryDao.query(cemeteryBuilder.prepare());
	        	if(findedCemetery.size() == 1){
	        		Cemetery parentCemetery = findedCemetery.get(0);
	        		builder = dao.queryBuilder();
	        		builder.where().eq("Cemetery_id", parentCemetery.Id).and().eq("Name", region.Name);
	        		findedRegions = dao.query(builder.prepare());
	        	}
			}
			Region findedRegion = null;
			if(findedRegions.size() > 0){
				findedRegion = findedRegions.get(0);
				region.Id = findedRegion.Id;
				region.PlaceSyncDate = findedRegion.PlaceSyncDate;
				region.GraveSyncDate = findedRegion.GraveSyncDate;
				region.BurialSyncDate = findedRegion.BurialSyncDate;
				region.IsGPSChanged = findedRegion.IsGPSChanged;				
				if(findedRegion.IsChanged == 0){
					dao.createOrUpdate(region);
				}
			} else {
				dao.createOrUpdate(region);
			}
			
			if(region.IsGPSChanged == 0 && region.GPSRegionList != null){
				DeleteBuilder<GPSRegion, Integer> deleteBuilderGPS = gpsRegionDao.deleteBuilder();					
				deleteBuilderGPS.where().eq("Region_id", region.Id);
				gpsRegionDao.delete(deleteBuilderGPS.prepare());
				for(GPSRegion gps : region.GPSRegionList){
					gps.Region = region;
					gpsRegionDao.create(gps);
				}
			}
			
        }
        if(syncDate != null && cemeteryServerId > 0){
        	List<Cemetery> findedCemeteryList = DB.dao(Cemetery.class).queryForEq("ServerId", cemeteryServerId);
        	for(Cemetery cem : findedCemeteryList){
        		cem.RegionSyncDate = syncDate;
        		DB.dao(Cemetery.class).update(cem);
        	}
        }        
	}
	
	public void handleResponseUploadRegionJSON(String regionJSON) throws Exception {	
		ArrayList<Region> regionList = parseRegionJSON(regionJSON);
        for(int i = 0; i < regionList.size(); i++){
        	Region region = regionList.get(i);
        	region.Cemetery = null;
        	RuntimeExceptionDao<Cemetery, Integer> cemeteryDAO = DB.dao(Cemetery.class);
        	QueryBuilder<Cemetery, Integer> cemeteryBuilder = cemeteryDAO.queryBuilder();
			cemeteryBuilder.where().eq("ServerId", region.ParentServerId);
			List<Cemetery> findedCemetery = cemeteryDAO.query(cemeteryBuilder.prepare());
			if(findedCemetery.size() > 0){
				region.Cemetery = findedCemetery.get(0);
				RuntimeExceptionDao<Region, Integer> dao = DB.dao(Region.class);
	        	QueryBuilder<Region, Integer> builder = dao.queryBuilder();
				builder.where().eq("Cemetery_id", region.Cemetery.Id).and().eq("Name", region.Name);
				List<Region> findedRegions = dao.query(builder.prepare());
				if(findedRegions.size() > 0){
					region.Id = findedRegions.get(0).Id;
					dao.createOrUpdate(region);
				}			
			}
        }
	}
	
	private ArrayList<Place> parsePlaceJSON(String placeJSON) throws Exception {	
		JSONTokener tokener = new JSONTokener(placeJSON);
        JSONArray jsonArray = new JSONArray(tokener);
        ArrayList<Place> placeList = new ArrayList<Place>();       
        for(int i = 0; i < jsonArray.length(); i++){
        	checkIsCancelTask();        	
        	JSONObject jsonObj = jsonArray.getJSONObject(i);
        	JSONObject jsonArea = jsonObj.getJSONObject("area");
        	JSONObject jsonResponsibleUser = null;
        	if(!jsonObj.isNull("responsible")){
        		jsonResponsibleUser = jsonObj.getJSONObject("responsible");
        	}        	
        	JSONObject jsonAddress = null;
        	JSONObject jsonCountry = null;
        	JSONObject jsonRegion = null;
        	JSONObject jsonCity = null;
        	JSONObject jsonStreet = null;
        	if(jsonResponsibleUser != null){
        		if(!jsonResponsibleUser.isNull("address")){
        			jsonAddress = jsonResponsibleUser.getJSONObject("address");
        			if(jsonAddress != null){
        				if(!jsonAddress.isNull("country")){
        					jsonCountry = jsonAddress.getJSONObject("country");
        				}
        				if(!jsonAddress.isNull("region")){
        					jsonRegion = jsonAddress.getJSONObject("region");
        				}
        				if(!jsonAddress.isNull("city")){
        					jsonCity = jsonAddress.getJSONObject("city");
        				}
        				if(!jsonAddress.isNull("street")){
        					jsonStreet = jsonAddress.getJSONObject("street");
        				}

        			}
            	} 
        	}        	
        	Place place = new Place();
        	place.ServerId = jsonObj.getInt("pk");
        	place.Name = jsonObj.getString("place");
        	place.OldName = jsonObj.getString("oldplace");
        	String rowName = jsonObj.getString("row");
        	if((place.OldName != null) && (place.OldName.equalsIgnoreCase("null"))){
        		place.OldName = null;
        	}
        	String strLength = jsonObj.getString("place_length");
        	String strWidth = jsonObj.getString("place_width");
        	if(strLength != null && !strLength.equalsIgnoreCase("null")) {
        		place.Length = Double.parseDouble(strLength);
        	}
        	if(strWidth != null && !strWidth.equalsIgnoreCase("null")) {
        		place.Width = Double.parseDouble(strWidth);
        	}        	
        	String strDateWrongFIO = jsonObj.getString("dt_wrong_fio");
        	String strDateMilitary = jsonObj.getString("dt_military");
        	String strDateSizeViolated = jsonObj.getString("dt_size_violated");
        	String strDateUnowned = jsonObj.getString("dt_unowned");
        	String strDateUnindentified = jsonObj.getString("dt_unindentified");
        	if(strDateWrongFIO != null && !strDateWrongFIO.equalsIgnoreCase("null")) {
                place.WrongFIODate = parseDate(strDateWrongFIO);
            }
        	if(strDateMilitary != null && !strDateMilitary.equalsIgnoreCase("null")) {
                place.MilitaryDate = parseDate(strDateMilitary);
            }
        	if(strDateSizeViolated != null && !strDateSizeViolated.equalsIgnoreCase("null")) {
                place.SizeViolatedDate = parseDate(strDateSizeViolated);
            }
        	if(strDateUnowned != null && !strDateUnowned.equalsIgnoreCase("null")) {
                place.UnownedDate = parseDate(strDateUnowned);
                place.IsOwnerLess = true;
            } else {
                place.IsOwnerLess = false;
            }
        	if(strDateUnindentified != null && !strDateUnindentified.equalsIgnoreCase("null")) {
                place.UnindentifiedDate = parseDate(strDateUnindentified);
            }        	
        	int regionServerId = jsonArea.getInt("pk");        	
        	if(rowName == null || rowName.equalsIgnoreCase("") ){
        		place.ParentServerId = regionServerId;
        		place.Region = new Region();
        		place.Region.ServerId = regionServerId;
        		place.Row = null;		                		
        	} else {
        		Row row = new Row();
        		row.ParentServerId = regionServerId;
        		row.Name = rowName;
        		row.Region = new Region();
        		row.Region.ServerId = regionServerId;
        		place.Row = row;
        		place.Region = null;
        	}
        	if(jsonResponsibleUser != null){
	        	ResponsibleUser user = new ResponsibleUser();
	        	user.FirstName = jsonResponsibleUser.getString("first_name");
	        	user.LastName = jsonResponsibleUser.getString("last_name");
	        	user.MiddleName = jsonResponsibleUser.getString("middle_name");
	        	user.Phones= jsonResponsibleUser.getString("phones");
	        	user.LoginPhone = jsonResponsibleUser.getString("login_phone");
	        	if(jsonAddress != null){
		        	user.House = jsonAddress.getString("house");
		        	user.Block = jsonAddress.getString("block");
		        	user.Building = jsonAddress.getString("building");
		        	user.Flat = jsonAddress.getString("flat");
		        	if(jsonCountry != null){
		        		user.Country = jsonCountry.getString("name");
		        	}
		        	if(jsonRegion != null){
		        		user.Region = jsonRegion.getString("name");
		        	}
		        	if(jsonCity != null){
		        		user.City = jsonCity.getString("name");
		        	}
		        	if(jsonStreet != null){
		        		user.Street = jsonStreet.getString("name");
		        	}
	        	}
	        	user.FirstName = getStringOrNull(user.FirstName);
	        	user.LastName = getStringOrNull(user.LastName);
	        	user.MiddleName = getStringOrNull(user.MiddleName);
	        	user.Phones = getStringOrNull(user.Phones);
	        	user.LoginPhone = getStringOrNull(user.LoginPhone);
	        	user.House = getStringOrNull(user.House);
	        	user.Block = getStringOrNull(user.Block);
	        	user.Building = getStringOrNull(user.Building);
	        	user.Flat = getStringOrNull(user.Flat);
	        	user.Country = getStringOrNull(user.Country);
	        	user.Region = getStringOrNull(user.Region);
	        	user.City = getStringOrNull(user.City);
	        	user.Street = getStringOrNull(user.Street);     	
	        	place.ResponsibleUser = user;
        	}
        	placeList.add(place);    	
        }
        return placeList;
	}
	
	private String getStringOrNull(String value){
		if(!TextUtils.isEmpty(value) && !value.equalsIgnoreCase("null")) {
			return value;
		}
		return null;
	}
	
	public void handleResponseGetPlaceJSON(String placeJSON, int cemeteryServerId, int regionServerId, Date syncDate) throws Exception {		
		ArrayList<Place> placeList = parsePlaceJSON(placeJSON);
		RuntimeExceptionDao<Place, Integer> placeDAO = DB.dao(Place.class);
        for(int i = 0; i < placeList.size(); i++){
        	checkIsCancelTask();
        	Place place = placeList.get(i);
        	ResponsibleUser responsibleUser = place.ResponsibleUser;
        	RuntimeExceptionDao<Region, Integer> regionDAO = DB.dao(Region.class);
        	RuntimeExceptionDao<Row, Integer> rowDAO = DB.dao(Row.class);
        	if(place.Row != null){
        		place.Row.Region = null;
        	} else {
        		place.Region = null;	                		
        	}	                	
        	QueryBuilder<Place, Integer> builder = placeDAO.queryBuilder();
			builder.where().eq("ServerId", place.ServerId);
			List<Place> findedPlace = placeDAO.query(builder.prepare());
			Place dbPlace = null;
			boolean isChangePlaceOnClient = false;
			if(findedPlace.size() == 1){
				dbPlace = findedPlace.get(0);
				if(dbPlace.IsChanged == 1){
					isChangePlaceOnClient = true;
				} else {
					if(dbPlace.Row != null){
						rowDAO.refresh(dbPlace.Row);
						if(dbPlace.Row.IsChanged == 1){
							isChangePlaceOnClient = true;
						}
					}
				}
			}
			if(!isChangePlaceOnClient){
				if(dbPlace != null){
					//update place
					if(dbPlace.ResponsibleUser == null){
						dbPlace.ResponsibleUser = responsibleUser;
					} else {
						if(responsibleUser == null){
							dbPlace.ResponsibleUser = null;
						} else {
							responsibleUser.Id = dbPlace.ResponsibleUser.Id;
							dbPlace.ResponsibleUser = responsibleUser;
						}
					}
					if(dbPlace.ResponsibleUser != null){
						DB.dao(ResponsibleUser.class).createOrUpdate(dbPlace.ResponsibleUser);
					}
					if(dbPlace.Row != null){
						rowDAO.refresh(dbPlace.Row);
						dbPlace.MilitaryDate = place.MilitaryDate;
						dbPlace.WrongFIODate = place.WrongFIODate;
						dbPlace.UnindentifiedDate = place.UnindentifiedDate;
						dbPlace.UnownedDate = place.UnownedDate;
						dbPlace.SizeViolatedDate = place.SizeViolatedDate;
						if(place.Row != null){
							dbPlace.Row.Region = null;
							dbPlace.Row.ParentServerId = place.Row.ParentServerId;
							dbPlace.Row.Name = place.Row.Name;
							dbPlace.Name = place.Name;
							dbPlace.OldName = place.OldName;
							dbPlace.IsOwnerLess = place.IsOwnerLess;
							rowDAO.update(dbPlace.Row);
							placeDAO.update(dbPlace);
						} else {
							dbPlace.Row = null;
							dbPlace.ParentServerId = place.ParentServerId;
							dbPlace.Name = place.Name;
							dbPlace.OldName = place.OldName;
							dbPlace.IsOwnerLess = place.IsOwnerLess;
							placeDAO.update(dbPlace);
						}
					} else {
						regionDAO.refresh(dbPlace.Region);
						if(place.Row != null){
							dbPlace.Region = null;
							dbPlace.ParentServerId = BaseDTO.INT_NULL_VALUE;
							dbPlace.Name = place.Name;
							dbPlace.OldName = place.OldName;
							dbPlace.IsOwnerLess = place.IsOwnerLess;
							dbPlace.Row = new Row();
							dbPlace.Row.Name = place.Row.Name;
							dbPlace.Row.ParentServerId = place.Row.ParentServerId;
							rowDAO.create(dbPlace.Row);
							placeDAO.update(dbPlace);
						} else {
							dbPlace.Region = null;
							dbPlace.ParentServerId = place.ParentServerId;
							dbPlace.Name = place.Name;
							dbPlace.OldName = place.OldName;
							dbPlace.IsOwnerLess = place.IsOwnerLess;
							placeDAO.update(dbPlace);
						}
					}
				} else{
					//insert place
					if(place.ResponsibleUser != null){
						DB.dao(ResponsibleUser.class).createOrUpdate(place.ResponsibleUser);
					}
					if(place.Row != null){
						//дубликаты рядов создавать не нужно
						Row dbRow = null;
						QueryBuilder<Row, Integer> rowBuilder = rowDAO.queryBuilder();
						rowBuilder.where().eq(BaseDTO.COLUMN_PARENT_SERVER_ID, place.Row.ParentServerId).and().eq(BaseDTO.COLUMN_NAME, place.Row.Name);
						List<Row> findedRow = rowDAO.query(rowBuilder.prepare());
						if(findedRow.size() > 0){
							dbRow = findedRow.get(0);
						} else {
							Region dbRegion = null;
							dbRegion =  regionDAO.queryForEq(BaseDTO.COLUMN_SERVER_ID, place.Row.ParentServerId).get(0);
							rowBuilder = rowDAO.queryBuilder();
							rowBuilder.where().eq("Region_id", dbRegion.Id).and().eq(BaseDTO.COLUMN_NAME, place.Row.Name);
							findedRow = rowDAO.query(rowBuilder.prepare());
							if(findedRow.size() > 0){
								dbRow = findedRow.get(0);
							}							
						}						
						if(dbRow == null){
							rowDAO.create(place.Row);
							placeDAO.create(place);
						} else {
							place.Row = dbRow;
							placeDAO.create(place);
						}		        		
		        		
		        	} else {
		        		placeDAO.create(place);
		        	}	
				}
			}
        }
        
        if(syncDate != null && regionServerId > 0){
        	List<Region> findedRegionList = DB.dao(Region.class).queryForEq("ServerId", regionServerId);
        	for(Region region : findedRegionList){
        		region.PlaceSyncDate = syncDate;
        		DB.dao(Region.class).update(region);
        	}
        }
        
	}
	
	public void handleResponseUploadPlaceJSON(String placeJSON) throws Exception {		
		ArrayList<Place> placeList = parsePlaceJSON(placeJSON);
        for(int i = 0; i < placeList.size(); i++){
        	Place place = placeList.get(i);
        	RuntimeExceptionDao<Place, Integer> placeDAO = DB.dao(Place.class);
        	RuntimeExceptionDao<Region, Integer> regionDAO = DB.dao(Region.class);
        	RuntimeExceptionDao<Row, Integer> rowDAO = DB.dao(Row.class);
        	int regionServerId = - 1;
        	String placeName = null;
        	String rowName = null;
        	Region dbRegion = null;
        	Row dbRow = null;
        	Place dbPlace = null;
        	if(place.Row != null){
        		regionServerId = place.Row.ParentServerId;
        		rowName = place.Row.Name;
        		placeName = place.Name;
        	} else {
        		regionServerId = place.ParentServerId;
        		rowName = null;
        		placeName = place.Name;
        	}
        	QueryBuilder<Region, Integer> regionBuilder = regionDAO.queryBuilder();
			regionBuilder.where().eq("ServerId", regionServerId);
			List<Region> findedRegions = regionDAO.query(regionBuilder.prepare());
			if(findedRegions.size() > 0){
				dbRegion = findedRegions.get(0);
			}
        	
			if(place.Row != null){
				QueryBuilder<Row, Integer> rowBuilder = rowDAO.queryBuilder();
				rowBuilder.where().eq("Region_id", dbRegion.Id).and().eq("Name", rowName);
				List<Row> findedRows = rowDAO.query(rowBuilder.prepare());
				if(findedRows.size() > 0){
					dbRow = findedRows.get(0);
					dbRow.ParentServerId = dbRegion.Id;
					rowDAO.createOrUpdate(dbRow);					
				}
				
				QueryBuilder<Place, Integer> placeBuilder = placeDAO.queryBuilder();
				placeBuilder.where().eq("Row_id", dbRow.Id).and().eq("Name", placeName);
				List<Place> findedPlace = placeDAO.query(placeBuilder.prepare());
				if(findedPlace.size() > 0 ){
					dbPlace = findedPlace.get(0);
					dbPlace.ServerId = place.ServerId;
					placeDAO.createOrUpdate(dbPlace);
				}				
				
			} else {
				QueryBuilder<Place, Integer> placeBuilder = placeDAO.queryBuilder();
				placeBuilder.where().eq("Region_id", dbRegion.Id).and().eq("Name", placeName);
				List<Place> findedPlaces = placeDAO.query(placeBuilder.prepare());
				if(findedPlaces.size() > 0){
					dbPlace = findedPlaces.get(0);
					dbPlace.ServerId = place.ServerId;
					dbPlace.ParentServerId = place.Region.ServerId;
					placeDAO.createOrUpdate(dbPlace);
				}
			}
			
        }
	}
	
	private ArrayList<Grave> parseGraveJSON(String graveJSON) throws Exception {	
		JSONTokener tokener = new JSONTokener(graveJSON);
        JSONArray jsonArray = new JSONArray(tokener);
        ArrayList<Grave> graveList = new ArrayList<Grave>();
        for(int i = 0; i < jsonArray.length(); i++){
        	checkIsCancelTask();
        	Grave grave = new Grave();
        	JSONObject jsonObj = jsonArray.getJSONObject(i);
        	JSONObject jsonPlace = jsonObj.getJSONObject("place");
        	grave.ServerId = jsonObj.getInt("pk");
        	grave.Name = jsonObj.getString("grave_number");
        	grave.Place = new Place();	                	
        	grave.Place.ServerId = jsonPlace.getInt("pk");
        	grave.ParentServerId = jsonPlace.getInt("pk");
        	String isMilitary =  jsonObj.getString("is_military");
        	String isWrongFIO =  jsonObj.getString("is_wrong_fio");
        	if(isMilitary != null && isMilitary.equalsIgnoreCase("true")){
        		grave.IsMilitary = true;
        	} else {
        		grave.IsMilitary = false;
        	}
        	if(isWrongFIO != null && isWrongFIO.equalsIgnoreCase("true")){
        		grave.IsWrongFIO = true;
        	} else {
        		grave.IsWrongFIO = false;
        	}
        	graveList.add(grave);    	
        }
        return graveList;
	}
	
	public void handleResponseGetGraveJSON(String graveJSON, int cemeteryServerId, int regionServerId, Date syncDate) throws Exception {	
		ArrayList<Grave> graveList = parseGraveJSON(graveJSON);                
        for(int i = 0; i < graveList.size(); i++){
        	checkIsCancelTask();        	
        	Grave grave = graveList.get(i);
        	grave.Place = null;
        	RuntimeExceptionDao<Grave, Integer> graveDAO = DB.dao(Grave.class);
        	QueryBuilder<Grave, Integer> builder = graveDAO.queryBuilder();
			builder.where().eq("ServerId", grave.ServerId); 
			List<Grave> findedGraves = graveDAO.query(builder.prepare());
			if(findedGraves.size() == 0){
				//ищем по имени
				RuntimeExceptionDao<Place, Integer> placeDAO = DB.dao(Place.class);
				QueryBuilder<Place, Integer> placeBuilder = placeDAO.queryBuilder();
				placeBuilder.where().eq("ServerId", grave.ParentServerId);
				List<Place> findedPlaces = placeDAO.query(placeBuilder.prepare());
				if(findedPlaces.size() == 1){
					Place parentPlace = findedPlaces.get(0);
					builder = graveDAO.queryBuilder();
					builder.where().eq("Place_id", parentPlace.Id).and().eq("Name", grave.Name);
					findedGraves = graveDAO.query(builder.prepare());
				}
				
			}
			Grave findedGrave = null;
			if(findedGraves.size() > 0){
				findedGrave = findedGraves.get(0);
				grave.Id = findedGraves.get(0).Id;
				boolean isStory = false;
				if(grave.Name != null) {
					if(grave.Name.equals(findedGrave.Name)){
						isStory = true;
					}
				}
				if(findedGrave.IsChanged == 0 || isStory){
					graveDAO.createOrUpdate(grave);
				}
			} else {
				graveDAO.createOrUpdate(grave);
			}
			
        }
        if(syncDate != null && regionServerId > 0){
        	List<Region> findedRegionList = DB.dao(Region.class).queryForEq("ServerId", regionServerId);
        	for(Region region : findedRegionList){
        		region.GraveSyncDate = syncDate;
        		DB.dao(Region.class).update(region);
        	}
        }
	}
	
	public void handleResponseUploadGraveJSON(String graveJSON) throws Exception {	
		ArrayList<Grave> graveList = parseGraveJSON(graveJSON);                
        for(int i = 0; i < graveList.size(); i++){
        	Grave grave = graveList.get(i);
        	Place dbPlace = null;
        	Grave dbGrave = null;
        	RuntimeExceptionDao<Place, Integer> placeDAO = DB.dao(Place.class);
        	RuntimeExceptionDao<Grave, Integer> graveDAO = DB.dao(Grave.class);
        	QueryBuilder<Place, Integer> placeBuilder = placeDAO.queryBuilder();
			placeBuilder.where().eq("ServerId", grave.ParentServerId);
			List<Place> findedPlace = placeDAO.query(placeBuilder.prepare());
			if(findedPlace.size() > 0){
				dbPlace = findedPlace.get(0);
			}
			
        	QueryBuilder<Grave, Integer> graveBuilder = graveDAO.queryBuilder();
			graveBuilder.where().eq("Place_id", dbPlace.Id).and().eq("Name", grave.Name);
			List<Grave> findedGrave = graveDAO.query(graveBuilder.prepare());
			if(findedGrave.size() > 0){
				dbGrave = findedGrave.get(0);
				dbGrave.ServerId = grave.ServerId;
				dbGrave.ParentServerId = grave.ParentServerId;
				graveDAO.createOrUpdate(dbGrave);
			}     			
			
        }
	}
	
	private ArrayList<Burial> parseBurialJSON(String burialJSON) throws Exception {	
		JSONTokener tokener = new JSONTokener(burialJSON);
        JSONArray jsonArray = new JSONArray(tokener);
        ArrayList<Burial> burialList = new ArrayList<Burial>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        for(int i = 0; i < jsonArray.length(); i++){
        	checkIsCancelTask();
        	Burial burial = new Burial();
        	JSONObject jsonObj = jsonArray.getJSONObject(i);
        	burial.ServerId = jsonObj.getInt("pk");        	
        	JSONObject jsonGrave = jsonObj.getJSONObject("grave");
        	JSONObject jsonDeadman = null;
        	if(!jsonObj.isNull("deadman")){
        		jsonDeadman = jsonObj.getJSONObject("deadman");
        	}
        	burial.ParentServerId = jsonGrave.getInt("pk");
        	String containerString = jsonObj.getString("burial_container");
        	try{
        	    burial.ContainerType = Burial.ContainerTypeEnum.getEnum(containerString);
        	} catch(Exception exc){
        	    burial.ContainerType = null;
        	}        	
        	String factDateString = jsonObj.getString("fact_date");
        	factDateString = getStringOrNull(factDateString);
        	try {
        		if(factDateString != null){
        			burial.FactDate = dateFormat.parse(factDateString);
        		}
            } catch (ParseException e) {
                burial.FactDate = null;
            }
        	if(jsonDeadman != null){
        		burial.FName = jsonDeadman.getString("first_name");
        		burial.LName = jsonDeadman.getString("last_name");
        		burial.MName = jsonDeadman.getString("middle_name");
        		burial.FName = getStringOrNull(burial.FName);
        		burial.LName = getStringOrNull(burial.LName);
        		burial.MName = getStringOrNull(burial.MName);
        	}
        	burialList.add(burial);    	
        }
        return burialList;
	}
	
	public void handleResponseGetBurialJSON(String burialJSON, int cemeteryServerId, int regionServerId, Date syncDate) throws Exception {
		ArrayList<Burial> burialList = parseBurialJSON(burialJSON);                
        for(int i = 0; i < burialList.size(); i++){
        	checkIsCancelTask();
        	Burial burial = burialList.get(i);
        	RuntimeExceptionDao<Burial, Integer> burialDAO = DB.dao(Burial.class);
        	DeleteBuilder<Burial, Integer> deleteBuilder = burialDAO.deleteBuilder();
        	deleteBuilder.where().eq("ParentServerId", burial.ParentServerId).and().isNotNull("Grave_id");
        	burialDAO.delete(deleteBuilder.prepare());        	
			burialDAO.create(burial);			
        }        
        
        if(syncDate != null && regionServerId > 0){
        	List<Region> findedRegionList = DB.dao(Region.class).queryForEq("ServerId", regionServerId);
        	for(Region region : findedRegionList){
        		region.BurialSyncDate = syncDate;
        		DB.dao(Region.class).update(region);
        	}
        }        
	}
	
	public ArrayList<GravePhoto> parseGravePhotoJSON(String gravePhotoJSON) throws Exception {	
		JSONTokener tokener = new JSONTokener(gravePhotoJSON);
        JSONArray jsonArray = new JSONArray(tokener);
        ArrayList<GravePhoto> gravePhotoList = new ArrayList<GravePhoto>();
        for(int i = 0; i < jsonArray.length(); i++){
        	checkIsCancelTask();
        	GravePhoto gravePhoto = new GravePhoto();
        	JSONObject jsonObj = jsonArray.getJSONObject(i);
        	JSONObject jsonGrave = jsonObj.getJSONObject("grave");
        	gravePhoto.ServerId = jsonObj.getInt("pk");
        	gravePhoto.ParentServerId = jsonGrave.getInt("pk");
        	gravePhotoList.add(gravePhoto);    	
        }
        return gravePhotoList;
	}
	
	public ArrayList<PlacePhoto> parsePlacePhotoJSON(String placePhotoJSON) throws Exception { 
        JSONTokener tokener = new JSONTokener(placePhotoJSON);
        JSONArray jsonArray = new JSONArray(tokener);
        ArrayList<PlacePhoto> placePhotoList = new ArrayList<PlacePhoto>();
        for(int i = 0; i < jsonArray.length(); i++){
            checkIsCancelTask();
            PlacePhoto placePhoto = new PlacePhoto();
            JSONObject jsonObj = jsonArray.getJSONObject(i);
            JSONObject jsonPlace = jsonObj.getJSONObject("place");
            placePhoto.ServerId = jsonObj.getInt("pk");            
            placePhoto.ParentServerId = jsonPlace.getInt("place");
            placePhotoList.add(placePhoto);
        }
        return placePhotoList;
    }
}