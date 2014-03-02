package com.coolchoice.monumentphoto;

import java.io.File;
import java.net.ServerSocket;
import java.util.regex.Pattern;

import com.coolchoice.monumentphoto.data.SettingsData;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;

public class Settings {
	
	public interface ISettings {
		
		void onCloseCheckGPS(boolean isTurnGPS);
		
	}
    
    final static String TYPE_PHOTO = "image/jpg";
        
    public final static int httpTimeOut = 100000; 
    
    public static final String DEFAULT_ENCODING ="utf-8";
    
    public static final String PREFS_NAME = "MonumentPreferencesName";
    public static String PREFS_LOGIN_KEY = "Login";
    public static String PREFS_PASSWORD_KEY = "Password";
    public static String PREFS_SERVERADDRES_KEY = "ServerAdress";
    public static String PREFS_GPSINTERVAL_KEY = "GPSInterval";
    public static String PREFS_ISAUTOSENDPHOTO_KEY = "IsAutoSendPhoto";
    public static String PREFS_ISAUTODOWNLOADDATA_KEY = "IsAutoSendPhoto";
    public static String PREFS_ISOLDPLACENAME_KEY = "IsOldPlaceName";
    
    public static final int STATUS_SERVER_DEFAULT = 0;
    public static final int STATUS_SERVER_UNAVALIBLE = 1;
    public static final int STATUS_SEERVER_LOGIN_FAILED = 2;
    public static final int STATUS_SERVER_LOGIN_SUCCESS = 3;
    
    public static final String UNEXPECTED_ERROR_MESSAGE = "unexpected error:";
    
    public static final String TASK_LOGIN = "login";
    public static final String TASK_GETCEMETERY = "getCemetery";
    public static final String TASK_POSTCEMETERY = "postCemetery";
    public static final String TASK_GETREGION = "getRegion";
    public static final String TASK_POSTREGION = "postRegion";
    public static final String TASK_GETPLACE = "getPlace";
    public static final String TASK_POSTPLACE = "postPlace";
    public static final String TASK_GETGRAVE = "getGrave";
    public static final String TASK_POSTGRAVE = "postGrave";
    public static final String TASK_GETBURIAL = "getBurial";
    public static final String TASK_POSTPHOTO = "postPhoto";
    public static final String TASK_REMOVEPHOTO = "removePhoto";
    
        
    //private static String DefaultServerAddress = "http://192.168.53.11:8000";
    private static String DefaultServerAddress = "http://pd2.pohoronnoedelo.ru";
    private static String RelativeUploadGravePhotoUrl = "/mobile/uploadgravephoto/";
    private static String RelativeRemoveGravePhotoUrl = "/mobile/removegravephoto/";
    private static String RelativeUploadPlacePhotoUrl = "/mobile/uploadplacephoto/";
    private static String RelativeRemovePlacePhotoUrl = "/mobile/removeplacephoto/";
    private static String RelativeLoginUrl = "/login/";
    private static String RelativeGetCemeteryDataUrl = "/mobile/getcemetery/";
    private static String RelativeGetRegionDataUrl = "/mobile/getarea/";
    private static String RelativeGetPlaceDataUrl = "/mobile/getplace/";
    private static String RelativeGetGraveDataUrl = "/mobile/getgrave/";
    private static String RelativeGetBurialDataUrl = "/mobile/getburial/";
    
    private static String RelativeUploadCemeteryDataUrl = "/mobile/uploadcemetery/";
    private static String RelativeUploadRegionDataUrl = "/mobile/uploadarea/";
    private static String RelativeUploadPlaceDataUrl = "/mobile/uploadplace/";
    private static String RelativeUploadGraveDataUrl = "/mobile/uploadgrave/";
    
    private static final String StorageDirPhoto = "MonumentPhoto";
    private static Location CurrentLocation;
    
    private static String pdSession;
    
    public static void setPDSession(String session){
    	pdSession = session;
    }
    
    public static String getPDSession(){
    	return pdSession;
    }
    
