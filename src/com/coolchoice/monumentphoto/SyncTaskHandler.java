package com.coolchoice.monumentphoto;

import java.util.ArrayList;

import android.R;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.coolchoice.monumentphoto.dal.DB;
import com.coolchoice.monumentphoto.data.SettingsData;
import com.coolchoice.monumentphoto.task.*;
import com.coolchoice.monumentphoto.task.TaskResult.Status;

class SyncTaskHandler implements AsyncTaskCompleteListener<TaskResult>, AsyncTaskProgressListener{

	public interface SyncCompleteListener {
		public void onComplete(int type, TaskResult taskResult);
	}
	
	private Context mContext;
	private ProgressDialog mProgressDialogSyncData;
	private boolean isStartedCheckLogin = false;
	private boolean isStartedGetData = false;
	private boolean isStartedUploadData = false;
	private String mProgressDialogTitle;
	private String mProgressDialogMessage;
	
	private ArrayList<BaseTask> mTasks = new ArrayList<BaseTask>();
	private int mCurrentTaskIndex = -1;
	private int mCemeteryServerId, mRegionServerId, mPlaceServerId, mGraveServerId;
	private int mType;
	
	private static SyncCompleteListener onSyncCompleteListener;
	
	public void setOnSyncCompleteListener(SyncCompleteListener syncCompleteListener){
		onSyncCompleteListener = syncCompleteListener;
	}
	
	public SyncTaskHandler(){		
	}	
	
	private void setEmptyServerId(){
		this.mCemeteryServerId = Integer.MIN_VALUE;
		this.mRegionServerId = Integer.MIN_VALUE;
		this.mPlaceServerId = Integer.MIN_VALUE;
		this.mGraveServerId = Integer.MIN_VALUE;
	}
	
	public void setContext(Context context){
		this.mContext = context;
	}
	
	public void startGetCemetery(){
		setEmptyServerId();
		if(isStartedUploadData || isStartedCheckLogin){
			return;
		}
		isStartedGetData = true;
		mType = 1;
		mTasks.clear();
		mTasks.add(new GetCemeteryTask(this, this, this.mContext));
		mCurrentTaskIndex = -1;
		mProgressDialogTitle = "Загрузка данных...";
		mProgressDialogMessage = "Подождите";
		mProgressDialogSyncData = ProgressDialog.show(this.mContext, mProgressDialogTitle, mProgressDialogMessage, true);
		mProgressDialogSyncData.setCancelable(false);
		SettingsData settingsData = Settings.getSettingData(this.mContext);
		LoginTask loginTask = new LoginTask(this, this, this.mContext);
		loginTask.execute(Settings.getLoginUrl(mContext), settingsData.Login, settingsData.Password);	
	}
	
	public void startGetRegion(int cemeteryServerId){
		setEmptyServerId();
		this.mCemeteryServerId = cemeteryServerId;
		if(isStartedUploadData || isStartedCheckLogin){
			return;
		}
		isStartedGetData = true;
		mType = 1;
		mTasks.clear();
		mTasks.add(new GetRegionTask(this, this, this.mContext));
		mCurrentTaskIndex = -1;
		mProgressDialogTitle = "Загрузка данных...";
		mProgressDialogMessage = "Подождите";
		mProgressDialogSyncData = ProgressDialog.show(this.mContext, mProgressDialogTitle, mProgressDialogMessage, true);
		mProgressDialogSyncData.setCancelable(false);
		SettingsData settingsData = Settings.getSettingData(this.mContext);
		LoginTask loginTask = new LoginTask(this, this, this.mContext);
		loginTask.execute(Settings.getLoginUrl(mContext), settingsData.Login, settingsData.Password);
	}
	
	public void startGetPlace(int regionServerId){
		setEmptyServerId();
		this.mRegionServerId = regionServerId;
		if(isStartedUploadData || isStartedCheckLogin){
			return;
		}
		isStartedGetData = true;
		mType = 1;
		mTasks.clear();
		mTasks.add(new GetPlaceTask(this, this, this.mContext));
		mCurrentTaskIndex = -1;
		mProgressDialogTitle = "Загрузка данных...";
		mProgressDialogMessage = "Подождите";
		mProgressDialogSyncData = ProgressDialog.show(this.mContext, mProgressDialogTitle, mProgressDialogMessage, true);
		mProgressDialogSyncData.setCancelable(false);
		SettingsData settingsData = Settings.getSettingData(this.mContext);
		LoginTask loginTask = new LoginTask(this, this, this.mContext);
		loginTask.execute(Settings.getLoginUrl(mContext), settingsData.Login, settingsData.Password);	
	}
	
