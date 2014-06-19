package com.coolchoice.monumentphoto.dbmigration;

import java.util.List;

import android.database.sqlite.SQLiteDatabase;

import com.coolchoice.monumentphoto.dal.DB;
import com.coolchoice.monumentphoto.data.Burial;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;

public class DBMigration5 implements IDBMigrate {

    @Override
    public void migrate(SQLiteDatabase db, ConnectionSource connectionSource) {
        db.beginTransaction();
        try {
            db.execSQL("alter table place add column Width DOUBLE PRECISION;");
            db.execSQL("alter table place add column Length DOUBLE PRECISION;");
            db.setTransactionSuccessful();               
        } finally {
            db.endTransaction();
        }        
        RuntimeExceptionDao<Burial, Integer> burialDAO = DB.dao(Burial.class);
        List<Burial> burials = burialDAO.queryForAll();
        for(Burial b : burials){
            b.toLowerCaseFIO();
            burialDAO.update(b);
        }        
    }

}
