package com.coolchoice.monumentphoto.task;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
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
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.coolchoice.monumentphoto.Settings;
import com.coolchoice.monumentphoto.dal.DB;
import com.coolchoice.monumentphoto.data.BaseDTO;
import com.coolchoice.monumentphoto.data.Cemetery;
import com.coolchoice.monumentphoto.data.Grave;
import com.coolchoice.monumentphoto.data.GravePhoto;
import com.coolchoice.monumentphoto.data.Monument;
import com.coolchoice.monumentphoto.data.Place;
import com.coolchoice.monumentphoto.data.Region;
import com.coolchoice.monumentphoto.data.Row;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Базовый класс фоновых задач для вызова из диалоговых окон.
 *
 */
public abstract class BaseTask extends AsyncTask<String, String, TaskResult> {
    AsyncTaskProgressListener progressListener;
    AsyncTaskCompleteListener<TaskResult> callback;
    Context mainContext;
    protected String mTaskName = null;
    public static final String ARG_CEMETERY_ID = "cemeteryId";
    public static final String ARG_AREA_ID = "areaId";
    public static final String ARG_PLACE_ID = "placeId";
    public static final String ARG_GRAVE_ID = "graveId";

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
    	if(callback!=null) callback.onTaskComplete(this, result);
    }
    
    @Override
    protected void onCancelled(){
    	TaskResult result = new TaskResult();
    	result.setError(true);
    	result.setStatus(TaskResult.Status.CANCEL_TASK);
    	if(callback!=null) callback.onTaskComplete(this, result);
    }
    
    private void checkIsCancelTask() throws CancelTaskException{
    	if(this.isCancelled()){
    		throw new CancelTaskException();
    	}
    }
    
    protected String getJSON(String url) throws ClientProtocolException, IOException, AuthorizationException{
    	HttpUriRequest httpGet = new HttpGet(url);
    	HttpParams httpParams = new BasicHttpParams();
    	httpParams.setParameter("http.protocol.handle-redirects", false);
    	httpGet.setParams(httpParams);
    	HttpClient client = new DefaultHttpClient();
    	httpGet.addHeader(LoginTask.HEADER_COOKIE, String.format(LoginTask.KEY_PDSESSION + "=%s", Settings.getPDSession()));
        HttpResponse response = client.execute(httpGet);
        if(response.getStatusLine().getStatusCode() == 302){
        	throw new AuthorizationException();
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), Settings.DEFAULT_ENCODING));
        StringBuilder sb = new StringBuilder();
        for (String line = null; (line = reader.readLine()) != null;) {
            sb.append(line).append("\n");
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
            	/*HttpEntity entity = httpResponse.getEntity();
            	String result = EntityUtils.toString(entity);*/
            	String result = EntityUtils.toString(httpResponse.getEntity());
            	return result;
    		} else if (statusCode == 302){
    			throw new AuthorizationException();
    		} else {
    			throw new ServerException();
    		}
        }
        catch(IOException exc){
        	throw new ServerException();
        }              
	}
	
	public String postData(String url, Dictionary<String, String> dictPostData) throws AuthorizationException, ServerException{
		MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		for (Enumeration e =  dictPostData.keys(); e.hasMoreElements();){
			Object key = e.nextElement();
			String value = dictPostData.get(key);
			try {
				multipartEntity.addPart(key.toString(), new StringBody(value, Charset.forName("UTF-8")));				
			} catch (UnsupportedEncodingException exc) {				
				exc.printStackTrace();
				return null;
			}
		}
		String responseString = postHTTPRequest(url, multipartEntity);
		return responseString;
	}
	
	private ArrayList<Cemetery> parseCemeteryJSON(String cemeteryJSON) throws Exception{
		JSONTokener tokener = new JSONTokener(cemeteryJSON);
        JSONArray jsonArray = new JSONArray(tokener);
        ArrayList<Cemetery> cemeteryList = new ArrayList<Cemetery>();
        for(int i = 0; i < jsonArray.length(); i++){  
        	checkIsCancelTask();
        	Cemetery cemetery = new Cemetery();
        	JSONObject jsonObj = jsonArray.getJSONObject(i);
        	cemetery.ServerId = jsonObj.getInt("pk");
        	JSONObject fields = jsonObj.getJSONObject("fields");
        	cemetery.Name = fields.getString("name");
        	cemeteryList.add(cemetery);
        }
        return cemeteryList;
	}
	
	public void handleResponseGetCemeteryJSON(String cemeteryJSON) throws Exception {    	
        ArrayList<Cemetery> cemeteryList = parseCemeteryJSON(cemeteryJSON);
        for(int i = 0; i < cemeteryList.size(); i++){
        	checkIsCancelTask();
        	Cemetery cemetery = cemeteryList.get(i);
        	RuntimeExceptionDao<Cemetery, Integer> dao = DB.dao(Cemetery.class);
			QueryBuilder<Cemetery, Integer> builder = dao.queryBuilder();
			builder.where().eq("ServerId", cemetery.ServerId); //.and().eq("Name", cemetery.Name);
			List<Cemetery> findedCemeteries = dao.query(builder.prepare());
			if(findedCemeteries.size() == 1){
				cemetery.Id = findedCemeteries.get(0).Id;
			}
			dao.createOrUpdate(cemetery);
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
        	Region region = new Region();	                	
        	JSONObject jsonObj = jsonArray.getJSONObject(i);
        	region.ServerId = jsonObj.getInt("pk");
        	JSONObject fields = jsonObj.getJSONObject("fields");
        	region.Name = fields.getString("name");
        	region.ParentServerId = fields.getInt("cemetery");
        	region.Cemetery = new Cemetery();
        	region.Cemetery.ServerId = region.ParentServerId;
        	regionList.add(region);
        }
        return regionList;
	}
	
	public void handleResponseGetRegionJSON(String regionJSON) throws Exception {	
		ArrayList<Region> regionList = parseRegionJSON(regionJSON);
        for(int i = 0; i < regionList.size(); i++){
        	checkIsCancelTask();
        	Region region = regionList.get(i);
        	region.Cemetery = null;
        	RuntimeExceptionDao<Region, Integer> dao = DB.dao(Region.class);
        	QueryBuilder<Region, Integer> builder = dao.queryBuilder();
			builder.where().eq("ServerId", region.ServerId); //.and().eq("Name", region.Name);
			List<Region> findedRegions = dao.query(builder.prepare());
			if(findedRegions.size() == 1){
				region.Id = findedRegions.get(0).Id;
			}
			dao.createOrUpdate(region);
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
	
	private ArrayList<Place> parsePlaceJSON(String placeJSON, ArrayList<Integer> unownedPlaceServerIdList) throws Exception {	
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
        		unownedPlaceServerIdList.add(placeServerId);
        		continue;
        	}
        	Place place = new Place();
        	place.ServerId = jsonObj.getInt("pk");
        	JSONObject fields = jsonObj.getJSONObject("fields");
        	place.Name = fields.getString("place");
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
	
	public void handleResponseGetPlaceJSON(String placeJSON) throws Exception {
		ArrayList<Integer> unownedPlaceServerIdList = new ArrayList<Integer>();
		ArrayList<Place> placeList = parsePlaceJSON(placeJSON, unownedPlaceServerIdList);
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
			if(findedPlace.size() == 1){
				dbPlace = findedPlace.get(0);
			}
			if(dbPlace != null){
				//update place
				if(dbPlace.Row != null){
					rowDAO.refresh(dbPlace.Row);
					if(place.Row != null){
						dbPlace.Row.Region = null;
						dbPlace.Row.ParentServerId = place.Row.ParentServerId;
						dbPlace.Row.Name = place.Row.Name;
						dbPlace.Name = place.Name;
						dbPlace.IsOwnerLess = place.IsOwnerLess;
						rowDAO.update(dbPlace.Row);
						placeDAO.update(dbPlace);
					} else {
						dbPlace.Row = null;
						dbPlace.ParentServerId = place.ParentServerId;
						dbPlace.Name = place.Name;
						dbPlace.IsOwnerLess = place.IsOwnerLess;
						placeDAO.update(dbPlace);
					}
				} else {
					regionDAO.refresh(dbPlace.Region);
					if(place.Row != null){
						dbPlace.Region = null;
						dbPlace.ParentServerId = BaseDTO.INT_NULL_VALUE;
						dbPlace.Name = place.Name;
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
						dbPlace.IsOwnerLess = place.IsOwnerLess;
						placeDAO.update(dbPlace);
					}
				}
			} else{
				//insert place
				if(place.Row != null){
	        		rowDAO.create(place.Row);
	        		placeDAO.create(place);
	        	} else {
	        		placeDAO.create(place);
	        	}	
			}        		        			
        }
        
        for(int placeServerId : unownedPlaceServerIdList){
        	UpdateBuilder<Place, Integer> updateBuilder = placeDAO.updateBuilder();
    		updateBuilder.updateColumnValue(Place.IsOwnerLessColumnName, true);
    		updateBuilder.where().eq("ServerId", placeServerId);
    		placeDAO.update(updateBuilder.prepare());
        }
	}
	
	public void handleResponseUploadPlaceJSON(String placeJSON) throws Exception {
		ArrayList<Integer> unownedPlaceServerIdList = new ArrayList<Integer>();
		ArrayList<Place> placeList = parsePlaceJSON(placeJSON, unownedPlaceServerIdList);
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
        	graveList.add(grave);    	
        }
        return graveList;
	}
	
	public void handleResponseGetGraveJSON(String graveJSON) throws Exception {	
		ArrayList<Grave> graveList = parseGraveJSON(graveJSON);                
        for(int i = 0; i < graveList.size(); i++){
        	checkIsCancelTask();
        	Grave grave = graveList.get(i);
        	grave.Place = null;
        	RuntimeExceptionDao<Grave, Integer> graveDAO = DB.dao(Grave.class);
        	QueryBuilder<Grave, Integer> builder = graveDAO.queryBuilder();
			builder.where().eq("ServerId", grave.ServerId); //.and().eq("Name", grave.Name);
			List<Grave> findedGrave = graveDAO.query(builder.prepare());
			if(findedGrave.size() == 1){
				grave.Id = findedGrave.get(0).Id;
			}     			
			graveDAO.createOrUpdate(grave);
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
}