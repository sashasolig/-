package com.coolchoice.monumentphoto.dal;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.coolchoice.monumentphoto.data.Burial;
import com.coolchoice.monumentphoto.data.Cemetery;
import com.coolchoice.monumentphoto.data.DeletedObject;
import com.coolchoice.monumentphoto.data.GPSCemetery;
import com.coolchoice.monumentphoto.data.GPSGrave;
import com.coolchoice.monumentphoto.data.GPSPlace;
import com.coolchoice.monumentphoto.data.GPSRegion;
import com.coolchoice.monumentphoto.data.GPSRow;
import com.coolchoice.monumentphoto.data.Grave;
import com.coolchoice.monumentphoto.data.Place;
import com.coolchoice.monumentphoto.data.PlacePhoto;
import com.coolchoice.monumentphoto.data.Region;
import com.coolchoice.monumentphoto.data.ResponsibleUser;
import com.coolchoice.monumentphoto.data.Row;
import com.coolchoice.monumentphoto.dbmigration.DBMigrateSet;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
	
    private final String LOG_TAG = getClass().getSimpleName();
    
    //public static final String DATABASE_NAME = "/mnt/sdcard/monument.db";
    public static final String DATABASE_NAME = "monument.db";
    
    public static final int DATABASE_VERSION = 13;
    
    private static final ArrayList<Class<?>> entityClassesArray = new ArrayList<Class<?>>();

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        
        entityClassesArray.add(ResponsibleUser.class);
        entityClassesArray.add(Cemetery.class);
        entityClassesArray.add(Region.class);
        entityClassesArray.add(Row.class);
        entityClassesArray.add(Place.class);
        entityClassesArray.add(Grave.class);        
        entityClassesArray.add(PlacePhoto.class);
        entityClassesArray.add(Burial.class);
        entityClassesArray.add(GPSCemetery.class);
        entityClassesArray.add(GPSRegion.class);
        entityClassesArray.add(GPSRow.class);
        entityClassesArray.add(GPSPlace.class);
        entityClassesArray.add(GPSGrave.class);
        
        
        entityClassesArray.add(DeletedObject.class);                
    }
    
    @SuppressWarnings("rawtypes")
    private static final Map<Class, RuntimeExceptionDao<?,Integer>> sDaoClassMap = new HashMap<Class, RuntimeExceptionDao<?,Integer>>();
    
    public <T> RuntimeExceptionDao<T,Integer> dao(Class<T> clazz){
        // Retrieve the common DAO for the entity class
    	@SuppressWarnings({ "unchecked" })
        RuntimeExceptionDao<T,Integer> dao = (RuntimeExceptionDao<T,Integer>) sDaoClassMap.get(clazz);
        if(dao == null) {
        	dao = getRuntimeExceptionDao(clazz);
        	dao.setObjectCache(false);
            sDaoClassMap.put(clazz, dao);
        }
        return dao;
    }
	
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        try {
        	for(Class<?> c : entityClassesArray){
        		TableUtils.createTable(connectionSource, c);
            }
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Unable to create databases", e);
        }        
    }    

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVer, int newVer) {        
    	DBMigrateSet dbMigrateSet = new DBMigrateSet(oldVer, db, connectionSource);
    	dbMigrateSet.startMigrate(); 	
    }
    
    
    public void clearTables(){
    	try {
        	for(Class<?> c : entityClassesArray){
        		TableUtils.clearTable(connectionSource, c);
            }
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Unable to clear databases", e);
        }
    }
    
    public void updateDBLink(){
    	SQLiteDatabase sqLiteDatabase = getWritableDatabase();
    	sqLiteDatabase.execSQL("update region set Cemetery_id = (select Id from cemetery where cemetery.ServerId = region.ParentServerId) where region.Cemetery_id is null;");
    	sqLiteDatabase.execSQL("update row set Region_id = (select Id from region where region.ServerId = row.ParentServerId) where row.Region_id is null;");
    	sqLiteDatabase.execSQL("update place set Region_id = (select Id from region where region.ServerId = place.ParentServerId ) where Row_id is null and Region_id is null;");
    	sqLiteDatabase.execSQL("update grave set Place_id = (select Id from place where place.ServerId = grave.ParentServerId ) where Place_Id is null;");
    	sqLiteDatabase.execSQL("update burial set Grave_id = (select Id from grave where grave.ServerId = burial.ParentServerId ) where Grave_Id is null;");
    }
            
    public void execManualSQL(String sqlString){
    	SQLiteDatabase sqLiteDatabase = getWritableDatabase();
    	try{
    		sqLiteDatabase.execSQL(sqlString);
    	}catch(Exception exc){
    		Log.e("execManualSQL =" + sqlString, exc.getLocalizedMessage());
    	}
    }
}
