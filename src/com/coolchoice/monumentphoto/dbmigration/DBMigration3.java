package com.coolchoice.monumentphoto.dbmigration;

import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.support.ConnectionSource;

public class DBMigration3 implements IDBMigrate {

    @Override
    public void migrate(SQLiteDatabase db, ConnectionSource connectionSource) {
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
        
    }

}
