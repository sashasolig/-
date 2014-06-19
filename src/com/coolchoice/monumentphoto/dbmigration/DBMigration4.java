package com.coolchoice.monumentphoto.dbmigration;

import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.support.ConnectionSource;

public class DBMigration4 implements IDBMigrate {

    @Override
    public void migrate(SQLiteDatabase db, ConnectionSource connectionSource) {
        db.beginTransaction();
        try {
            db.execSQL("alter table grave add column IsMilitary SMALLINT;");
            db.execSQL("alter table grave add column IsWrongFIO SMALLINT;");
            db.setTransactionSuccessful();               
        } finally {
            db.endTransaction();
        }
        
    }

}
