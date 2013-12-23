package com.coolchoice.monumentphoto.task;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
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

import com.coolchoice.monumentphoto.Settings;

import android.content.Context;
import android.os.SystemClock;


public class LoginTask extends BaseTask {
	
	public final static String HEADER_SET_COOKIE = "Set-Cookie";
	public final static String HEADER_COOKIE = "Cookie";
	public final static String KEY_CSRFTOKEN = "csrftoken";	
	public final static String KEY_PDSESSION = "pdsession";
	private final static String KEY_CSRFMIDDLETOKEN = "csrfmiddlewaretoken";
	private final static String KEY_USERNAME = "username";
	private final static String KEY_PASSWORD = "password";
	
	private String pdSession = null;
	
	private String csrfToken = null;
	
	private String userName = null;
	
	private String password = null;
	
	public String getPDSession(){
		return this.pdSession;
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
    	if (params.length == 3) {
            try {
            	String url = params[0];
            	HttpUriRequest httpGet = new HttpGet(params[0]);
            	this.userName = params[1];
            	this.password = params[2];
            	HttpParams httpParams = new BasicHttpParams();
            	httpParams.setParameter("http.protocol.handle-redirects", false);
            	httpGet.setParams(httpParams);
            	HttpClient client = new DefaultHttpClient();
            	if(WebHttpsClient.isHttps(url)){
            		client = WebHttpsClient.wrapClient(client);
            	}
            	HttpResponse response = null;
            	Header[] headers = null;
            	String temp = null;
            	
                response = client.execute(httpGet);
            	headers = response.getHeaders(HEADER_SET_COOKIE);	                
                for(Header h : headers){
                	temp = getCookieValue(KEY_PDSESSION, h.getValue());
                	if(temp != null){
                		this.pdSession = temp;
                	}
                	temp = getCookieValue(KEY_CSRFTOKEN, h.getValue());
                	if(temp != null){
                		this.csrfToken = temp;
                	}                	
                	
                }                
                if(response.getStatusLine().getStatusCode() != HttpStatus.SC_OK){
                	result.setError(true);
            		result.setStatus(TaskResult.Status.SERVER_UNAVALAIBLE);
            		return result;            	
                }
                
                HttpPost httpPost = new HttpPost(params[0]);
                httpParams = new BasicHttpParams();
            	httpParams.setParameter("http.protocol.handle-redirects", false);
            	httpPost.setParams(httpParams);
            	httpPost.addHeader(HEADER_REFERER, url);
                httpPost.addHeader(HEADER_COOKIE, String.format(KEY_PDSESSION + "=%s; " + KEY_CSRFTOKEN + "=%s;", this.pdSession, this.csrfToken));                
                MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
                multipartEntity.addPart(KEY_CSRFMIDDLETOKEN, new StringBody(this.csrfToken, Charset.forName("UTF-8")));
    			multipartEntity.addPart(KEY_USERNAME, new StringBody(this.userName, Charset.forName("UTF-8")));
    			multipartEntity.addPart(KEY_PASSWORD, new StringBody(this.password, Charset.forName("UTF-8")));    			
    			httpPost.setEntity(multipartEntity);
            	response = client.execute(httpPost);
            	if(response.getStatusLine().getStatusCode() == 302){
	            	headers = response.getHeaders(HEADER_SET_COOKIE);
	                temp = null;
	                for(Header h : headers){
	                	temp = getCookieValue(KEY_PDSESSION, h.getValue());
	                	if(temp != null){
	                		this.pdSession = temp;
	                	}    	                	
	                	
	                }
	                result.setError(false);
            		result.setStatus(TaskResult.Status.LOGIN_SUCCESSED);
            	} else {
            		result.setError(true);
            		result.setStatus(TaskResult.Status.LOGIN_FAILED);
            	}
            	
            } catch (Exception e) {                
                result.setError(true);
        		result.setStatus(TaskResult.Status.SERVER_UNAVALAIBLE);            }
        
        }else{
        	throw new IllegalArgumentException("Needs 3 param: url, login, password");        	
        }
        return result;
    }
    
    private String getCookieValue(String name, String str){
        String regexp = name + "=([^;]+)[;$]";
        Pattern pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);        
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            /*System.out.print("Start index: " + matcher.start());
            System.out.print(" End index: " + matcher.end() + " ");
            System.out.println(matcher.group());*/
        	return matcher.group(1);
        }
        return null;
        
    }
    
    
}