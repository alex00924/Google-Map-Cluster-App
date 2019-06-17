package com.rol.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.rol.beans.LoginBean;

public class DBHelper extends SQLiteOpenHelper {

    final static String DB_NAME = "roldb.db";
    final static int DB_VERSION = 1;
    private static final String LOGIN_TABLE = "login_table";
    private static final String USER_ID = "user_id";
    private static final String FIRST_NAME = "first_name";
    private static final String LAST_NAME = "last_name";
    private static final String EMAIL = "email";
    private static final String PROFILE = "profile";
    private static final String AUTH_TOKEN = "auth_token";
    private static final String ABOUT = "about";
    private static final String BIRTH_DATE = "birth_date";
    private static final String GENDER = "gender";
    private static final String HOME_TOWN = "home_town";
    static DBHelper dbHelper;
    SQLiteDatabase database;
    String CREATE_LOGIN_TABLE = "CREATE TABLE " + LOGIN_TABLE + "(" + USER_ID + " TEXT," + FIRST_NAME + " TEXT,"
            + LAST_NAME + " TEXT," + EMAIL + " TEXT," + PROFILE + " TEXT," + AUTH_TOKEN + " TEXT," + ABOUT + " TEXT," + BIRTH_DATE + " TEXT," + GENDER + " TEXT," + HOME_TOWN + " TEXT)";

    public DBHelper(Context context, String name, CursorFactory factory,
                    int version) {
        super(context, name, factory, version);
    }

    public static DBHelper getInstance(Context context) {
        if (dbHelper == null) {

            dbHelper = new DBHelper(context, DB_NAME, null, DB_VERSION);
        }
        return dbHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_LOGIN_TABLE);

        Log.e("create login table", CREATE_LOGIN_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CREATE_LOGIN_TABLE);

        onCreate(db);
    }

    //add register data
    public void addRegisterData(LoginBean d) {
        ContentValues values = new ContentValues();
        values.put(USER_ID, d.user_id);
        values.put(FIRST_NAME, d.first_name);
        values.put(LAST_NAME, d.last_name);
        values.put(EMAIL, d.email);
        values.put(PROFILE, d.profile_picture);
        values.put(AUTH_TOKEN, d.auth_token);

        Log.e("add data", "inserted");

        database.insert(LOGIN_TABLE, null, values);
    }

    // Get user register  data
    public LoginBean getRegisterData() {
        LoginBean d = new LoginBean();

        Cursor cursor = database.query(LOGIN_TABLE, null, null, null,
                null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            d.user_id = cursor.getString(0);
            d.first_name = cursor.getString(1);
            d.last_name = cursor.getString(2);
            d.email = cursor.getString(3);
            d.profile_picture = cursor.getString(4);
            d.auth_token = cursor.getString(5);
            d.about = cursor.getString(6);
            d.birth_date = cursor.getString(7);
            d.gender = cursor.getString(8);
            d.home_town = cursor.getString(9);

            return d;
        }

        Log.e("register id", d.user_id + "");

        if (cursor != null) {
            cursor.close();
        }
        return d;
    }

    public void updateRegisterData(LoginBean d) {

        ContentValues values = new ContentValues();

        values.put(FIRST_NAME, d.first_name);
        values.put(LAST_NAME, d.last_name);
        values.put(EMAIL, d.email);
        values.put(PROFILE, d.profile_picture);
        values.put(ABOUT, d.about);
        values.put(BIRTH_DATE, d.birth_date);
        values.put(GENDER, d.gender);
        values.put(HOME_TOWN, d.home_town);

        database.update(LOGIN_TABLE, values, USER_ID + "=" + d.user_id, null);
    }

    public boolean deleteUser() {
        database.delete(LOGIN_TABLE, null, null);
        return true;
    }

    public void open() {

        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        if (database != null) {
            database.close();
        }
    }
}
