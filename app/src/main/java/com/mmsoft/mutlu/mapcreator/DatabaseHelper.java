package com.mmsoft.mutlu.mapcreator;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by MUTLU on 02/01/2016.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE = "locations.sqlite";
    private static final int SURUM =  1;
    private static final String TABLE_CREATE = "CREATE TABLE line (id INTEGER PRIMARY KEY AUTOINCREMENT, seferno INTEGER, latitude DOUBLE,longitude DOUBLE, kavsakno BOOLEAN DEFAULT 0)";
    private static final String TABLE_NAME = "line";

    SQLiteDatabase dtbs = null;

    public DatabaseHelper(Context con) {
        super(con, DATABASE, null, SURUM);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
        db.execSQL("INSERT INTO line VALUES (0,0,0.0,0.0,0)");
        this.dtbs = db;

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXIST"+TABLE_NAME);
        this.onCreate(db);
    }

    public void createTable(String road){
        dtbs = this.getWritableDatabase();
        dtbs.execSQL(road);
    }
    public void insertPoint(String table,int point, double lat, double lon){
        dtbs = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("seferno", point);
        values.put("latitude",lat);
        values.put("longitude",lon);
        dtbs.insert(table, null, values);

    }
    public  void closeCon(){
        dtbs.close();

    }
}
