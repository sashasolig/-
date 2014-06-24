package com.coolchoice.monumentphoto.dbmigration;

import java.io.File;

import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.coolchoice.monumentphoto.ConfigureLog4J;
import com.coolchoice.monumentphoto.Settings;
import com.j256.ormlite.support.ConnectionSource;

public class DBMigration8 implements IDBMigrate {

    @Override
    public void migrate(SQLiteDatabase db, ConnectionSource connectionSource) {
        File file = new File(Environment.getExternalStorageDirectory() + File.separator + "MonumentPhoto" + File.separator);
        File newFile = new File(Environment.getExternalStorageDirectory() + File.separator + Settings.getStorageDirPhoto() + File.separator);
        if(file.exists()){
            if(newFile.exists()){
                deleteFolder(newFile);
            }
            boolean success = file.renameTo(newFile);
            try{
                ConfigureLog4J.configure();
            }catch(Exception exc){
                Log.e("ConfigureLog4J", "ConfigureLog4J", exc);
            }
            if(success){
                db.beginTransaction();
                try {
                    db.execSQL("update placephoto set UriString = replace(UriString , '/MonumentPhoto/','/MobileKeeper/');");                                                     
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }
        }
        
    }
    
    private void deleteFolder(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()){
            for (File child : fileOrDirectory.listFiles()){
                deleteFolder(child);
            }
        }
        fileOrDirectory.delete();
    }  

}
