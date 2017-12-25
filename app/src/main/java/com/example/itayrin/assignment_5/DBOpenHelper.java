package com.example.itayrin.assignment_5;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by itayrin on 12/4/2017.
 */

 public class DBOpenHelper extends SQLiteOpenHelper {

    //The database name as it will be saved
    public static final String DATA_BASE = "MyDataBase";

    //The version of the DB
    //when we want to update the DB we will set the version 1 up
    public static final int VERSION = 2;

    //Table name
    public static final String TABLE_NAME = "students";

    //the column names
    public static final String KEY_NAME = "name";
    public static final String KEY_AGE = "age";
    public static final String KEY_GRADE = "grade";
    public static final String KEY_ID = "_id";
    public static final String KEY_IMAGE = "Image";

    public DBOpenHelper(Context ctx) {
        super(ctx, DATA_BASE, null, VERSION);
    }

    /**
     * Here we make the create table call
     * this will be called when the database will need to be created
     *
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE " + TABLE_NAME);
        sql.append("(");
        sql.append(KEY_ID + " INTEGER PRIMARY KEY autoincrement,");
        sql.append(KEY_NAME + " TEXT,");
        sql.append(KEY_AGE + " INT,");
        sql.append(KEY_GRADE + " INT,");
        sql.append(KEY_IMAGE + " TEXT");

        sql.append(")");


        //Final SQL query     =>     create table students(_id integer primary key, Name text, Age int,Grade int))

        db.execSQL(sql.toString());
    }

    /**
     * Helper method that will help us to add items into the Database table
     *
     * @param obj
     */
    public void insertLine(StudentObject obj) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_NAME, obj.name);
        cv.put(KEY_AGE, Integer.toString(obj.age));
        cv.put(KEY_GRADE, Integer.toString(obj.grade));
        cv.put(KEY_IMAGE, obj.imageUrl);

        getWritableDatabase().insert(TABLE_NAME, null, cv);
    }

    /**
     * Helper method that will get us all related rows
     * here we added filter on data
     * and order by date
     *
     * @return
     */
    public Cursor getAllRows() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_NAME + " order by " + KEY_NAME, null);
        return cursor;
    }

    public Cursor getRowById(long _id) {
        SQLiteDatabase db = getReadableDatabase();
        String strId = Long.toString(_id);
        Cursor cursor = db.rawQuery("select * from " + TABLE_NAME + " where " + KEY_ID + " = '" + strId + "'", null);
        cursor.moveToFirst();
        return cursor;
    }

    /**
     * will be called when the database will need to be updated
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion == 2 && oldVersion == 1) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + KEY_IMAGE + " TEXT");
        }
    }

    public void debugDeleteAllRows(){
        SQLiteDatabase db = getReadableDatabase();
        db.execSQL("delete from "+ TABLE_NAME);
    }

}

