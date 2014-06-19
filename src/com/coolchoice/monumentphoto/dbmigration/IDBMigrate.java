package com.coolchoice.monumentphoto.dbmigration;

import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.support.ConnectionSource;

public interface IDBMigrate {
    
    void migrate(SQLiteDatabase db, ConnectionSource connectionSource);

}
