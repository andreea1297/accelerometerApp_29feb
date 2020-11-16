package com.example.accelerometerapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;


public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Activitati.db";
    public static final String TABLE_NAME = "activitati_table";

    public static final String COL1_ID = "ID";
    public static final String COL2_activitate = "Activitate";
    public static final String COL3_timp = "Timp";
    public static final String COL4_x = "X";
    public static final String COL5_y = "Y";
    public static final String COL6_z = "Z";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table "+ TABLE_NAME + "(ID INTEGER PRIMARY KEY AUTOINCREMENT, Activitate TEXT, Timp TEXT, X TEXT, Y TEXT, Z TEXT)");


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(String activitate, String timp, float x, float y, float z){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL2_activitate, activitate);
        contentValues.put(COL3_timp, timp);
        contentValues.put(COL4_x, x);
        contentValues.put(COL5_y, y);
        contentValues.put(COL6_z, z);

        long result = db.insert(TABLE_NAME, null, contentValues);

        if (result == -1)
            return false;
        else
            return true;
    }

    public Cursor getAllData(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TABLE_NAME,null);
        return res;
    }


}
