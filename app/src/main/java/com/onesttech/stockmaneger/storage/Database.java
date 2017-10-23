package com.onesttech.stockmaneger.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.onesttech.stockmaneger.pojo.OfflineDataPojo;

import java.util.ArrayList;
import java.util.List;


public class Database extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ca.db";
    private static final int VERSION = 5;

    public Database(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }


    public long saveCanData(String logId, String customerId, String canId, String canName, String canCollect, String canDeliver, String canDamage, String canLost, String orderStatus, String amount, String paymentMode, String check, String time, String oneTimeReturn) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(Constants.LOGID, logId);
        cv.put(Constants.customerID, customerId);
        cv.put(Constants.canId, canId);
        cv.put(Constants.canName, canName);
        cv.put(Constants.canCollect, canCollect);
        cv.put(Constants.canDeliver, canDeliver);
        cv.put(Constants.canDamage, canDamage);
        cv.put(Constants.canLost, canLost);
        cv.put(Constants.orderStatus, orderStatus);
        cv.put(Constants.amount, amount);
        cv.put(Constants.paymentMode, paymentMode);
        cv.put(Constants.checkNo, check);
        cv.put("oneTimeReturn", oneTimeReturn);
        cv.put("time", time);
        return db.insert("sm", null, cv);
    }

    public List<OfflineDataPojo> getOfflineData(String logID) {
        SQLiteDatabase db = this.getWritableDatabase();
        List<OfflineDataPojo> list = new ArrayList<>();
        String[] col = {"customerID", "canId", "canName", "canCollect", "canDeliver", "canDamage",
                "canLost", "orderStatus", "amount", "paymentMode", "checkNo", "time", "oneTimeReturn"};
        String where = "logid = ?";
        String whereArgs[] = {logID};
        Cursor cursor = db.query("sm", col, where, whereArgs, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                OfflineDataPojo pojo = new OfflineDataPojo();
                pojo.setCustomerId(cursor.getString(cursor.getColumnIndex("customerID")));
                pojo.setCanId(cursor.getString(cursor.getColumnIndex("canId")));
                pojo.setCanName(cursor.getString(cursor.getColumnIndex("canName")));
                pojo.setCanCollect(cursor.getString(cursor.getColumnIndex("canCollect")));
                pojo.setCanDeliver(cursor.getString(cursor.getColumnIndex("canDeliver")));
                pojo.setCanDamage(cursor.getString(cursor.getColumnIndex("canDamage")));
                pojo.setCanLost(cursor.getString(cursor.getColumnIndex("canLost")));
                pojo.setOrderStatus(cursor.getString(cursor.getColumnIndex("orderStatus")));
                pojo.setAmount(cursor.getString(cursor.getColumnIndex("amount")));
                pojo.setPaymentMode(cursor.getString(cursor.getColumnIndex("paymentMode")));
                pojo.setCheck(cursor.getString(cursor.getColumnIndex("checkNo")));
                pojo.setTime(cursor.getString(cursor.getColumnIndex("time")));
                pojo.setOneTimeReturn(cursor.getString(cursor.getColumnIndex("oneTimeReturn")));
                list.add(pojo);
            }
            cursor.close();
        }
        return list;
    }
    public boolean deleteData(String customerId){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("sm","customerID = "+customerId,null) > 0;
    }

    public void deleteTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("sm",null,null);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String table = "CREATE TABLE sm (_id INTEGER PRIMARY KEY AUTOINCREMENT,logid TEXT, customerID TEXT, canId TEXT, canName TEXT, " +
                "canCollect TEXT, canDeliver TEXT,canDamage TEXT, canLost TEXT, orderStatus TEXT, amount TEXT, paymentMode TEXT, checkNo Text, time TEXT, oneTimeReturn TEXT );";
        db.execSQL(table);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
