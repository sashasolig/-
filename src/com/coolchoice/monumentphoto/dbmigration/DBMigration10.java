package com.coolchoice.monumentphoto.dbmigration;

import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.support.ConnectionSource;

public class DBMigration10 implements IDBMigrate {

    @Override
    public void migrate(SQLiteDatabase db, ConnectionSource connectionSource) {
        db.beginTransaction();
        try {            
            db.execSQL("alter table placephoto add column FileName VARCHAR;");
            db.execSQL("alter table placephoto add column ServerFileName VARCHAR;");
            db.execSQL("alter table placephoto add column ThumbnailUriString VARCHAR;");
            db.setTransactionSuccessful();               
        } finally {
            db.endTransaction();
        }
        
    }

}
