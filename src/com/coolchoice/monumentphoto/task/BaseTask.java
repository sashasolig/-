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
import com.coolchoice.monumentphoto.data.Region;
import com.coolchoice.monumentphoto.data.Row;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;

import android.content.Context;
import android.os.AsyncTask;
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
        HashMap<Integer, List<GPSCemetery>> hashMapGPS = new HashMap<Integer, List<GPSCemetery>>();
        for(int i = 0; i < jsonArray.length(); i++){  
        	checkIsCancelTask();        	
        	JSONObject jsonObj = jsonArray.getJSONObject(i);
        	JSONObject fields = jsonObj.getJSONObject("fields");
        	String modelName = jsonObj.getString("model");
        	if(modelName.equalsIgnoreCase("burials.cemetery")){
        		Cemetery cemetery = new Cemetery();
            	cemetery.ServerId = jsonObj.getInt("pk");            	
            	cemetery.Name = fields.getString("name");
            	cemeteryList.add(cemetery);
        	}
        	if(modelName.equalsIgnoreCase("burials.cemeterycoordinates")){
        		GPSCemetery gps = new GPSCemetery();
            	gps.ServerId = jsonObj.getInt("pk");            	
            	gps.OrdinalNumber = fields.getInt("angle_number");
            	gps.Latitude = fields.getDouble("lat");
            	gps.Longitude = fields.getDouble("lng");
            	int cemeteryServerId = fields.getInt("cemetery");
            	if(hashMapGPS.containsKey(cemeteryServerId)){
            		hashMapGPS.get(cemeteryServerId).add(gps);
            	} else {
            		List<GPSCemetery> listGPS = new ArrayList<GPSCemetery>();
            		listGPS.add(gps);
            		hashMapGPS.put(cemeteryServerId, listGPS);
            	}
        	}
        }
        for(Cemetery cem : cemeteryList){
        	if(hashMapGPS.containsKey(cem.ServerId)){
        		cem.GPSCemeteryList = hashMapGPS.get(cem.ServerId);
        	}
        }
        return cemeteryList;
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
        HashMap<Integer, List<GPSRegion>> hashMapGPS = new HashMap<Integer, List<GPSRegion>>();
        for(int i = 0; i < jsonArray.length(); i++){
        	checkIsCancelTask();        	
        	JSONObject jsonObj = jsonArray.getJSONObject(i);
        	JSONObject fields = jsonObj.getJSONObject("fields");
        	String modelName = jsonObj.getString("model");
        	if(modelName.equalsIgnoreCase("burials.area")){
        		Region region = new Region();
        		region.ServerId = jsonObj.getInt("pk");
        		region.Name = fields.getString("name");
            	region.ParentServerId = fields.getInt("cemetery");
            	region.Cemetery = new Cemetery();
            	region.Cemetery.ServerId = region.ParentServerId;
            	regionList.add(region);
        	}
        	if(modelName.equalsIgnoreCase("burials.areacoordinates")){
        		GPSRegion gps = new GPSRegion();
            	gps.ServerId = jsonObj.getInt("pk");            	
            	gps.OrdinalNumber = fields.getInt("angle_number");
            	gps.Latitude = fields.getDouble("lat");
            	gps.Longitude = fields.getDouble("lng");
            	int regionServerId = fields.getInt("area");
            	if(hashMapGPS.containsKey(regionServerId)){
            		hashMapGPS.get(regionServerId).add(gps);
            	} else {
            		List<GPSRegion> listGPS = new ArrayList<GPSRegion>();
            		listGPS.add(gps);
            		hashMapGPS.put(regionServerId, listGPS);
            	}
        	}
        }
        for(Region reg : regionList){
        	if(hashMapGPS.containsKey(reg.ServerId)){
        		reg.GPSRegionList = hashMapGPS.get(reg.ServerId);
        	}
        }
        return regionList;
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
	
	private ArrayList<Place> parsePlaceJSON(String placeJSON, ArrayList<Integer> unownedPlaceServerIdList, ArrayList<Integer> ownedPlaceServerIdList) throws Exception {	
		JSONTokener tokener = new JSONTokener(placeJSON);
        JSONArray jsonArray = new JSONArray(tokener);
        ArrayList<Place> placeList = new ArrayList<Place>();
        String modelName;
        for(int i = 0; i < jsonArray.length(); i++){
        	checkIsCancelTask();        	
        	JSONObject jsonObj = jsonArray.getJSONObject(i);
        	modelName = jsonObj.getString("model");
        	if(modelName.equalsIgnoreCase("burials.placestatus")){
        		JSONObject fieldsPlaceStatus = jsonObj.getJSONObject("fields");
        		int placeServerId = fieldsPlaceStatus.getInt("place");
        		String status = fieldsPlaceStatus.getString("status");
        		if((status != null) && status.equalsIgnoreCase("found-unowned")){
        			unownedPlaceServerIdList.add(placeServerId);
        		} else {
        			ownedPlaceServerIdList.add(placeServerId);
        		}
        		continue;
        	}
        	Place place = new Place();
        	place.ServerId = jsonObj.getInt("pk");
        	JSONObject fields = jsonObj.getJSONObject("fields");
        	place.Name = fields.getString("place");
        	place.OldName = fields.getString("oldplace");
        	if((place.OldName != null) && (place.OldName.equalsIgnoreCase("null"))){
        		place.OldName = null;
        	}
        	String strLength = fields.getString("place_length");
        	String strWidth = fields.getString("place_width");
        	if(strLength != null && !strLength.equalsIgnoreCase("null")) {
        		place.Length = Double.parseDouble(strLength);
        	}
        	if(strWidth != null && !strWidth.equalsIgnoreCase("null")) {
        		place.Width = Double.parseDouble(strWidth);
        	}
        	int regionServerId = fields.getInt("area");
        	String rowName = fields.getString("row");
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
        	place.IsOwnerLess = false;
        	placeList.add(place);    	
        }
        return placeList;
	}
	
	public void handleResponseGetPlaceJSON(String placeJSON, int cemeteryServerId, int regionServerId, Date syncDate) throws Exception {
		ArrayList<Integer> unownedPlaceServerIdList = new ArrayList<Integer>();
		ArrayList<Integer> ownedPlaceServerIdList = new ArrayList<Integer>();
		ArrayList<Place> placeList = parsePlaceJSON(placeJSON, unownedPlaceServerIdList, ownedPlaceServerIdList);
		RuntimeExceptionDao<Place, Integer> placeDAO = DB.dao(Place.class);
        for(int i = 0; i < placeList.size(); i++){
        	checkIsCancelTask();
        	Place place = placeList.get(i);        	
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
					if(dbPlace.Row != null){
						rowDAO.refresh(dbPlace.Row);
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
        
        for(int placeServerId : unownedPlaceServerIdList){
        	UpdateBuilder<Place, Integer> updateBuilder = placeDAO.updateBuilder();
    		updateBuilder.updateColumnValue(Place.IsOwnerLessColumnName, true);
    		updateBuilder.where().eq("ServerId", placeServerId);
    		placeDAO.update(updateBuilder.prepare());
        }
        for(int placeServerId : ownedPlaceServerIdList){
        	UpdateBuilder<Place, Integer> updateBuilder = placeDAO.updateBuilder();
    		updateBuilder.updateColumnValue(Place.IsOwnerLessColumnName, false);
    		updateBuilder.where().eq("ServerId", placeServerId);
    		placeDAO.update(updateBuilder.prepare());
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
		ArrayList<Integer> unownedPlaceServerIdList = new ArrayList<Integer>();
		ArrayList<Integer> ownedPlaceServerIdList = new ArrayList<Integer>();
		ArrayList<Place> placeList = parsePlaceJSON(placeJSON, unownedPlaceServerIdList, ownedPlaceServerIdList);
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
        	grave.ServerId = jsonObj.getInt("pk");
        	JSONObject fields = jsonObj.getJSONObject("fields");
        	grave.Name = fields.getString("grave_number");
        	grave.Place = new Place();	                	
        	grave.Place.ServerId = fields.getInt("place");
        	grave.ParentServerId = fields.getInt("place");
        	String isMilitary =  fields.getString("is_military");
        	String isWrongFIO =  fields.getString("is_wrong_fio");
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
	
	private ArrayList<Burial> parseBurialJSON(String burialJSON, ArrayList<Burial> persons) throws Exception {	
		JSONTokener tokener = new JSONTokener(burialJSON);
        JSONArray jsonArray = new JSONArray(tokener);
        ArrayList<Burial> burialList = new ArrayList<Burial>();
        String modelName;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        for(int i = 0; i < jsonArray.length(); i++){
        	checkIsCancelTask();
        	Burial burial = new Burial();
        	JSONObject jsonObj = jsonArray.getJSONObject(i);
        	modelName = jsonObj.getString("model");
        	if(modelName.equalsIgnoreCase("persons.baseperson")){
        		burial.DeadManId = jsonObj.getInt("pk");
        		JSONObject fieldsPlaceStatus = jsonObj.getJSONObject("fields");
        		burial.FName = fieldsPlaceStatus.getString("first_name");
        		burial.LName = fieldsPlaceStatus.getString("last_name");
        		burial.MName = fieldsPlaceStatus.getString("middle_name");
        		persons.add(burial);
        		continue;
        	}
        	burial.ServerId = jsonObj.getInt("pk");
        	JSONObject fields = jsonObj.getJSONObject("fields");
        	burial.ParentServerId = fields.getInt("grave");
        	try {
        		burial.DeadManId = fields.getInt("deadman");
        	}catch(Exception exc){
        		burial.DeadManId = BaseDTO.INT_NULL_VALUE;
        	}
        	String factDateString = fields.getString("fact_date");
        	try {
                burial.FactDate = dateFormat.parse(factDateString);
            } catch (ParseException e) {
                e.printStackTrace();
            }        	
        	burialList.add(burial);    	
        }
        return burialList;
	}
	
	public void handleResponseGetBurialJSON(String burialJSON, int cemeteryServerId, int regionServerId, Date syncDate) throws Exception {
		ArrayList<Burial> persons = new ArrayList<Burial>();
		ArrayList<Burial> burialList = parseBurialJSON(burialJSON, persons);                
        for(int i = 0; i < burialList.size(); i++){
        	checkIsCancelTask();
        	Burial burial = burialList.get(i);
        	RuntimeExceptionDao<Burial, Integer> burialDAO = DB.dao(Burial.class);
        	DeleteBuilder<Burial, Integer> deleteBuilder = burialDAO.deleteBuilder();
        	deleteBuilder.where().eq("ParentServerId", burial.ParentServerId).and().isNotNull("Grave_id");
        	burialDAO.delete(deleteBuilder.prepare());
        	
			burialDAO.create(burial);			
        }
        for(int i = 0; i < persons.size(); i++){
        	checkIsCancelTask();
        	Burial person = persons.get(i);
        	RuntimeExceptionDao<Burial, Integer> burialDAO = DB.dao(Burial.class);
        	QueryBuilder<Burial, Integer> builder = burialDAO.queryBuilder();
        	builder.where().eq("DeadManId", person.DeadManId);
        	List<Burial> findedBurials = burialDAO.query(builder.prepare());
        	for(int j = 0; j < findedBurials.size(); j++){
        		Burial burial = findedBurials.get(j);
        		burial.FName = person.FName;
        		burial.LName = person.LName;
        		burial.MName = person.MName;
        		burial.toLowerCaseFIO();
        		burialDAO.update(burial);
        	}
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
        	gravePhoto.ServerId = jsonObj.getInt("pk");
        	JSONObject fields = jsonObj.getJSONObject("fields");
        	gravePhoto.ParentServerId = fields.getInt("grave");
        	gravePhotoList.add(gravePhoto);    	
        }
        return gravePhotoList;
	}
}