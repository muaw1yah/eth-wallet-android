package com.namadi.crimson.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import com.namadi.crimson.models.CrimsonWallet;

/**
 * Created by crimson on 29/05/2018.
 */

public class SampleSQLiteDBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "crimson_wallet";
    public static final String WALLET_TABLE_NAME = "wallet";
    public static final String PERSON_COLUMN_ID = "_id";
    public static final String PERSON_COLUMN_NAME = "name";
    public static final String PERSON_COLUMN_ADDRESS = "address";
    public static final String PERSON_COLUMN_KEY = "privateKey";
    public static final String PERSON_COLUMN_BALANCE = "balance";

    public SampleSQLiteDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + WALLET_TABLE_NAME + " (" +
                PERSON_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                PERSON_COLUMN_NAME + " TEXT, " +
                PERSON_COLUMN_ADDRESS + " TEXT, " +
                PERSON_COLUMN_BALANCE + " TEXT, " +
                PERSON_COLUMN_KEY + " TEXT" + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + WALLET_TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public ArrayList<CrimsonWallet> getAllWallets() {
        ArrayList<CrimsonWallet> array_list = new ArrayList<>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from " + WALLET_TABLE_NAME, null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            String name = res.getString(res.getColumnIndex(PERSON_COLUMN_NAME));
            String address = res.getString(res.getColumnIndex(PERSON_COLUMN_ADDRESS));
            String key = res.getString(res.getColumnIndex(PERSON_COLUMN_KEY));
            Double balance = Double.parseDouble(res.getString(res.getColumnIndex(PERSON_COLUMN_BALANCE)));
            int id = Integer.parseInt(res.getString(res.getColumnIndex(PERSON_COLUMN_ID)));
            CrimsonWallet wallet = new CrimsonWallet(id, name, address, key, balance);
            array_list.add(wallet);
            res.moveToNext();
        }
        return array_list;
    }

    public CrimsonWallet getWallet(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from " + WALLET_TABLE_NAME + " WHERE _id=" + id, null );
        res.moveToFirst();

        String name = res.getString(res.getColumnIndex(PERSON_COLUMN_NAME));
        String address = res.getString(res.getColumnIndex(PERSON_COLUMN_ADDRESS));
        String key = res.getString(res.getColumnIndex(PERSON_COLUMN_KEY));
        Double balance = Double.parseDouble(res.getString(res.getColumnIndex(PERSON_COLUMN_BALANCE)));

        return new CrimsonWallet(name, address, key, balance);
    }

    public CrimsonWallet saveToDB(CrimsonWallet wallet) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SampleSQLiteDBHelper.PERSON_COLUMN_NAME, wallet.getName());
        values.put(SampleSQLiteDBHelper.PERSON_COLUMN_ADDRESS, wallet.getAddress());
        values.put(SampleSQLiteDBHelper.PERSON_COLUMN_KEY, wallet.getPrivateKey());
        values.put(SampleSQLiteDBHelper.PERSON_COLUMN_BALANCE, wallet.getBalance());
        long newRowId = database.insert(SampleSQLiteDBHelper.WALLET_TABLE_NAME, null, values);

        Log.i("Wallet", "Saved Wallet Id: " + newRowId);

        wallet.setId((int)newRowId);
        Log.i("Wallet", "Saved Wallet Id: " + wallet.getId());
        return wallet;
    }

    public long updateWalletDB(CrimsonWallet wallet) {
        if(wallet.getId() == -1) {
            return -1;
        }
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SampleSQLiteDBHelper.PERSON_COLUMN_NAME, wallet.getName());
        values.put(SampleSQLiteDBHelper.PERSON_COLUMN_BALANCE, wallet.getBalance());
        return (long) database.update(SampleSQLiteDBHelper.WALLET_TABLE_NAME, values, "_id=" + wallet.getId(), null);
    }
}