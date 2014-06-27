package com.coolchoice.monumentphoto.dbmigration;

import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.support.ConnectionSource;

public class DBMigration12 implements IDBMigrate {

    @Override
    public void migrate(SQLiteDatabase db, ConnectionSource connectionSource) {
        db.beginTransaction();
        try {
            db.execSQL("alter table burial add column Row VARCHAR;");
            db.execSQL("alter table burial add column Region_id INTEGER;");
            db.execSQL("alter table burial add column Place_id INTEGER;");
            db.execSQL("alter table burial add column ResponsibleUser_id INTEGER;");            
            db.setTransactionSuccessful();             
        } finally {
            db.endTransaction();
        }       
    }

}