    public static Location getCurrentLocation(){
    	return CurrentLocation;
    }
    
    public static void setCurrentLocation(Location newLocation){
    	CurrentLocation = newLocation;
    }
    
    public static String getUploadGravePhotoUrl(Context context){
    	String serverAddress = getServerAddress(context);
    	return serverAddress + RelativeUploadGravePhotoUrl;
    }
    
    public static String getRemoveGravePhotoUrl(Context context){
    	String serverAddress = getServerAddress(context);
    	return serverAddress + RelativeRemoveGravePhotoUrl;
    }
    
    public static String getUploadPlacePhotoUrl(Context context){
        String serverAddress = getServerAddress(context);
        return serverAddress + RelativeUploadPlacePhotoUrl;
    }
    
    public static String getRemovePlacePhotoUrl(Context context){
        String serverAddress = getServerAddress(context);
        return serverAddress + RelativeRemovePlacePhotoUrl;
    }
    
    public static String getLoginUrl(Context context){
    	String serverAddress = getServerAddress(context);
    	return serverAddress + RelativeLoginUrl;
    }
    
    public static String getCemeteryUrl(Context context){
    	String serverAddress = getServerAddress(context);
    	return serverAddress + RelativeGetCemeteryDataUrl;
    }
    
    public static String getRegionUrl(Context context){
    	String serverAddress = getServerAddress(context);
    	return serverAddress + RelativeGetRegionDataUrl;
    }
    
    public static String getPlaceUrl(Context context){
    	String serverAddress = getServerAddress(context);
    	return serverAddress + RelativeGetPlaceDataUrl;
    }
    
    public static String getGraveUrl(Context context){
    	String serverAddress = getServerAddress(context);
    	return serverAddress + RelativeGetGraveDataUrl;
    }
    
    public static String getBurialUrl(Context context){
    	String serverAddress = getServerAddress(context);
    	return serverAddress + RelativeGetBurialDataUrl;
    }
    
    public static String getUploadCemeteryUrl(Context context){
    	String serverAddress = getServerAddress(context);
    	return serverAddress + RelativeUploadCemeteryDataUrl;
    }
    
    public static String getUploadRegionUrl(Context context){
    	String serverAddress = getServerAddress(context);
    	return serverAddress + RelativeUploadRegionDataUrl;
    }
    
    public static String getUploadPlaceUrl(Context context){
    	String serverAddress = getServerAddress(context);
    	return serverAddress + RelativeUploadPlaceDataUrl;
    }
    
    public static String getUploadGraveUrl(Context context){
    	String serverAddress = getServerAddress(context);
    	return serverAddress + RelativeUploadGraveDataUrl;
    }
    
    public static String getStorageDirPhoto(){
    	return StorageDirPhoto;
    }
    
