/*
 * Copyright (C) 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.android.demo.UDBuildings;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import java.util.Arrays;
import android.os.Bundle;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Simple notes database access helper class. Defines the basic CRUD operations
 * for the notepad example, and gives the ability to list all notes as well as
 * retrieve or modify a specific note.
 * 
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 */
public class DataDbAdapter {

    public static final String KEY_CODE = "code";
    public static final String KEY_NAME = "name";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_ROWID = "_id";

    private static final String TAG = "DataDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
        "create table notes (_id integer primary key autoincrement, "
        + "code text unique, name text unique, latitude text unique, longitude text unique);";

    private static final String DATABASE_NAME = "UDpositions5";
    private static final String DATABASE_TABLE = "notes";
    private static final int DATABASE_VERSION = 3;

    private final Context mCtx;

    private class UDDataItem
    {
        public String mCode;
        public String mName;
        public String mLatitude;
        public String mLongitude;

        public UDDataItem(String code, String name, String latitude, String longitude)
        {
            mCode      = code;
            mName      = name;
            mLatitude  = latitude;
            mLongitude = longitude;
        }

        public UDDataItem(UDDataItem item)
        {
            mCode      = item.mCode;
            mName      = item.mName;
            mLatitude  = item.mLatitude;
            mLongitude = item.mLongitude;
        }
    }

    private ArrayList<UDDataItem> getUDDataFromFile()
    {
        final String UDDATA_PATH = "/sdcard/UDBuildingPositions";
        String sCurrentLine;
        BufferedReader br = null;
        String delims = ":";
        String[] tokens;
        final int FIELDARRAYLEN = 256;

        ArrayList<UDDataItem> uddata_array = new ArrayList<UDDataItem>();

        try {
            br = new BufferedReader(new FileReader(UDDATA_PATH));
            while ((sCurrentLine = br.readLine()) != null) {
                tokens = sCurrentLine.split(":");
                UDDataItem new_item = new UDDataItem(tokens[0], tokens[1], tokens[2], tokens[3]);
                uddata_array.add(new_item);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return uddata_array;
    }

    private class UDDataListAdapter extends ArrayAdapter<UDDataItem>
    {
        private ArrayList<UDDataItem> mUDData_array;

        public UDDataListAdapter(Context context, int layoutResourceId, ArrayList<UDDataItem> item_array)
        {
            super(context, layoutResourceId, item_array);
            mUDData_array = item_array;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View cv = convertView;
            if (cv == null)
            {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                cv = inflater.inflate(R.layout.item_layout, null);
            }
            UDDataItem item = mUDData_array.get(position);
            if (item != null)
            {
                TextView tvCode = (TextView)cv.findViewById(R.id.item_code);
                TextView tvName = (TextView)cv.findViewById(R.id.item_name);
                TextView tvLatitude = (TextView)cv.findViewById(R.id.item_latitude);
                TextView tvLongitude = (TextView)cv.findViewById(R.id.item_longitude);
                tvCode.setText("Code: " + item.mCode);
                tvName.setText("Name: " + item.mName);
                tvLatitude.setText("Latitude: " + item.mLatitude);
                tvLongitude.setText("Longitude: " + item.mLongitude);
            }
            return cv;
        }
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public DataDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public DataDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();

        Cursor cursor = fetchAllNotes();

        ArrayList<UDDataItem> tmp_array = getUDDataFromFile();
        ContentValues initialValues = new ContentValues();

        if(cursor.getCount() == 0) {

            for(int i = 0; i < tmp_array.size(); i++) {
                initialValues.put(KEY_CODE, tmp_array.get(i).mCode);
                initialValues.put(KEY_NAME, tmp_array.get(i).mName);
                initialValues.put(KEY_LATITUDE, tmp_array.get(i).mLatitude);
                initialValues.put(KEY_LONGITUDE, tmp_array.get(i).mLongitude);

                mDb.insert(DATABASE_TABLE, null, initialValues);
            }
        }

        return this;
    }

    public void close() {
        mDbHelper.close();
    }


    /**
     * Create a new note using the code and name provided. If the note is
     * successfully created return the new rowId for that note, otherwise return
     * a -1 to indicate failure.
     * 
     * @param code the code of the note
     * @param name the name of the note
     * @param latitude the latitude
     * @param longitude the longitude
     * @return rowId or -1 if failed
     */
    public long createNote(String code, String name, String latitude, String longitude) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_CODE, code);
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_LATITUDE, latitude);
        initialValues.put(KEY_LONGITUDE, longitude);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the note with the given rowId
     * 
     * @param rowId id of note to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteNote(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all notes in the database
     * 
     * @return Cursor over all notes
     */
    public Cursor fetchAllNotes() {

        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_CODE,
                KEY_NAME, KEY_LATITUDE, KEY_LONGITUDE}, null, null, null, null, null, null);
    }

    /**
     * Return a Cursor positioned at the note that matches the given rowId
     * 
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchNote(long rowId) throws SQLException {

        Cursor mCursor =

            mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                            KEY_CODE, KEY_NAME, KEY_LATITUDE, KEY_LONGITUDE}, KEY_ROWID + "=" + rowId, null,
                    null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    /**
     * Update the note using the details provided. The note to be updated is
     * specified using the rowId, and it is altered to use the code and name
     * values passed in
     * 
     * @param rowId id of note to update
     * @param code value to set note code to
     * @param name value to set note name to
     * @param latitude value of latitude
     * @param longitude value of longitude
     * @return true if the note was successfully updated, false otherwise
     */
    public boolean updateNote(long rowId, String code, String name, String latitude, String longitude) {
        ContentValues args = new ContentValues();
        args.put(KEY_CODE, code);
        args.put(KEY_NAME, name);
        args.put(KEY_LATITUDE, latitude);
        args.put(KEY_LONGITUDE, longitude);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}
