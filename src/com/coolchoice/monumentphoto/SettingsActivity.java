package com.coolchoice.monumentphoto;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.coolchoice.monumentphoto.SyncTaskHandler.OperationType;
import com.coolchoice.monumentphoto.data.SettingsData;
import com.coolchoice.monumentphoto.task.TaskResult;

public class SettingsActivity extends Activity implements SyncTaskHandler.SyncCompleteListener {

	private EditText etLogin, etPassword, etServerAdress, etGPSInterval;
	
	private CheckBox cbIsAutoPhotoSend, cbIsAutoDownloadData, cbIsOldPlace ;
	
	private Button btnCheck, btnClearHistory, btnSyncData, btnUploadData, btnExit;
	
	private TextView tvStatusServer;
	
	private ImageView ivStatusServer;
	
	private static int mStatusServer = 0;
	
	private static SyncTaskHandler mSyncTaskHandler;
	
	public static final String EXTRA_EXIT = "EXIT";
			
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
		this.cbIsOldPlace = (CheckBox) findViewById(R.id.cbOldPlace);
		this.btnCheck = (Button) findViewById(R.id.btnCheck);
		this.btnClearHistory = (Button) findViewById(R.id.btnClearHistory);
		this.btnSyncData = (Button) findViewById(R.id.btnSyncData);
		this.btnUploadData = (Button) findViewById(R.id.btnUploadData);
		this.tvStatusServer = (TextView) findViewById(R.id.tvStatusServer);
		this.ivStatusServer = (ImageView) findViewById(R.id.ivStatusServer);
		this.btnExit = (Button) findViewById(R.id.btnExit);
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
				mSyncTaskHandler.startGetOnlyChangedData();			
			}
		});
		
		this.btnUploadData.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				saveSettingsData();
				mSyncTaskHandler.startUploadData();				
			}
		});
		
		this.btnExit.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(getApplicationContext(), CemeteryListActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra(EXTRA_EXIT, true);
				startActivity(intent);	
			}
		});
		
		SettingsData data = Settings.getSettingData(this);
		this.etLogin.setText(data.Login);
		this.etPassword.setText(data.Password);
		this.etServerAdress.setText(data.ServerAddress);
		this.etGPSInterval.setText(Integer.toString(data.GPSInterval));
		this.cbIsAutoPhotoSend.setChecked(data.IsAutoSendPhoto);
		this.cbIsAutoDownloadData.setChecked(data.IsAutoDownloadData);
		this.cbIsOldPlace.setChecked(data.IsOldPlaceName);
		
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
		data.IsOldPlaceName = this.cbIsOldPlace.isChecked();
		Settings.saveSettingsData(this, data);
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
	public void onComplete(SyncTaskHandler.OperationType operationType, TaskResult taskResult) {
		if(operationType == OperationType.CHECK_LOGIN){
			setStatusServer(taskResult);
		}
		
	}	
}
