package com.coolchoice.monumentphoto.dal;


import java.util.UUID;

import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;

public class DB{

    private static DatabaseHelper databaseHelper;

    public static DatabaseHelper db(){
        return databaseHelper;
    }
    
    public static <T> QueryBuilder<T,Integer> q(Class<T> clazz){
    	return db().dao(clazz).queryBuilder();
    }
    
    public static <T> RuntimeExceptionDao<T,Integer> dao(Class<T> clazz){
    	return db().dao(clazz);
    }

    public static void setContext(Context context){
        databaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
    }
    
    public static void release(){
        OpenHelperManager.releaseHelper();
        databaseHelper = null;
    }
}