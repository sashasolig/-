package com.coolchoice.monumentphoto.dbmigration;

import java.sql.SQLException;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.coolchoice.monumentphoto.data.Burial;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DBMigration1 implements IDBMigrate {

    @Override
    public void migrate(SQLiteDatabase db, ConnectionSource connectionSource) {
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
        
    }
    
    public void dropDBTrigger(SQLiteDatabase db){        
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

}