	public void startGetGrave(int placeServerId){
		setEmptyServerId();
		this.mPlaceServerId = placeServerId;
		if(isStartedUploadData || isStartedCheckLogin){
			return;
		}
		isStartedGetData = true;
		mType = 1;
		mTasks.clear();
		mTasks.add(new GetGraveTask(this, this, this.mContext));
		mCurrentTaskIndex = -1;
		mProgressDialogTitle = "Загрузка данных...";
		mProgressDialogMessage = "Подождите";
		mProgressDialogSyncData = ProgressDialog.show(this.mContext, mProgressDialogTitle, mProgressDialogMessage, true);
		mProgressDialogSyncData.setCancelable(false);
		SettingsData settingsData = Settings.getSettingData(this.mContext);
		LoginTask loginTask = new LoginTask(this, this, this.mContext);
		loginTask.execute(Settings.getLoginUrl(mContext), settingsData.Login, settingsData.Password);	
	}
	
	public void startGetBurial(int graveServerId){
		setEmptyServerId();
		this.mGraveServerId = graveServerId;
		if(isStartedUploadData || isStartedCheckLogin){
			return;
		}
		isStartedGetData = true;
		mType = 1;
		mTasks.clear();
		mTasks.add(new GetBurialTask(this, this, this.mContext));
		mCurrentTaskIndex = -1;
		mProgressDialogTitle = "Загрузка данных...";
		mProgressDialogMessage = "Подождите";
		mProgressDialogSyncData = ProgressDialog.show(this.mContext, mProgressDialogTitle, mProgressDialogMessage, true);
		mProgressDialogSyncData.setCancelable(false);
		SettingsData settingsData = Settings.getSettingData(this.mContext);
		LoginTask loginTask = new LoginTask(this, this, this.mContext);
		loginTask.execute(Settings.getLoginUrl(mContext), settingsData.Login, settingsData.Password);	
	}
	
	public void startCheckLogin(){
		setEmptyServerId();
		if(isStartedUploadData || isStartedGetData){
			Toast.makeText(this.mContext, "Идет завершение предыдущей операции", Toast.LENGTH_LONG).show();
			return;
		}
		isStartedCheckLogin = true;
		mType = 0;
		mTasks.clear();
		mCurrentTaskIndex = -1;
		mProgressDialogTitle = "Проверка доступа к серверу";
		mProgressDialogMessage = "Авторизация";
		mProgressDialogSyncData = ProgressDialog.show(this.mContext, mProgressDialogTitle, mProgressDialogMessage, true);
		mProgressDialogSyncData.setCancelable(false);
		SettingsData settingsData = Settings.getSettingData(this.mContext);
		LoginTask loginTask = new LoginTask(this, this, this.mContext);
		loginTask.execute(Settings.getLoginUrl(mContext), settingsData.Login, settingsData.Password);			
	}
	
	public void startGetData(){
		setEmptyServerId();
		if(isStartedUploadData || isStartedCheckLogin){
			Toast.makeText(this.mContext, "Идет завершение предыдущей операции", Toast.LENGTH_LONG).show();
			return;
		}
		isStartedGetData = true;
		mType = 1;
		mTasks.clear();
		mTasks.add(new GetCemeteryTask(this, this, this.mContext));
		mTasks.add(new GetRegionTask(this, this, this.mContext));
		mTasks.add(new GetPlaceTask(this, this, this.mContext));
		mTasks.add(new GetGraveTask(this, this, this.mContext));
		mTasks.add(new GetBurialTask(this, this, this.mContext));
		mCurrentTaskIndex = -1;
		mProgressDialogTitle = "Загрузка данных...";
		mProgressDialogMessage = "Авторизация";
		showProgressDialogGetData();
		/*mProgressDialogSyncData = ProgressDialog.show(this.mContext, mProgressDialogTitle, mProgressDialogMessage, true);
		mProgressDialogSyncData.setCancelable(false);*/
		SettingsData settingsData = Settings.getSettingData(this.mContext);
		LoginTask loginTask = new LoginTask(this, this, this.mContext);
		loginTask.execute(Settings.getLoginUrl(mContext), settingsData.Login, settingsData.Password);			
	}
	
