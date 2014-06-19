package com.coolchoice.monumentphoto.dbmigration;

import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.support.ConnectionSource;

public class DBMigration6 implements IDBMigrate {

    @Override
    public void migrate(SQLiteDatabase db, ConnectionSource connectionSource) {
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
    }

}