    public static File getRootDirPhoto() {
    	boolean isExternalStorage = false;
    	if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			isExternalStorage = true;
    	}
    	File rootDir = null;
    	if(isExternalStorage){
    		rootDir = new File(Environment.getExternalStorageDirectory(), Settings.getStorageDirPhoto());
    	} else {
    		rootDir = new File(Environment.getDataDirectory(), Settings.getStorageDirPhoto());
    	}
		if (!rootDir.exists()) {
			if (!rootDir.mkdirs()) {
				return null;
			}
		}
		return rootDir;
	}
    
    private static String getServerAddress(Context context){
    	SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);    	
        String serverAddress = settings.getString(PREFS_SERVERADDRES_KEY, null);        
        return serverAddress;
    }
    
    public static SettingsData getSettingData(Context context){
    	SettingsData data = new SettingsData();
    	SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
    	data.Login = settings.getString(PREFS_LOGIN_KEY, null);
        data.Password = settings.getString(PREFS_PASSWORD_KEY, null);
        data.ServerAddress = settings.getString(PREFS_SERVERADDRES_KEY, null);
        data.GPSInterval = settings.getInt(PREFS_GPSINTERVAL_KEY, 0);
        data.IsAutoSendPhoto = settings.getBoolean(PREFS_ISAUTOSENDPHOTO_KEY, false);
        data.IsAutoDownloadData = settings.getBoolean(PREFS_ISAUTODOWNLOADDATA_KEY, false);
        data.IsOldPlaceName = settings.getBoolean(PREFS_ISOLDPLACENAME_KEY, false);
        if(data.ServerAddress == null){
        	data.ServerAddress = DefaultServerAddress;
        	saveSettingsData(context, data);
        }
        return data;
    }
    
    public static boolean IsAutoSendPhotoToServer(Context context){
    	SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
    	boolean isAutoSendPhoto = settings.getBoolean(PREFS_ISAUTOSENDPHOTO_KEY, false);
    	return isAutoSendPhoto;
    	
    }
    
    public static boolean IsOldPlaceNameOption(Context context){
    	SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
    	boolean isOldPlaceName = settings.getBoolean(PREFS_ISOLDPLACENAME_KEY, false);
    	return isOldPlaceName;
    	
    }
    
    public static boolean IsAutoDownloadData(Context context){
    	SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
    	boolean isAutoDownloadData = settings.getBoolean(PREFS_ISAUTODOWNLOADDATA_KEY, false);
    	return isAutoDownloadData;    	
    }
    
    public static int getGPSInterval(Context context){
    	SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
    	int GPSInterval = settings.getInt(PREFS_GPSINTERVAL_KEY, 0);
    	return GPSInterval;
    	
    }
    
    public static void saveSettingsData(Context context, SettingsData settingData){
    	SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);    	
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREFS_LOGIN_KEY, settingData.Login);
        editor.putString(PREFS_PASSWORD_KEY, settingData.Password);
        editor.putString(PREFS_SERVERADDRES_KEY, settingData.ServerAddress);
        editor.putInt(PREFS_GPSINTERVAL_KEY, settingData.GPSInterval);
        editor.putBoolean(PREFS_ISAUTOSENDPHOTO_KEY, settingData.IsAutoSendPhoto);
        editor.putBoolean(PREFS_ISAUTODOWNLOADDATA_KEY, settingData.IsAutoDownloadData);
        editor.putBoolean(PREFS_ISOLDPLACENAME_KEY, settingData.IsOldPlaceName);
        editor.commit();
    }   
        
    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public static boolean isWiFi(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
    }

    public static String getCurrentVersion(Context context) {
        String version = "";
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            version = pInfo.versionName;            
        } catch (PackageManager.NameNotFoundException e) {
            
        }
        return version;
    }
    
    public static boolean checkWorkOfGPS(final Context context, final ISettings settings){
    	LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    	boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    	if(!statusOfGPS){
	    	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
	    	    @Override
	    	    public void onClick(DialogInterface dialog, int which) {
	    	        switch (which){
	    	        case DialogInterface.BUTTON_POSITIVE:
	    	        	context.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
	    	        	if(settings != null){
	    	        		settings.onCloseCheckGPS(true);
	    	        	}
	    	            break;
	    	        case DialogInterface.BUTTON_NEGATIVE:
	    	        	if(settings != null){
	    	        		settings.onCloseCheckGPS(false);
	    	        	}
	    	            break;
	    	        }
	    	    }
	    	};	
	    	AlertDialog.Builder builder = new AlertDialog.Builder(context);
	    	builder.setMessage(context.getString(R.string.turnOnGPSQuestion)).setPositiveButton(context.getString(R.string.yes), dialogClickListener)
	    	    .setNegativeButton(context.getString(R.string.no), dialogClickListener).show();
    	}
    	return statusOfGPS;
    }
    
    private static final int TWO_MINUTES = 1000 * 60 * 2;

    /** Determines whether one Location reading is better than the current Location fix
      * @param location  The new Location that you want to evaluate
      * @param currentBestLocation  The current Location fix, to which you want to compare the new one
      */
    public static boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
        // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private static boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
          return provider2 == null;
        }
        return provider1.equals(provider2);
    }
    
}