	public void startUploadData(){
		setEmptyServerId();
		if(isStartedGetData || isStartedCheckLogin){
			Toast.makeText(this.mContext, "Идет завершение предыдущей операции", Toast.LENGTH_LONG).show();
			return;
		}
		isStartedUploadData = true;
		mType = 2;
		mTasks.clear();
		mTasks.add(new UploadCemeteryTask(this, this, this.mContext));
		mTasks.add(new UploadRegionTask(this, this, this.mContext));
		mTasks.add(new UploadPlaceTask(this, this, this.mContext));
		mTasks.add(new UploadGraveTask(this, this, this.mContext));
		mTasks.add(new UploadPhotoTask(this, this, this.mContext));
		mCurrentTaskIndex = -1;
		mProgressDialogTitle = "Отправка данных...";
		mProgressDialogMessage = "Авторизация";
		mProgressDialogSyncData = ProgressDialog.show(this.mContext, mProgressDialogTitle, mProgressDialogMessage, true);
		mProgressDialogSyncData.setCancelable(false);
		SettingsData settingsData = Settings.getSettingData(this.mContext);
		LoginTask loginTask = new LoginTask(this, this, this.mContext);
		loginTask.execute(Settings.getLoginUrl(mContext), settingsData.Login, settingsData.Password);			
	}
	
