package com.example.vpoorn001c.ble;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;


public class NotesDatabaseAdapter {


ArrayList<Data> dataList = new ArrayList<>();
    HelperClass helper;

    public NotesDatabaseAdapter(Context context){

        helper= new HelperClass(context);
    }
    public void insertData(Data data)
    {
        //int duration = Toast.LENGTH_SHORT;
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(HelperClass.COUPON_ID, data.getId());
        contentValues.put(HelperClass.DEVICE_ADDRESS ,data.getDeviceAddress());
        contentValues.put(HelperClass.COUPON_NAME ,data.getCouponName());

        contentValues.put(HelperClass.COUPON_DESC ,data.getCouponDesc());
        contentValues.put(HelperClass.DISCOUNT_PERC  ,data.getDiscountPerc());
        contentValues.put(HelperClass.COUPON_EXPIRY,data.getCouponExpiry());



        db.insert(HelperClass.TABLE_NAME,null,contentValues);
        Log.d("Tag", "Tag");
        //return id;

    }


    public   ArrayList<Data> getData() {
        SQLiteDatabase db = helper.getWritableDatabase();
        String[] columns = {helper.COUPON_ID ,helper.DEVICE_ADDRESS,helper.COUPON_NAME,helper.COUPON_DESC,helper.DISCOUNT_PERC ,helper.COUPON_EXPIRY};
        Cursor cursor = db.query(helper.TABLE_NAME, columns, null, null, null, null, null);
        StringBuffer buffer = new StringBuffer();

        while (cursor.moveToNext()) {

            Data data = new Data(cursor.getInt(0),cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getString(4),cursor.getString(5));
           dataList.add(data);


        }
        return  dataList;
    }

    public int deleteData(){
        SQLiteDatabase db = helper.getWritableDatabase();
        //String[] whereArgs ={"Macy's: 25% Off Sitewide"};
       int delete = db.delete(HelperClass.TABLE_NAME,null,null);
        return  delete;
    }


    public Cursor fetchRecordsByQuery(String query) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.query(true, helper.TABLE_NAME, new String[]{helper.COUPON_ID, helper.COUPON_NAME }, helper.COUPON_NAME+"LIKE" + "'%" + query + "%'", null, null, null, null, null);

    }



    static class HelperClass extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Device_Database";
    private static final String TABLE_NAME = "Device_Table";
    private static final int DATABASE_VERSION =8;
    private static final String COUPON_ID = "_id";
    private static final String COUPON_NAME ="coupon_name";
    private static final String  DEVICE_ADDRESS = "device_address";
    private static final String  COUPON_DESC = "coupon_desc";
    private static final String  COUPON_EXPIRY= "coupon_expiry";
    private static final String DISCOUNT_PERC = "discount_perc";
    private Context context;
    private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + COUPON_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            " " + DEVICE_ADDRESS + "  VARCHAR(255),"+COUPON_NAME+"  VARCHAR(255) ,"+COUPON_DESC +" VARCHAR(255) ,"+DISCOUNT_PERC+" VARCHAR(255),"+COUPON_EXPIRY+" VARCHAR(255));";
    private static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        public HelperClass(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            this.context = context;
         //   Message.message(context, "");
        }

        @Override
        public void onCreate(SQLiteDatabase db) {


            try {
                db.execSQL(CREATE_TABLE);
              //  Message.message(context, "o");
            } catch (SQLException e)


            {

                Message.message(context, "" + e);
            }

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            try {
                db.execSQL(DROP_TABLE);
                onCreate(db);
               // Message.message(context, "onUpgrade Called");

            } catch (SQLException e) {
               // Message.message(context, "" + e);
            }
        }
    }
}