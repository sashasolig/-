package com.coolchoice.monumentphoto.task;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;

import com.coolchoice.monumentphoto.Settings;
import com.coolchoice.monumentphoto.dal.UserDB;
import com.coolchoice.monumentphoto.data.SettingsData;
import com.coolchoice.monumentphoto.data.User;

public class LoginTask extends BaseTask {
	
	public final static String HEADER_SET_COOKIE = "Set-Cookie";
	public final static String HEADER_COOKIE = "Cookie";
	public final static String KEY_CSRFTOKEN = "csrftoken";	
	public final static String KEY_PDSESSION = "pdsession";
	private final static String KEY_CSRFMIDDLETOKEN = "csrfmiddlewaretoken";
	private final static String KEY_USERNAME = "username";
	private final static String KEY_PASSWORD = "password";
	
	private static final int LIVE_TIME_TOKEN_MS = 5 * 60 * 1000; //5 min
	private static Date mLastSuccessLoginDate = null;
	private static String mLastSuccessUrl = null;
	private static String mLastSuccessUserName = null;
			
	private String csrfToken = null;
	
	private String userName = null;
	
	private String password = null;
		
	private SettingsData settingsData = null;
    
    public SettingsData getSettingsData() {
        return this.settingsData;
    }

    public LoginTask(AsyncTaskProgressListener pl, AsyncTaskCompleteListener<TaskResult> cb, Context context) {
		super(pl, cb, context);
		this.mTaskName = Settings.TASK_LOGIN;
	}

    @Override
    public void init() {

    }

    @Override
    protected TaskResult doInBackground(String... params) {
    	TaskResult result = new TaskResult();
    	result.setTaskName(Settings.TASK_LOGIN);
    	result.setStatus(TaskResult.Status.OK);    	
        try {
        	String url = params[0];
        	this.userName = params[1];
            this.password = params[2];
            Date now = new Date();
            if(mLastSuccessLoginDate != null && (this.mLastSuccessLoginDate.getTime() + LIVE_TIME_TOKEN_MS) > now.getTime() &&
            		url.equals(mLastSuccessUrl) && this.userName.equals(mLastSuccessUserName)){
            	result.setError(false);
                result.setStatus(TaskResult.Status.LOGIN_SUCCESSED);
                mLastSuccessLoginDate = now;
                return result;            	
            }
            HttpClient client = new DefaultHttpClient();
            if(WebHttpsClient.isHttps(url)){
                client = WebHttpsClient.wrapClient(client);
            }
            HttpResponse response = null;
            HttpPost httpPost = new HttpPost(url);
            HttpParams httpParams = new BasicHttpParams();
        	httpParams.setParameter("http.protocol.handle-redirects", false);
        	httpPost.setParams(httpParams);
        	httpPost.addHeader(HEADER_REFERER, url);
        	httpPost.addHeader(HEADER_CONTENT_TYPE, HEADER_CONTENT_TYPE_JSON);
            String userDataJSON = String.format("{\"username\": \"%s\", \"password\": \"%s\"}", this.userName, this.password); 
            HttpEntity entity = new StringEntity(userDataJSON, Settings.DEFAULT_ENCODING);             			
			httpPost.setEntity(entity);
			response = client.execute(httpPost);
			int responseStatusCode = response.getStatusLine().getStatusCode();
			if(responseStatusCode == HttpStatus.SC_OK || responseStatusCode == HttpStatus.SC_BAD_REQUEST){
			    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), Settings.DEFAULT_ENCODING));
		        StringBuilder sb = new StringBuilder();
		        for (String line = null; (line = reader.readLine()) != null;) {
		            sb.append(line);		            
		        }
		        String userResponseDataJSON = sb.toString();
		        SettingsData settingsData = parseUserDataJSON(userResponseDataJSON);
		        if(settingsData != null){
		            this.settingsData = settingsData;
		            Settings.setPDSession(this.settingsData.Session);
					Settings.setToken(this.settingsData.Token);					
		            result.setError(false);
	                result.setStatus(TaskResult.Status.LOGIN_SUCCESSED);
	                mLastSuccessLoginDate = new Date();
	                mLastSuccessUrl = url;
	                mLastSuccessUserName = this.userName;
	                User user = new User();
	                user.UserName = this.settingsData.Login;
	                user.LName = this.settingsData.LName;
	                user.MName = this.settingsData.MName;
	                user.FName = this.settingsData.FName;
	                user.OrgId = this.settingsData.OrgId;
	                UserDB.loginUser(user);
		        } else {
		            result.setError(true);
                    result.setStatus(TaskResult.Status.LOGIN_FAILED);                    
		        }
		        
			} else {
			    result.setError(true);
                result.setStatus(TaskResult.Status.SERVER_UNAVALAIBLE);                
			}
        	
        } catch (Exception e) {                
            result.setError(true);
    		result.setStatus(TaskResult.Status.SERVER_UNAVALAIBLE);    		
    	}
        if(result.isError()){
        	UserDB.setAllUsersDisabled();
        	Settings.setPDSession(null);
			Settings.setToken(null);
			mLastSuccessLoginDate = null;
			mLastSuccessUrl = null;
			mLastSuccessUserName = null;
        }
        return result;
    }
    
    private SettingsData parseUserDataJSON(String userDataJSON) throws Exception{
        JSONTokener tokener = new JSONTokener(userDataJSON);
        JSONObject jsonObject = new JSONObject(tokener);
        SettingsData settingData = new SettingsData();        
        String status = jsonObject.getString("status");
        if(status.equalsIgnoreCase("success")){            
            settingData.Token = jsonObject.getString("token");
            settingData.Session = jsonObject.getString("sessionId");            
            JSONObject jsonProfile = jsonObject.getJSONObject("profile");
            JSONObject jsonOrg = jsonObject.getJSONObject("org");
            if(jsonProfile != null){
            	settingData.Login = jsonProfile.getString("username");
                settingData.FName = jsonProfile.getString("firstname");
                settingData.LName = jsonProfile.getString("lastname");
                settingData.MName = jsonProfile.getString("middlename");
                settingData.Email = jsonProfile.getString("email");
                if(settingData.FName != null && settingData.FName.equalsIgnoreCase("null")){
                	settingData.FName = null;
                }
                if(settingData.LName != null && settingData.LName.equalsIgnoreCase("null")){
                	settingData.LName = null;
                }
                if(settingData.MName != null && settingData.MName.equalsIgnoreCase("null")){
                	settingData.MName = null;
                }
                if(settingData.Email != null && settingData.Email.equalsIgnoreCase("null")){
                	settingData.Email = null;
                }
            }
            if(jsonOrg != null){
            	settingData.OrgId = jsonOrg.getInt("id");
            }            
        } else {
            settingData = null;
        }        
        return settingData;
    }
    
    private String getCookieValue(String name, String str){
        String regexp = name + "=([^;]+)[;$]";
        Pattern pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);        
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {            
        	return matcher.group(1);
        }
        return null;
        
    }
    
    
}