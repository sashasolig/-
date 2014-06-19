package com.coolchoice.monumentphoto.dbmigration;

import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.support.ConnectionSource;

public class DBMigration9 implements IDBMigrate {

    @Override
    public void migrate(SQLiteDatabase db, ConnectionSource connectionSource) {
        db.beginTransaction();
        try {
            db.execSQL("alter table cemetery add column Square DOUBLE PRECISION;");
            db.execSQL("alter table region add column Square DOUBLE PRECISION;");
            db.setTransactionSuccessful();               
        } finally {
            db.endTransaction();
        }
        
    }

}
