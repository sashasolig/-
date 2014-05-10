package com.coolchoice.monumentphoto.dal;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


import android.content.Context;
import android.content.pm.FeatureInfo;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.coolchoice.monumentphoto.ConfigureLog4J;
import com.coolchoice.monumentphoto.Settings;
import com.coolchoice.monumentphoto.data.Burial;
import com.coolchoice.monumentphoto.data.Cemetery;
import com.coolchoice.monumentphoto.data.DeletedObject;
import com.coolchoice.monumentphoto.data.GPSCemetery;
import com.coolchoice.monumentphoto.data.GPSGrave;
import com.coolchoice.monumentphoto.data.GPSPlace;
import com.coolchoice.monumentphoto.data.GPSRegion;
import com.coolchoice.monumentphoto.data.GPSRow;
import com.coolchoice.monumentphoto.data.Grave;
import com.coolchoice.monumentphoto.data.GravePhoto;
import com.coolchoice.monumentphoto.data.Place;
import com.coolchoice.monumentphoto.data.PlacePhoto;
import com.coolchoice.monumentphoto.data.Region;
import com.coolchoice.monumentphoto.data.ResponsibleUser;
import com.coolchoice.monumentphoto.data.Row;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
	
    private final String LOG_TAG = getClass().getSimpleName();
    
    public static final String DATABASE_NAME = "/mnt/sdcard/monument.db";
    //public static final String DATABASE_NAME = "monument.db";
    
    public static final int DATABASE_VERSION = 10;
    
    private Context context;

    private static final ArrayList<Class<?>> entityClassesArray = new ArrayList<Class<?>>();

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        
        entityClassesArray.add(ResponsibleUser.class);
        entityClassesArray.add(Cemetery.class);
        entityClassesArray.add(Region.class);
        entityClassesArray.add(Row.class);
        entityClassesArray.add(Place.class);
        entityClassesArray.add(Grave.class);
        entityClassesArray.add(GravePhoto.class);
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
            Log.e(LOG_TAG, "Unable to create datbases", e);
        }        
    }    

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVer, int newVer) {
        //context.deleteDatabase(DATABASE_NAME);
        //onCreate(db, connectionSource);
    	switch(oldVer){
	    	case 1:
	    		migrateDBFromVer1ToLast(db, connectionSource);
	    		break;
	    	case 2:
	    		migrateDBFromVer2ToLast(db, connectionSource);
	    		break;
	    	case 3:
	    		migrateDBFromVer3ToLast(db, connectionSource);
	    		break;
	    	case 4:
	    		migrateDBFromVer4ToLast(db, connectionSource);
	    		break;
	    	case 5:
	    		migrateDBFromVer5ToLast(db, connectionSource);
	    		break;
	    	case 6:
	    		migrateDBFromVer6ToLast(db, connectionSource);	    		
	    		break;
	    	case 7:
                migrateDBFromVer7ToLast(db, connectionSource);
                break;
	    	case 8:	    	
                migrateDBFromVer8ToLast(db, connectionSource);
                break;
	    	case 9:	    	
                migrateDBFromVer9ToLast(db, connectionSource);
                break;
	    	
    	}    	
    	
    }
    	
    private void migrateDBFromVer1ToLast(SQLiteDatabase db, ConnectionSource connectionSource){
    	dropDBTrigger(db);
    	db.beginTransaction();
        try {            	
			TableUtils.createTable(connectionSource, Burial.class);				
        	db.execSQL("alter table place add column OldName VARCHAR;");              
            db.setTransactionSuccessful();                
        } catch (SQLException e) {
			e.printStackTrace();
		} finally {
            db.endTransaction();
        }
        migrateDBFromVer2ToLast(db, connectionSource);
    }
    
    private void migrateDBFromVer2ToLast(SQLiteDatabase db, ConnectionSource connectionSource){
    	db.beginTransaction();
        try {            	
			TableUtils.createTable(connectionSource, DeletedObject.class);
            db.setTransactionSuccessful();                
        } catch (SQLException e) {
			e.printStackTrace();
		} finally {
            db.endTransaction();
        }
        migrateDBFromVer3ToLast(db, connectionSource);
    }
    
    private void migrateDBFromVer3ToLast(SQLiteDatabase db, ConnectionSource connectionSource){
    	db.beginTransaction();
        try {
        	db.execSQL("alter table cemetery add column RegionSyncDate VARCHAR;");
        	db.execSQL("alter table region add column PlaceSyncDate VARCHAR;");
        	db.execSQL("alter table region add column GraveSyncDate VARCHAR;");
        	db.execSQL("alter table region add column BurialSyncDate VARCHAR;");     	
            db.setTransactionSuccessful();               
        } finally {
            db.endTransaction();
        }
        migrateDBFromVer4ToLast(db, connectionSource);
    }
    
    private void migrateDBFromVer4ToLast(SQLiteDatabase db, ConnectionSource connectionSource){
    	db.beginTransaction();
        try {
        	db.execSQL("alter table grave add column IsMilitary SMALLINT;");
        	db.execSQL("alter table grave add column IsWrongFIO SMALLINT;");
        	db.setTransactionSuccessful();               
        } finally {
            db.endTransaction();
        }
        migrateDBFromVer5ToLast(db, connectionSource);
    }
    
    private void migrateDBFromVer5ToLast(SQLiteDatabase db, ConnectionSource connectionSource){
    	db.beginTransaction();
        try {
        	db.execSQL("alter table place add column Width DOUBLE PRECISION;");
        	db.execSQL("alter table place add column Length DOUBLE PRECISION;");
        	db.setTransactionSuccessful();               
        } finally {
            db.endTransaction();
        }
        
        RuntimeExceptionDao<Burial, Integer> burialDAO = DB.dao(Burial.class);
        List<Burial> burials = burialDAO.queryForAll();
        for(Burial b : burials){
        	b.toLowerCaseFIO();
        	burialDAO.update(b);
        }
        migrateDBFromVer6ToLast(db, connectionSource);
    }
    
    private void migrateDBFromVer6ToLast(SQLiteDatabase db, ConnectionSource connectionSource){
    	db.beginTransaction();
        try {
            db.execSQL("alter table burial add column ContainerType INTEGER;");
            
        	db.execSQL("alter table gpscemetery add column OrdinalNumber INTEGER;");
        	db.execSQL("alter table gpscemetery add column ServerId INTEGER;");
        	db.execSQL("alter table gpsregion add column OrdinalNumber INTEGER;");
        	db.execSQL("alter table gpsregion add column ServerId INTEGER;");
        	db.execSQL("alter table gpsrow add column OrdinalNumber INTEGER;");
        	db.execSQL("alter table gpsrow add column ServerId INTEGER;");
        	db.execSQL("alter table gpsplace add column OrdinalNumber INTEGER;");
        	db.execSQL("alter table gpsplace add column ServerId INTEGER;");
        	db.execSQL("alter table gpsgrave add column OrdinalNumber INTEGER;");
        	db.execSQL("alter table gpsgrave add column ServerId INTEGER;");
        	
        	db.execSQL("alter table cemetery add column IsGPSChanged TINYINT;");
        	db.execSQL("update cemetery set IsGPSChanged = 0;");
        	db.execSQL("alter table region add column IsGPSChanged TINYINT;");
        	db.execSQL("update region set IsGPSChanged = 0;");
        	
        	db.execSQL("delete from gpscemetery;");
        	db.execSQL("delete from gpsregion;");
        	db.execSQL("delete from gpsrow;");
        	db.execSQL("delete from gpsplace;");
        	db.execSQL("delete from gpsgrave;");
        	
        	db.setTransactionSuccessful();               
        } finally {
            db.endTransaction();
        }
        migrateDBFromVer7ToLast(db, connectionSource);
    }
    
    private void migrateDBFromVer7ToLast(SQLiteDatabase db, ConnectionSource connectionSource){
        db.beginTransaction();
        try {
        	db.execSQL("alter table place add column MilitaryDate VARCHAR;");
            db.execSQL("alter table place add column WrongFIODate VARCHAR;");
            db.execSQL("alter table place add column SizeViolatedDate VARCHAR;");
            db.execSQL("alter table place add column UnindentifiedDate VARCHAR;");
            db.execSQL("alter table place add column UnownedDate VARCHAR;");
            db.execSQL("alter table place add column ResponsibleUser_id INTEGER;");
            TableUtils.createTable(connectionSource, PlacePhoto.class);
            TableUtils.createTable(connectionSource, ResponsibleUser.class);                        
            db.setTransactionSuccessful();                
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        migrateDBFromVer8ToLast(db, connectionSource);
    }
    
    private void migrateDBFromVer8ToLast(SQLiteDatabase db, ConnectionSource connectionSource){
        File file = new File(Environment.getExternalStorageDirectory() + File.separator + "MonumentPhoto" + File.separator);
        File newFile = new File(Environment.getExternalStorageDirectory() + File.separator + Settings.getStorageDirPhoto() + File.separator);
        if(file.exists()){
            if(newFile.exists()){
                deleteFolder(newFile);
            }
            boolean success = file.renameTo(newFile);
            ConfigureLog4J.configure();
            if(success){
                db.beginTransaction();
                try {
                    db.execSQL("update placephoto set UriString = replace(UriString , '/MonumentPhoto/','/MobileKeeper/');");
                    db.execSQL("update gravephoto set UriString = replace(UriString , '/MonumentPhoto/','/MobileKeeper/');");                                  
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }
        }
        migrateDBFromVer9ToLast(db, connectionSource);
    }
    
    private void migrateDBFromVer9ToLast(SQLiteDatabase db, ConnectionSource connectionSource){
    	db.beginTransaction();
        try {
        	db.execSQL("alter table cemetery add column Square DOUBLE PRECISION;");
        	db.execSQL("alter table region add column Square DOUBLE PRECISION;");
        	db.setTransactionSuccessful();               
        } finally {
            db.endTransaction();
        }
    }
    
    private void deleteFolder(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()){
            for (File child : fileOrDirectory.listFiles()){
                deleteFolder(child);
            }
        }
        fileOrDirectory.delete();
    }
    
    public void delete(){
        context.deleteDatabase(DATABASE_NAME);
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
    
    public void dropDBTrigger(SQLiteDatabase db){
    	if(db == null){
    	    db = getWritableDatabase();
    	}
    	//cemetery
    	try{
    		db.execSQL("DROP trigger tr_cemetery_update;");
    	}catch(Exception exc){
    		Log.e("dropTrigger tr_cemetery_update", exc.getLocalizedMessage());
    	}
    	try{
    		db.execSQL("DROP trigger tr_cemetery_insert;");
    	}catch(Exception exc){
    		Log.e("dropTrigger tr_cemetery_insert", exc.getLocalizedMessage());
    	}
    	
    	//region
    	try{
    		db.execSQL("DROP trigger tr_region_update;");
    	}catch(Exception exc){
    		Log.e("dropTrigger tr_region_update", exc.getLocalizedMessage());
    	}
    	try{
    		db.execSQL("DROP trigger tr_region_insert;");
    	}catch(Exception exc){
    		Log.e("dropTrigger tr_region_insert", exc.getLocalizedMessage());
    	}
    	
    	//row
    	try{
    		db.execSQL("DROP trigger tr_row_update;");
    	}catch(Exception exc){
    		Log.e("dropTrigger tr_row_update", exc.getLocalizedMessage());
    	}
    	
    	//place
    	try{
    		db.execSQL("DROP trigger tr_place_update;");
    	}catch(Exception exc){
    		Log.e("dropTrigger tr_place_update", exc.getLocalizedMessage());
    	}
    	try{
    		db.execSQL("DROP trigger tr_place_insert;");
    	}catch(Exception exc){
    		Log.e("dropTrigger tr_place_insert", exc.getLocalizedMessage());
    	}
    	
    	//grave
    	try{
    		db.execSQL("DROP trigger tr_grave_update;");
    	}catch(Exception exc){
    		Log.e("dropTrigger tr_grave_update", exc.getLocalizedMessage());
    	}
    	try{
    		db.execSQL("DROP trigger tr_grave_insert;");
    	}catch(Exception exc){
    		Log.e("dropTrigger tr_grave_insert", exc.getLocalizedMessage());
    	}
    }
    
    /*public void createDBTrigger(){
    	SQLiteDatabase sqLiteDatabase = getWritableDatabase();
    	//cemetery
    	try{
    		sqLiteDatabase.execSQL("CREATE TRIGGER tr_cemetery_update after UPDATE ON cemetery BEGIN  UPDATE cemetery SET IsChanged = 1 WHERE Id = old.Id and old.Name != new.Name; END;");
    	}catch(Exception exc){
    		Log.e("createTrigger tr_cemetery_update", exc.getLocalizedMessage());
    	}
    	try{
    		sqLiteDatabase.execSQL("CREATE TRIGGER tr_cemetery_insert after INSERT ON cemetery  BEGIN   UPDATE cemetery SET IsChanged = 1 WHERE Id = new.Id;  END;");
    	}catch(Exception exc){
    		Log.e("createTrigger tr_cemetery_insert", exc.getLocalizedMessage());
    	}
    	
    	//region
    	try{
    		sqLiteDatabase.execSQL("CREATE TRIGGER tr_region_update after UPDATE ON region  BEGIN    UPDATE region SET IsChanged = 1 WHERE Id = new.Id and (old.Name != new.Name or old.Cemetery_id != new.Cemetery_id);  END;");
    	}catch(Exception exc){
    		Log.e("createTrigger tr_region_update", exc.getLocalizedMessage());
    	}
    	try{
    		sqLiteDatabase.execSQL("CREATE TRIGGER tr_region_insert after INSERT ON region  BEGIN    UPDATE region SET IsChanged = 1 WHERE Id = new.Id;  END;");
    	}catch(Exception exc){
    		Log.e("createTrigger tr_region_insert", exc.getLocalizedMessage());
    	}
    	
    	//row
    	try{
    		sqLiteDatabase.execSQL("CREATE TRIGGER tr_row_update after UPDATE ON row  BEGIN    UPDATE place SET IsChanged = 1 WHERE Row_id = new.Id;  END;");
    	}catch(Exception exc){
    		Log.e("createTrigger tr_row_update", exc.getLocalizedMessage());
    	}
    	    	
    	//place
    	try{
    		sqLiteDatabase.execSQL("CREATE TRIGGER tr_place_update after UPDATE ON place  BEGIN    UPDATE place SET IsChanged = 1 WHERE Id = new.Id and (old.Name != new.Name  or old.IsOwnerLess != new.IsOwnerLess  or old.Row_id != new.Row_id or old.Region_id != new.Region_id);  END;");
    	}catch(Exception exc){
    		Log.e("createTrigger tr_place_update", exc.getLocalizedMessage());
    	}
    	try{
    		sqLiteDatabase.execSQL("CREATE TRIGGER tr_place_insert after INSERT ON place  BEGIN    UPDATE place SET IsChanged = 1 WHERE Id = new.Id;  END;");
    	}catch(Exception exc){
    		Log.e("createTrigger tr_place_insert", exc.getLocalizedMessage());
    	}
    	
    	//grave
    	try{
    		sqLiteDatabase.execSQL("CREATE TRIGGER tr_grave_update after UPDATE ON grave  BEGIN    UPDATE grave SET IsChanged = 1 WHERE Id = new.Id and (old.Name != new.Name or old.Place_id != new.Place_id);  END;");
    	}catch(Exception exc){
    		Log.e("createTrigger tr_grave_update", exc.getLocalizedMessage());
    	}
    	try{
    		sqLiteDatabase.execSQL("CREATE TRIGGER tr_grave_insert after INSERT ON grave  BEGIN    UPDATE grave SET IsChanged = 1 WHERE Id = new.Id;  END;");
    	}catch(Exception exc){
    		Log.e("createTrigger tr_grave_insert", exc.getLocalizedMessage());
    	}
    	
    }*/
    
    public void execManualSQL(String sqlString){
    	SQLiteDatabase sqLiteDatabase = getWritableDatabase();
    	try{
    		sqLiteDatabase.execSQL(sqlString);
    	}catch(Exception exc){
    		Log.e("execManualSQL =" + sqlString, exc.getLocalizedMessage());
    	}
    }
}
