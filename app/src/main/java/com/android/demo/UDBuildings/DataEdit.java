/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.demo.UDBuildings;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class DataEdit extends Activity {

    private EditText mCodeText;
    private EditText mNameText;
    private EditText mLatitudeText;
    private EditText mLongitudeText;
    private Long mRowId;
    private DataDbAdapter mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new DataDbAdapter(this);
        mDbHelper.open();

        setContentView(R.layout.note_edit);
        setTitle(R.string.edit_note);

        mCodeText     = (EditText) findViewById(R.id.code);
        mNameText     = (EditText) findViewById(R.id.name);
        mLatitudeText = (EditText) findViewById(R.id.latitude);
        mLongitudeText = (EditText) findViewById(R.id.longitude);

        Button confirmButton = (Button) findViewById(R.id.confirm);

        mRowId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(DataDbAdapter.KEY_ROWID);
		if (mRowId == null) {
			Bundle extras = getIntent().getExtras();
			mRowId = extras != null ? extras.getLong(DataDbAdapter.KEY_ROWID)
									: null;
		}

		populateFields();

        confirmButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                setResult(RESULT_OK);
                finish();
            }

        });
    }

    private void populateFields() {
        if (mRowId != null) {
            Cursor note = mDbHelper.fetchNote(mRowId);
            startManagingCursor(note);
            mCodeText.setText(note.getString(
                    note.getColumnIndexOrThrow(DataDbAdapter.KEY_CODE)));
            mNameText.setText(note.getString(
                    note.getColumnIndexOrThrow(DataDbAdapter.KEY_NAME)));
            mLatitudeText.setText(note.getString(
                    note.getColumnIndexOrThrow(DataDbAdapter.KEY_LATITUDE)));
            mLongitudeText.setText(note.getString(
                    note.getColumnIndexOrThrow(DataDbAdapter.KEY_LONGITUDE)));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        outState.putSerializable(DataDbAdapter.KEY_ROWID, mRowId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateFields();
    }

    private void saveState() {
        String code      = mCodeText.getText().toString();
        String name      = mNameText.getText().toString();
        String latitude  = mLatitudeText.getText().toString();
        String longitude = mLongitudeText.getText().toString();

        if (mRowId == null) {
            long id = mDbHelper.createNote(code, name, latitude, longitude);
            if (id > 0) {
                mRowId = id;
            }
        } else {
            mDbHelper.updateNote(mRowId, code, name, latitude, longitude);
        }
    }

}
