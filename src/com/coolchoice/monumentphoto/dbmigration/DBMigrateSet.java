package com.coolchoice.monumentphoto.dbmigration;

import java.util.ArrayList;

import android.database.sqlite.SQLiteDatabase;

import com.coolchoice.monumentphoto.dal.DatabaseHelper;
import com.j256.ormlite.support.ConnectionSource;

public class DBMigrateSet {
    
    private ArrayList<IDBMigrate> mMigrations = new ArrayList<IDBMigrate>();
    
    private int mFromVersion;
    
    private SQLiteDatabase mSQLiteDatabase;
    
    private ConnectionSource mConnectionSource;
    
    public DBMigrateSet(int fromVersion, SQLiteDatabase db, ConnectionSource connectionSource){
        this.mFromVersion = fromVersion;
        this.mSQLiteDatabase = db;
        this.mConnectionSource = connectionSource;
        
        mMigrations.add(new DBMigration1());
        mMigrations.add(new DBMigration2());
        mMigrations.add(new DBMigration3());
        mMigrations.add(new DBMigration4());
        mMigrations.add(new DBMigration5());
        mMigrations.add(new DBMigration6());
        mMigrations.add(new DBMigration7());
        mMigrations.add(new DBMigration8());
        mMigrations.add(new DBMigration9());
        mMigrations.add(new DBMigration10());
        mMigrations.add(new DBMigration11());
        mMigrations.add(new DBMigration12());
        
        
        if((DatabaseHelper.DATABASE_VERSION - 1) != this.mMigrations.size()){
            throw new RuntimeException("Wrong count of DB migration has declared");
        }
    }
    
    public void startMigrate(){
        for(int i = (this.mFromVersion - 1); i < this.mMigrations.size(); i++){
            IDBMigrate dbMigrate = this.mMigrations.get(i);
            dbMigrate.migrate(this.mSQLiteDatabase, this.mConnectionSource);
        }
    }

}
