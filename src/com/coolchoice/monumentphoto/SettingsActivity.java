package com.coolchoice.monumentphoto;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolchoice.monumentphoto.dal.DB;
import com.coolchoice.monumentphoto.dal.MonumentDB;
import com.coolchoice.monumentphoto.data.Monument;
import com.coolchoice.monumentphoto.data.GravePhoto;
import com.coolchoice.monumentphoto.data.SettingsData;
import com.coolchoice.monumentphoto.service.*;
import java.net.HttpURLConnection;

import com.coolchoice.monumentphoto.task.*;

public class SettingsActivity extends Activity implements SyncTaskHandler.SyncCompleteListener {

	private EditText etLogin, etPassword, etServerAdress, etGPSInterval;
	
	private CheckBox cbIsAutoPhotoSend, cbIsAutoDownloadData;
	
	private Button btnCheck, btnClearHistory, btnSyncData, btnUploadData;
	
	private TextView tvStatusServer;
	
	private ImageView ivStatusServer;
	
	private static int mStatusServer = 0;
	
	private static SyncTaskHandler mSyncTaskHandler;
			
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_activity);
		String title = getTitle().toString();
		String versionName = Settings.getCurrentVersion(this);
		title = String.format("%s (версия %s)", title, versionName);
		setTitle(title);
		this.etLogin = (EditText) findViewById(R.id.etLogin);
		this.etPassword = (EditText) findViewById(R.id.etPassword);
		this.etServerAdress = (EditText) findViewById(R.id.etServerAddress);
		this.etGPSInterval = (EditText) findViewById(R.id.etGPSInterval);
		this.cbIsAutoPhotoSend = (CheckBox) findViewById(R.id.cbAutoSenverdPhotoToServer);
		this.cbIsAutoDownloadData = (CheckBox) findViewById(R.id.cbAutoDownloadData);
		this.btnCheck = (Button) findViewById(R.id.btnCheck);
		this.btnClearHistory = (Button) findViewById(R.id.btnClearHistory);
		this.btnSyncData = (Button) findViewById(R.id.btnSyncData);
		this.btnUploadData = (Button) findViewById(R.id.btnUploadData);
		this.tvStatusServer = (TextView) findViewById(R.id.tvStatusServer);
		this.ivStatusServer = (ImageView) findViewById(R.id.ivStatusServer);		
		this.btnCheck.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				saveSettingsData();
				mSyncTaskHandler.startCheckLogin();				
			}
		});
		
		this.btnClearHistory.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		    	    @Override
		    	    public void onClick(DialogInterface dialog, int which) {
		    	        switch (which){
		    	        case DialogInterface.BUTTON_POSITIVE:
		    	        	
		    	            break;
		    	        case DialogInterface.BUTTON_NEGATIVE:
		    	            //do nothing
		    	            break;
		    	        }
		    	    }
		    	};	
		    	AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
		    	builder.setMessage(getString(R.string.deleteSendedMonumentQuestion)).setPositiveButton(getString(R.string.yes), dialogClickListener)
		    	    .setNegativeButton(getString(R.string.no), dialogClickListener).show();			
								
			}
		});
		
		this.btnSyncData.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				saveSettingsData();
				mSyncTaskHandler.startGetData();				
			}
		});
		
		this.btnUploadData.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				saveSettingsData();
				mSyncTaskHandler.startUploadData();				
			}
		});
		
		SettingsData data = Settings.getSettingData(this);
		this.etLogin.setText(data.Login);
		this.etPassword.setText(data.Password);
		this.etServerAdress.setText(data.ServerAddress);
		this.etGPSInterval.setText(Integer.toString(data.GPSInterval));
		this.cbIsAutoPhotoSend.setChecked(data.IsAutoSendPhoto);
		this.cbIsAutoDownloadData.setChecked(data.IsAutoDownloadData);
		
		if(mSyncTaskHandler == null){
			mSyncTaskHandler = new SyncTaskHandler();
		}
		mSyncTaskHandler.setContext(this);
		mSyncTaskHandler.checkResumeDataOperation(this);
		mSyncTaskHandler.setOnSyncCompleteListener(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		updateStatusServerInUI();
	}
	
	@Override
    public void onBackPressed() {
		saveSettingsData();
		super.onBackPressed();
    }
	
	public void saveSettingsData(){
		SettingsData data = new SettingsData();		
		data.Login = this.etLogin.getText().toString();
		data.Password = this.etPassword.getText().toString();
		data.ServerAddress = this.etServerAdress.getText().toString();
		int interval = 0;
		try{
			interval = Integer.parseInt(this.etGPSInterval.getText().toString());
		}catch(Exception ex){
			//do nothing
		}
		data.GPSInterval = interval;
		data.IsAutoSendPhoto = this.cbIsAutoPhotoSend.isChecked();
		data.IsAutoDownloadData = this.cbIsAutoDownloadData.isChecked();
		Settings.saveSettingsData(this, data);
	}
	
	private void deleteSendedMonuments(){
		List<Monument> monuments = MonumentDB.getMonumentsWithPhotos(Monument.STATUS_SEND);
		for(Monument monument: monuments){
			boolean isCanDeleteMonument = true;
			for(GravePhoto photo : monument.Photos){
				if(photo.Status != Monument.STATUS_SEND){
					isCanDeleteMonument = false;
					break;
				}
			}
			if(isCanDeleteMonument){
				for(GravePhoto photo : monument.Photos){
					try{
						Uri uri = Uri.parse(photo.UriString);
						File file = new File(uri.getPath());
						file.delete();
					}catch(Exception exc){
						//do nothing
					}
					MonumentDB.deleteMonumentPhoto(photo);
				}
				DB.dao(Monument.class).deleteById(monument.Id);
			}
		}	
		
	}

	private void setStatusServer (TaskResult taskResult) {
		int status = 0;
        if(taskResult.isError()){
        	if(taskResult.getStatus() == TaskResult.Status.SERVER_UNAVALAIBLE){
        		status = 1;
        	} 
        	if(taskResult.getStatus() == TaskResult.Status.LOGIN_FAILED){
        		status = 2;
        	}
        } else {
        	status = 3;
        }        
        mStatusServer = status;
        updateStatusServerInUI();        
	}
	
	private void updateStatusServerInUI(){
		this.tvStatusServer = (TextView) findViewById(R.id.tvStatusServer);
		this.ivStatusServer = (ImageView) findViewById(R.id.ivStatusServer);
		switch (mStatusServer) {
		case 0:
			this.tvStatusServer.setText(R.string.serverStatusDefault);					
			break;
		case 1:
			this.tvStatusServer.setText(R.string.serverStatusUnavalaible);					
			break;
		case 2:
			this.tvStatusServer.setText(R.string.serverStatusLoginFailed);					
			break;
		case 3:
			this.tvStatusServer.setText(R.string.serverStatusLoginSuccess);					
			break;
		}
        this.ivStatusServer.getDrawable().setLevel(mStatusServer);
	}

	@Override
	public void onComplete(int type, TaskResult taskResult) {
		if(type == 0){
			setStatusServer(taskResult);
		}
		
	}

	
	/*class ServerTaskHandler implements AsyncTaskCompleteListener<TaskResult>, AsyncTaskProgressListener{

		private Context mContext;
		private ProgressDialog mProgressDialogSyncData;
		private boolean isStartedCheckLogin = false;
		private boolean isStartedGetData = false;
		private boolean isStartedUploadData = false;
		private String mProgressDialogTitle;
		private String mProgressDialogMessage;
		
		public ServerTaskHandler(){			
		}
		
		public void setContext(Context context){
			this.mContext = context;
		}
		
		public void startCheckLogin(){
			if(isStartedUploadData || isStartedGetData){
				return;
			}
			isStartedCheckLogin = true;
			mProgressDialogTitle = "Проверка доступа к серверу";
			mProgressDialogMessage = "Авторизация";
			mProgressDialogSyncData = ProgressDialog.show(this.mContext, mProgressDialogTitle, mProgressDialogMessage, true);
			mProgressDialogSyncData.setCancelable(false);
			SettingsData settingsData = Settings.getSettingData(this.mContext);
			LoginTask loginTask = new LoginTask(this, this, this.mContext);
			loginTask.execute(Settings.getLoginUrl(mContext), settingsData.Login, settingsData.Password);			
		}
		
		public void startGetData(){
			if(isStartedUploadData || isStartedCheckLogin){
				return;
			}
			isStartedGetData = true;
			mProgressDialogTitle = "Загрузка данных...";
			mProgressDialogMessage = "Авторизация";
			mProgressDialogSyncData = ProgressDialog.show(this.mContext, mProgressDialogTitle, mProgressDialogMessage, true);
			mProgressDialogSyncData.setCancelable(false);
			SettingsData settingsData = Settings.getSettingData(this.mContext);
			LoginTask loginTask = new LoginTask(this, this, this.mContext);
			loginTask.execute(Settings.getLoginUrl(mContext), settingsData.Login, settingsData.Password);			
		}
		
		public void startUploadData(){
			if(isStartedGetData || isStartedCheckLogin){
				return;
			}
			isStartedUploadData = true;
			mProgressDialogTitle = "Отправка данных...";
			mProgressDialogMessage = "Авторизация";
			mProgressDialogSyncData = ProgressDialog.show(this.mContext, mProgressDialogTitle, mProgressDialogMessage, true);
			mProgressDialogSyncData.setCancelable(false);
			SettingsData settingsData = Settings.getSettingData(this.mContext);
			LoginTask loginTask = new LoginTask(this, this, this.mContext);
			loginTask.execute(Settings.getLoginUrl(mContext), settingsData.Login, settingsData.Password);			
		}
		
		public void checkResumeDataOperation(Context context){
			if(isStartedGetData){
				setContext(context);
				mProgressDialogSyncData = ProgressDialog.show(this.mContext, mProgressDialogTitle, mProgressDialogMessage, true);
				mProgressDialogSyncData.setCancelable(false);
			}
			if(isStartedUploadData){
				setContext(context);
				mProgressDialogSyncData = ProgressDialog.show(this.mContext, mProgressDialogTitle, mProgressDialogMessage, true);
				mProgressDialogSyncData.setCancelable(false);
			}
			if(isStartedCheckLogin){
				setContext(context);
				mProgressDialogSyncData = ProgressDialog.show(this.mContext, mProgressDialogTitle, mProgressDialogMessage, true);
				mProgressDialogSyncData.setCancelable(false);
			}
		}
		
		@Override
		public void onProgressUpdate(String... messages) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onTaskComplete(BaseTask finishedTask, TaskResult result) {
			boolean isNextTaskStart = false;
			int cemeteryId = 3;
			String getArgs = String.format("?"+ BaseTask.ARG_CEMETERY_ID + "=%d", cemeteryId);
			
			if(isStartedGetData){
				if(result.getTaskName() == Settings.TASK_LOGIN){
					if(!result.isError()){
						DB.db().dropDBTrigger();
						mProgressDialogMessage = "Получение названия кладбищ";
						mProgressDialogSyncData.setMessage(mProgressDialogMessage);
						com.coolchoice.monumentphoto.task.LoginTask loginTask = (com.coolchoice.monumentphoto.task.LoginTask) finishedTask;
						Settings.setPDSession(loginTask.getPDSession());
						GetCemeteryTask getCemeteryTask = new GetCemeteryTask(this, this, this.mContext);
						getCemeteryTask.execute(Settings.getCemeteryUrl(mContext));
						isNextTaskStart = true;
					}
				}
				
				if(result.getTaskName() == Settings.TASK_GETCEMETERY){
					if(!result.isError()){
						mProgressDialogMessage = "Получение названия участков";
						mProgressDialogSyncData.setMessage(mProgressDialogMessage);
						GetRegionTask getRegionTask = new GetRegionTask(this, this, this.mContext);
						getRegionTask.execute(Settings.getRegionUrl(mContext) + getArgs);
						isNextTaskStart = true;
					}
				}		
									
				if(result.getTaskName() == Settings.TASK_GETREGION){
					if(!result.isError()){
						mProgressDialogMessage = "Получение названия мест";
						mProgressDialogSyncData.setMessage(mProgressDialogMessage);
						GetPlaceTask getPlaceTask = new GetPlaceTask(this, this, this.mContext);
						getPlaceTask.execute(Settings.getPlaceUrl(mContext) + getArgs);
						isNextTaskStart = true;
					}
				}
				
				if(result.getTaskName() == Settings.TASK_GETPLACE){
					if(!result.isError()){
						mProgressDialogMessage = "Получение названия могил";
						mProgressDialogSyncData.setMessage(mProgressDialogMessage);
						GetGraveTask getGraveTask = new GetGraveTask(this, this, this.mContext);
						getGraveTask.execute(Settings.getGraveUrl(mContext) + getArgs);
						isNextTaskStart = true;
					}
				}
				
				if(result.getTaskName() == Settings.TASK_GETGRAVE){
					if(!result.isError()){
						DB.db().updateDBLink();
						mProgressDialogMessage = "Данные получены";
						mProgressDialogSyncData.setMessage(mProgressDialogMessage);
						Toast.makeText(SettingsActivity.this, "Загрузка завершена", Toast.LENGTH_LONG).show();					
					}
				}				
				
			}
			
			if(isStartedUploadData){
				if(result.getTaskName() == Settings.TASK_LOGIN){
					if(!result.isError()){
						DB.db().dropDBTrigger();
						mProgressDialogMessage = "Отправка названия кладбищ на сервер";
						mProgressDialogSyncData.setMessage(mProgressDialogMessage);
						LoginTask loginTask = (LoginTask) finishedTask;
						Settings.setPDSession(loginTask.getPDSession());
						UploadCemeteryTask uploadCemeteryTask = new UploadCemeteryTask(this, this, this.mContext);
						uploadCemeteryTask.execute(Settings.getUploadCemeteryUrl(this.mContext));
						isNextTaskStart = true;
					}
				}
				if(result.getTaskName() == Settings.TASK_POSTCEMETERY){
					if(!result.isError()){
						DB.db().execManualSQL("update cemetery set IsChanged = 0;");
						mProgressDialogMessage = "Отправка названия участков на сервер";
						mProgressDialogSyncData.setMessage(mProgressDialogMessage);						
						UploadRegionTask uploadRegionTask = new UploadRegionTask(this, this, this.mContext);
						uploadRegionTask.execute(Settings.getUploadRegionUrl(this.mContext));
						isNextTaskStart = true;
					}
				}
				if(result.getTaskName() == Settings.TASK_POSTREGION){
					if(!result.isError()){
						DB.db().execManualSQL("update region set IsChanged = 0;");
						mProgressDialogMessage = "Отправка названия мест на сервер";
						mProgressDialogSyncData.setMessage(mProgressDialogMessage);						
						UploadPlaceTask uploadPlaceTask = new UploadPlaceTask(this, this, this.mContext);
						uploadPlaceTask.execute(Settings.getUploadPlaceUrl(this.mContext));
						isNextTaskStart = true;
					}
				}
				if(result.getTaskName() == Settings.TASK_POSTPLACE){
					if(!result.isError()){
						DB.db().execManualSQL("update row set IsChanged = 0;");
						DB.db().execManualSQL("update place set IsChanged = 0;");
						mProgressDialogMessage = "Отправка названия могил на сервер";
						mProgressDialogSyncData.setMessage(mProgressDialogMessage);						
						UploadGraveTask uploadGraveTask = new UploadGraveTask(this, this, this.mContext);
						uploadGraveTask.execute(Settings.getUploadGraveUrl(this.mContext));
						isNextTaskStart = true;
					}
				}
				if(result.getTaskName() == Settings.TASK_POSTGRAVE){
					if(!result.isError()){
						DB.db().execManualSQL("update grave set IsChanged = 0;");
						mProgressDialogMessage = "Отправка фотографий на сервер";
						mProgressDialogSyncData.setMessage(mProgressDialogMessage);						
						UploadPhotoTask uploadPhotoTask = new UploadPhotoTask(this, this, this.mContext);
						uploadPhotoTask.execute(Settings.getUploadPhotoUrl(this.mContext));
						isNextTaskStart = true;
					}
				}
				
				if(result.getTaskName() == Settings.TASK_POSTPHOTOGRAVE){
					if(!result.isError()){
						mProgressDialogMessage = "Данные отправлены";
						mProgressDialogSyncData.setMessage(mProgressDialogMessage);
						Toast.makeText(SettingsActivity.this, "Отправка завершена", Toast.LENGTH_LONG).show();					
					}
				}
				
			}
			
			if(isStartedCheckLogin){
				if(result.getTaskName() == Settings.TASK_LOGIN){
					setStatusServer(result);
				}
			}
			
			if(!isNextTaskStart){
				if(isStartedGetData){
					DB.db().createDBTrigger();
				}
				if(isStartedUploadData){
					DB.db().createDBTrigger();
				}
				mProgressDialogSyncData.dismiss();
				isStartedGetData = false;
				isStartedUploadData = false;
				isStartedCheckLogin = false;
			}
			
		}
		
	}*/
	
}
