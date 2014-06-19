package com.coolchoice.monumentphoto.dbmigration;

import java.sql.SQLException;

import android.database.sqlite.SQLiteDatabase;

import com.coolchoice.monumentphoto.data.PlacePhoto;
import com.coolchoice.monumentphoto.data.ResponsibleUser;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DBMigration7 implements IDBMigrate {

    @Override
    public void migrate(SQLiteDatabase db, ConnectionSource connectionSource) {
        db.beginTransaction();
        try {
            db.execSQL("alter table place add column MilitaryDate VARCHAR;");
            db.execSQL("alter table place add column WrongFIODate VARCHAR;");
            db.execSQL("alter table place add column SizeViolatedDate VARCHAR;");
            db.execSQL("alter table place add column UnindentifiedDate VARCHAR;");
            db.execSQL("alter table place add column UnownedDate VARCHAR;");
            db.execSQL("alter table place add column ResponsibleUser_id INTEGER;");
            TableUtils.createTable(connectionSource, PlacePhoto.class);
            TableUtils.createTable(connectionSource, ResponsibleUser.class);                        
            db.setTransactionSuccessful();                
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        
    }

}
