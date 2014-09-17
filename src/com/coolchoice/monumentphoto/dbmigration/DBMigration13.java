package com.coolchoice.monumentphoto.dbmigration;

import java.sql.SQLException;

import android.database.sqlite.SQLiteDatabase;

import com.coolchoice.monumentphoto.data.User;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DBMigration13 implements IDBMigrate {

    @Override
    public void migrate(SQLiteDatabase db, ConnectionSource connectionSource) {
        db.beginTransaction();
        try {
            db.execSQL("alter table cemetery add column OrgId INTEGER;");
            try {
	            TableUtils.createTable(connectionSource, User.class);
            } catch (SQLException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
            }
            db.setTransactionSuccessful();             
        } finally {
            db.endTransaction();
        }       
    }

}
