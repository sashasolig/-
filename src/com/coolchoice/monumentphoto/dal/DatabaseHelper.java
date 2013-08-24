package com.coolchoice.monumentphoto.dal;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.coolchoice.monumentphoto.data.Cemetery;
import com.coolchoice.monumentphoto.data.GPSCemetery;
import com.coolchoice.monumentphoto.data.GPSGrave;
import com.coolchoice.monumentphoto.data.GPSPlace;
import com.coolchoice.monumentphoto.data.GPSRegion;
import com.coolchoice.monumentphoto.data.GPSRow;
import com.coolchoice.monumentphoto.data.Grave;
import com.coolchoice.monumentphoto.data.Monument;
import com.coolchoice.monumentphoto.data.GravePhoto;
import com.coolchoice.monumentphoto.data.Place;
import com.coolchoice.monumentphoto.data.Region;
import com.coolchoice.monumentphoto.data.Row;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
	
    private final String LOG_TAG = getClass().getSimpleName();
    
    //public static final String DATABASE_NAME = "/mnt/sdcard/monument.db";
    public static final String DATABASE_NAME = "monument.db";
    
    public static final int DATABASE_VERSION = 1;
    
    private Context context;

    private static final ArrayList<Class<?>> entityClassesArray = new ArrayList<Class<?>>();

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        
        entityClassesArray.add(Cemetery.class);
        entityClassesArray.add(Region.class);
        entityClassesArray.add(Row.class);
        entityClassesArray.add(Place.class);
        entityClassesArray.add(Grave.class);
        entityClassesArray.add(GravePhoto.class);
        entityClassesArray.add(GPSCemetery.class);
        entityClassesArray.add(GPSRegion.class);
        entityClassesArray.add(GPSRow.class);
        entityClassesArray.add(GPSPlace.class);
        entityClassesArray.add(GPSGrave.class);
                
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
        context.deleteDatabase(DATABASE_NAME);
        onCreate(db, connectionSource);
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
    }
    
    public void dropDBTrigger(){
    	SQLiteDatabase sqLiteDatabase = getWritableDatabase();
    	//cemetery
    	try{
    		sqLiteDatabase.execSQL("DROP trigger tr_cemetery_update;");
    	}catch(Exception exc){
    		Log.e("dropTrigger tr_cemetery_update", exc.getLocalizedMessage());
    	}
    	try{
    		sqLiteDatabase.execSQL("DROP trigger tr_cemetery_insert;");
    	}catch(Exception exc){
    		Log.e("dropTrigger tr_cemetery_insert", exc.getLocalizedMessage());
    	}
    	
    	//region
    	try{
    		sqLiteDatabase.execSQL("DROP trigger tr_region_update;");
    	}catch(Exception exc){
    		Log.e("dropTrigger tr_region_update", exc.getLocalizedMessage());
    	}
    	try{
    		sqLiteDatabase.execSQL("DROP trigger tr_region_insert;");
    	}catch(Exception exc){
    		Log.e("dropTrigger tr_region_insert", exc.getLocalizedMessage());
    	}
    	
    	//row
    	try{
    		sqLiteDatabase.execSQL("DROP trigger tr_row_update;");
    	}catch(Exception exc){
    		Log.e("dropTrigger tr_row_update", exc.getLocalizedMessage());
    	}
    	
    	//place
    	try{
    		sqLiteDatabase.execSQL("DROP trigger tr_place_update;");
    	}catch(Exception exc){
    		Log.e("dropTrigger tr_place_update", exc.getLocalizedMessage());
    	}
    	try{
    		sqLiteDatabase.execSQL("DROP trigger tr_place_insert;");
    	}catch(Exception exc){
    		Log.e("dropTrigger tr_place_insert", exc.getLocalizedMessage());
    	}
    	
    	//grave
    	try{
    		sqLiteDatabase.execSQL("DROP trigger tr_grave_update;");
    	}catch(Exception exc){
    		Log.e("dropTrigger tr_grave_update", exc.getLocalizedMessage());
    	}
    	try{
    		sqLiteDatabase.execSQL("DROP trigger tr_grave_insert;");
    	}catch(Exception exc){
    		Log.e("dropTrigger tr_grave_insert", exc.getLocalizedMessage());
    	}
    }
    
    public void createDBTrigger(){
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
