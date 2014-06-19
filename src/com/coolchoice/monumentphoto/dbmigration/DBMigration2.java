package com.coolchoice.monumentphoto.dbmigration;

import java.sql.SQLException;

import android.database.sqlite.SQLiteDatabase;

import com.coolchoice.monumentphoto.data.DeletedObject;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DBMigration2 implements IDBMigrate {

    @Override
    public void migrate(SQLiteDatabase db, ConnectionSource connectionSource) {
        db.beginTransaction();
        try {               
            TableUtils.createTable(connectionSource, DeletedObject.class);
            db.setTransactionSuccessful();                
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        
    }

}
