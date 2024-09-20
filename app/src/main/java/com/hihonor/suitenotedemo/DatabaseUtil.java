/*
 * Copyright (c) Honor Technologies Co., Ltd. 2020-2024. All rights reserved.
 */

package com.hihonor.suitenotedemo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Database tools class
 *
 * @author hihonor pencil engine
 * @since 2024-07-01
 */
public class DatabaseUtil extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "SuiteNoteDemo.db";
    private static final int DATABASE_VERSION = 1;

    private final String createTableStatement = "create table NotesInfo ("
            + "uuid integer primary key autoincrement,"
            + "modifyTime text,"
            + "mTxtFileName text,"
            + "mPngFileName text)";

    /**
     * Constructor
     *
     * @param context context
     */
    public DatabaseUtil(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Create tables
     *
     * @param sqLiteDatabase database
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        createTable(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    private void createTable(SQLiteDatabase db) {
        db.execSQL(createTableStatement);
    }

}
