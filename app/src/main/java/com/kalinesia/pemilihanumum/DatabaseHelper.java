package com.kalinesia.pemilihanumum;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "pemilu.db"; // database name
    private static final int DATABASE_VERSION = 1; // database version

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE if not exists tbl_ip(id INTEGER PRIMARY KEY AUTOINCREMENT, ip text NULL);"; // query create table
        Log.d("Data", "onCreate: " + sql);
        db.execSQL(sql); // mengeksekusi query
        sql = "INSERT INTO tbl_ip (ip) VALUES ('http://192.168.1.11/pemilu');"; // query insert into ke table ip
        db.execSQL(sql); // mengeksekusi query
    }
    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {

    }
}
