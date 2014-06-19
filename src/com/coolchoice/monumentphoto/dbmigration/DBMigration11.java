package com.coolchoice.monumentphoto.dbmigration;

import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.support.ConnectionSource;

public class DBMigration11 implements IDBMigrate {

    @Override
    public void migrate(SQLiteDatabase db, ConnectionSource connectionSource) {
        db.beginTransaction();
        try {
            db.execSQL("alter table burial add column PlanDate VARCHAR;");
            db.execSQL("alter table burial add column Cemetery_id INTEGER ;");            
            db.setTransactionSuccessful();               
        } finally {
            db.endTransaction();
        }       
    }

}
