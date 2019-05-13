package com.example.gateway;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class GestioneDB {

    static final String KEY_SERIAL = "serial";
    static final String KEY_GATEWAY = "gateway";
    static final String KEY_DATE = "date";
    static final String KEY_TYPE = "type";
    static final String KEY_VALUE = "value";

    static final String TAG = "GestioneDB";
    static final String DATABASE_NOME = "TestDB";
    static final String DATABASE_TABELLA = "eventi";
    static final int DATABASE_VERSIONE = 2;

    static final String DATABASE_CREAZIONE = "CREATE TABLE " + DATABASE_TABELLA + " (date text, serial text not null, gateway text not null, type integer, value text);";

    final Context context;
    DatabaseHelper DBHelper;
    SQLiteDatabase db;

    public GestioneDB(Context ctx){
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context){
            super(context,DATABASE_NOME, null, DATABASE_VERSIONE);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(DATABASE_CREAZIONE);
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(DatabaseHelper.class.getName(),"Aggiornamento database dalla versione " + oldVersion + " alla " + newVersion + ". I dati esistenti verranno eliminati.");
            db.execSQL("DROP TABLE IF EXISTS eventi");
            onCreate(db);
        }
    }

    public GestioneDB open() throws SQLException{
        db = DBHelper.getWritableDatabase();
        return this;
    }

    public void close(){
        DBHelper.close();
    }

    public long putEvent(String serial, String gateway, int type, String value){

        Calendar now = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("E yyyy.MM.dd 'at' hh:mm:ss a zzz");
        String date = formatter.format(now.getTime());
        Log.e(TAG,"PUT : " + date);

        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_DATE, date);
        initialValues.put(KEY_SERIAL, serial);
        initialValues.put(KEY_GATEWAY, gateway);
        initialValues.put(KEY_TYPE, type);
        initialValues.put(KEY_VALUE, value);
        Log.e(TAG,String.valueOf(db.insert(DATABASE_TABELLA, null, initialValues)));
        return db.insert(DATABASE_TABELLA, null, initialValues);
    }

    public boolean deleteEvent(){
        return false;
    }

    public Cursor ottieniEventi(){
        return db.query(DATABASE_TABELLA, new String[] {KEY_DATE, KEY_SERIAL, KEY_SERIAL, KEY_TYPE, KEY_VALUE}, null, null, null, null, null);
    }

    public Cursor ottieniEvento(long rigaId) throws SQLException
    {
        Cursor mCursore = null;  //riga da eliminare
        /*Cursor mCursore = db.query(true, DATABASE_TABELLA, new String[] {KEY_DATE, KEY_SERIAL, KEY_SERIAL, KEY_TYPE, KEY_VALUE}, KEY_RIGAID + "=" + rigaId, null, null, null, null, null);
        if (mCursore != null) {
            mCursore.moveToFirst();
        }*/
        return mCursore;
    }
}