package com.mmsoft.mutlu.mapcreator;

/**
 * Created by MUTLU on 18/12/2015.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;
import android.view.View;

public class Database extends SQLiteOpenHelper{

    private static final String DATABASE = "locations.sqlite";
    private static final int SURUM =  1;
    private static final String TABLE_CREATE = "CREATE TABLE line (id INTEGER PRIMARY KEY AUTOINCREMENT, point INTEGER, latitude DOUBLE,longitude DOUBLE)";
    private static final String TABLE_NAME = "line";
    SQLiteDatabase db;

    public Database(Context con){
        super(con,DATABASE,null,SURUM);

    }
    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(TABLE_CREATE);
        this.db = db;
    }

     public void createTable(String road){
        db.execSQL(road);

    }


    public void insertPoint(String table,int point, double lat, double lon){
        db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("point", point);
        values.put("latitude",lat);
        values.put("longitude",lon);
        db.insert(table, null, values);
        db.close();

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

        db.execSQL("DROP TABLE IF EXIST"+TABLE_NAME);
        this.onCreate(db);

    }

}