	private void showProgressDialogGetData(){
		mProgressDialogSyncData = new ProgressDialog(this.mContext);
		mProgressDialogSyncData.setMessage(mProgressDialogMessage);
		mProgressDialogSyncData.setTitle(mProgressDialogTitle);
		mProgressDialogSyncData.setCancelable(false);		
		mProgressDialogSyncData.setButton("Отменить", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
            	for(BaseTask task : mTasks){
            		try{            			
            			task.cancel(true);
            		}catch(Exception exc){
            			exc.printStackTrace();
            			//do nothing
            		}
            	}            	
                mProgressDialogSyncData.dismiss();
            }
        });
		mProgressDialogSyncData.show();
	}
	
	public void checkResumeDataOperation(Context context){
		if(isStartedGetData){
			setContext(context);
			showProgressDialogGetData();
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
	
	public void showSummaryUploadInfo(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext);
		builder.setTitle("Статистика отпраленных данных");
		StringBuilder sbMessage = new StringBuilder();
		for(BaseTask task : mTasks){
			TaskResult result = task.getTaskResult();
			if(result == null || result.getUploadCount() == 0){
				continue;
			}
			sbMessage.append(System.getProperty("line.separator"));
			if(task instanceof UploadCemeteryTask){
				sbMessage.append(String.format("Успешно отправлено кладбищ: %d из %d.", result.getUploadCountSuccess(), result.getUploadCount()));				
			}
			if(task instanceof UploadRegionTask){
				sbMessage.append(String.format("Успешно отправлено участков: %d  из %d.", result.getUploadCountSuccess(), result.getUploadCount()));				
			}
			if(task instanceof UploadPlaceTask){
				sbMessage.append(String.format("Успешно отправлено мест: %d из %d.", result.getUploadCountSuccess(), result.getUploadCount()));				
			}
			if(task instanceof UploadGraveTask){
				sbMessage.append(String.format("Успешно отправлено могил: %d из %d.", result.getUploadCountSuccess(), result.getUploadCount()));				
			}
			if(task instanceof UploadPhotoTask){
				sbMessage.append(String.format("Успешно отправлено фотографий: %d из %d.", result.getUploadCountSuccess(), result.getUploadCount()));				
			}
			sbMessage.append(System.getProperty("line.separator"));
		}		
		builder.setMessage(sbMessage.toString());
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
		
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	@Override
	public void onProgressUpdate(String... messages) {
		mProgressDialogSyncData.setMessage(messages[0]);		
	}

	@Override
	public void onTaskComplete(BaseTask finishedTask, TaskResult result) {
		boolean isNextTaskStart = false;
		boolean isCancelTask = false;
		if(result.getStatus() == Status.CANCEL_TASK){
			isCancelTask = true;
		}
		String getArgs = "";
		if(mCemeteryServerId > 0){
			if(getArgs.length() == 0){
				getArgs += String.format("?"+ BaseTask.ARG_CEMETERY_ID + "=%d", mCemeteryServerId);
			} else{
				getArgs += String.format("&"+ BaseTask.ARG_CEMETERY_ID + "=%d", mCemeteryServerId);
			}
		}
		if(mRegionServerId > 0){
			if(getArgs.length() == 0){
				getArgs += String.format("?"+ BaseTask.ARG_AREA_ID + "=%d", mRegionServerId);
			} else{
				getArgs += String.format("&"+ BaseTask.ARG_AREA_ID + "=%d", mRegionServerId);
			}
		}
		if(mPlaceServerId > 0){
			if(getArgs.length() == 0){
				getArgs += String.format("?"+ BaseTask.ARG_PLACE_ID + "=%d", mPlaceServerId);
			} else{
				getArgs += String.format("&"+ BaseTask.ARG_PLACE_ID + "=%d", mPlaceServerId);
			}
		}
		if(mGraveServerId > 0){
			if(getArgs.length() == 0){
				getArgs += String.format("?"+ BaseTask.ARG_GRAVE_ID + "=%d", mGraveServerId);
			} else{
				getArgs += String.format("&"+ BaseTask.ARG_GRAVE_ID + "=%d", mGraveServerId);
			}
		}
		
		
		if(isStartedGetData && (!isCancelTask)){
			if(!result.isError() || (result.getStatus() == Status.HANDLE_ERROR)){
				mCurrentTaskIndex++;
				if(mCurrentTaskIndex < mTasks.size()){
					if(mCurrentTaskIndex == 0){
						LoginTask loginTask = (LoginTask) finishedTask;
						Settings.setPDSession(loginTask.getPDSession());
					}
					BaseTask nextTask = mTasks.get(mCurrentTaskIndex);
					if(nextTask.getTaskName() == Settings.TASK_GETCEMETERY){
						mProgressDialogMessage = "Получение кладбищ";
						mProgressDialogSyncData.setMessage(mProgressDialogMessage);						
						GetCemeteryTask getCemeteryTask = new GetCemeteryTask(this, this, this.mContext);
						getCemeteryTask.execute(Settings.getCemeteryUrl(mContext) + getArgs);
						isNextTaskStart = true;						
					}
					if(nextTask.getTaskName() == Settings.TASK_GETREGION){
						mProgressDialogMessage = "Получение участков";
						mProgressDialogSyncData.setMessage(mProgressDialogMessage);
						GetRegionTask getRegionTask = new GetRegionTask(this, this, this.mContext);
						getRegionTask.execute(Settings.getRegionUrl(mContext) + getArgs);
						isNextTaskStart = true;
					}
					if(nextTask.getTaskName() == Settings.TASK_GETPLACE){
						mProgressDialogMessage = "Получение мест";
						mProgressDialogSyncData.setMessage(mProgressDialogMessage);
						GetPlaceTask getPlaceTask = new GetPlaceTask(this, this, this.mContext);
						getPlaceTask.execute(Settings.getPlaceUrl(mContext) + getArgs);
						isNextTaskStart = true;
					}
					if(nextTask.getTaskName() == Settings.TASK_GETGRAVE){
						mProgressDialogMessage = "Получение могил";
						mProgressDialogSyncData.setMessage(mProgressDialogMessage);
						GetGraveTask getGraveTask = new GetGraveTask(this, this, this.mContext);
						getGraveTask.execute(Settings.getGraveUrl(mContext) + getArgs);
						isNextTaskStart = true;
					}
					if(nextTask.getTaskName() == Settings.TASK_GETBURIAL){
						mProgressDialogMessage = "Получение захоронений";
						mProgressDialogSyncData.setMessage(mProgressDialogMessage);
						GetBurialTask getBurialTask = new GetBurialTask(this, this, this.mContext);
						getBurialTask.execute(Settings.getBurialUrl(mContext) + getArgs);
						isNextTaskStart = true;
					}
				}
				
				if(mCurrentTaskIndex == mTasks.size()){
					DB.db().updateDBLink();
					mProgressDialogMessage = "Данные получены";
					mProgressDialogSyncData.setMessage(mProgressDialogMessage);
					if(mTasks.size() > 1){
						Toast.makeText(this.mContext, "Загрузка завершена", Toast.LENGTH_LONG).show();
					}
				}				
			}									
			
		}
		
		if(isStartedUploadData && (!isCancelTask)){
			if(mCurrentTaskIndex >= 0){
				mTasks.get(mCurrentTaskIndex).setTaskResult(result);
			}
			if(!result.isError() || (result.getStatus() == Status.HANDLE_ERROR)){
				mCurrentTaskIndex++;
				if(mCurrentTaskIndex < mTasks.size()){
					if(mCurrentTaskIndex == 0){
						LoginTask loginTask = (LoginTask) finishedTask;
						Settings.setPDSession(loginTask.getPDSession());
					}
					BaseTask nextTask = mTasks.get(mCurrentTaskIndex);
					if(nextTask.getTaskName() == Settings.TASK_POSTCEMETERY){
						mProgressDialogMessage = "Отправка кладбищ на сервер";
						mProgressDialogSyncData.setMessage(mProgressDialogMessage);
						UploadCemeteryTask uploadCemeteryTask = new UploadCemeteryTask(this, this, this.mContext);
						uploadCemeteryTask.execute(Settings.getUploadCemeteryUrl(this.mContext));
						isNextTaskStart = true;
					}
					if(nextTask.getTaskName() == Settings.TASK_POSTREGION){
						mProgressDialogMessage = "Отправка участков на сервер";
						mProgressDialogSyncData.setMessage(mProgressDialogMessage);						
						UploadRegionTask uploadRegionTask = new UploadRegionTask(this, this, this.mContext);
						uploadRegionTask.execute(Settings.getUploadRegionUrl(this.mContext));
						isNextTaskStart = true;
					}
					if(nextTask.getTaskName() == Settings.TASK_POSTPLACE){
						mProgressDialogMessage = "Отправка мест на сервер";
						mProgressDialogSyncData.setMessage(mProgressDialogMessage);						
						UploadPlaceTask uploadPlaceTask = new UploadPlaceTask(this, this, this.mContext);
						uploadPlaceTask.execute(Settings.getUploadPlaceUrl(this.mContext));
						isNextTaskStart = true;
					}
					if(nextTask.getTaskName() == Settings.TASK_POSTGRAVE){
						mProgressDialogMessage = "Отправка могил на сервер";
						mProgressDialogSyncData.setMessage(mProgressDialogMessage);						
						UploadGraveTask uploadGraveTask = new UploadGraveTask(this, this, this.mContext);
						uploadGraveTask.execute(Settings.getUploadGraveUrl(this.mContext));
						isNextTaskStart = true;
					}
					if(nextTask.getTaskName() == Settings.TASK_POSTPHOTOGRAVE){
						mProgressDialogMessage = "Отправка фотографий на сервер";
						mProgressDialogSyncData.setMessage(mProgressDialogMessage);						
						UploadPhotoTask uploadPhotoTask = new UploadPhotoTask(this, this, this.mContext);
						uploadPhotoTask.execute(Settings.getUploadPhotoUrl(this.mContext));
						isNextTaskStart = true;
					}
				}
				
				if(mCurrentTaskIndex == mTasks.size()){
					mProgressDialogMessage = "Данные отправлены";
					mProgressDialogSyncData.setMessage(mProgressDialogMessage);
					Toast.makeText(this.mContext, "Отправка завершена", Toast.LENGTH_LONG).show();
					showSummaryUploadInfo();
				}
			}
			
			
		}
		
		if(!isNextTaskStart){
			if(isStartedGetData){
				DB.db().updateDBLink();				
			}			
			mProgressDialogSyncData.dismiss();
			isStartedGetData = false;
			isStartedUploadData = false;
			isStartedCheckLogin = false;
			if(onSyncCompleteListener != null){
				onSyncCompleteListener.onComplete(this.mType, result);
			}			
		}
		
	}
	
}
