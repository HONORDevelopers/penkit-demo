/*
 * Copyright (c) Honor Technologies Co., Ltd. 2020-2024. All rights reserved.
 */

package com.hihonor.suitenotedemo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * DB operation class
 *
 * @author hihonor pencil engine
 * @since 2024-07-01
 */
public class UtilOperation {
    /**
     * The table name
     */
    public static final String TABLE_NAME = "NotesInfo";
    private static final int TABLE_KEY_UUID = 0;
    private static final int TABLE_KEY_MODIFY_TIME = 1;
    private static final int TABLE_KEY_TXT_FILE_NAME = 2;
    private static final int TABLE_KEY_PNG_FILE_NAME = 3;
    private static final String UPDATE_TABLE = "update NotesInfo set modifyTime=?, mTxtFileName=?, " +
            "mPngFileName=? where uuid=? ";
    private static final String INQUIRE_TABLE = "select uuid, modifyTime, mTxtFileName, mPngFileName from NotesInfo";
    private DatabaseUtil du;
    private SQLiteDatabase db;

    public UtilOperation(Context context) {
        du = new DatabaseUtil(context);
        db = du.getWritableDatabase();
    }

    /**
     * Insert data into the table
     *
     * @param tableName the table name
     * @param key keys statement
     * @param values values of keys
     */
    public void addData(String tableName, String[] key, String[] values) {
        ContentValues contentValues = new ContentValues();
        for (int i = 0; i < key.length; i++) {
            contentValues.put(key[i], values[i]);
        }
        db.insert(tableName, null, contentValues);
        contentValues.clear();
    }

    /**
     * Delete data from a table
     *
     * @param where keys of the table
     * @param values values of the keys
     * @return the number of rows affected if a whereClause is passed in, 0
     * otherwise. To remove all rows and get a count pass "1" as the
     * whereClause.
     */
    public int delData(String where, String[] values) {
        int delData;
        delData = db.delete(TABLE_NAME, where, values);
        return delData;
    }

    /**
     * Update the table
     *
     * @param values values of the keys
     */
    public void update(String[] values) {
        db.execSQL(UPDATE_TABLE, values);
    }

    /**
     * Query the table
     *
     * @return List array of the results
     */
    public List<NotesFileName> inquireData() {
        List<NotesFileName> list = new ArrayList<>();
            Cursor cursor = db.rawQuery(INQUIRE_TABLE, null);
        while (cursor.moveToNext()) {
            String uuid = cursor.getString(TABLE_KEY_UUID);
            String modifyTime = cursor.getString(TABLE_KEY_MODIFY_TIME);
            String txtFileName = cursor.getString(TABLE_KEY_TXT_FILE_NAME);
            String pngFileName = cursor.getString(TABLE_KEY_PNG_FILE_NAME);

            NotesFileName notesFileName = new NotesFileName();
            notesFileName.setUuid(uuid);
            notesFileName.setModifyTime(modifyTime);
            notesFileName.setTxtFileName(txtFileName);
            notesFileName.setPngFileName(pngFileName);
            list.add(notesFileName);
        }
        cursor.close();
        return list;
    }

    /**
     * Gets the uuid of the last open file
     *
     * @return uuid of the open file
     */
    public String getLastUuid() {
        String sql = "select last_insert_rowid() from " + TABLE_NAME ;
        Cursor cursor = db.rawQuery(sql, null);
        String uuid = "";
        if (cursor.moveToFirst()) {
            uuid = cursor.getString(TABLE_KEY_UUID);
        }
        cursor.close();
        return uuid;
    }

    /**
     * Query the file name by the uuid
     *
     * @param uuid the uuid
     * @return NotesFileName
     */
    public NotesFileName getTxtPathAccordingToId(String uuid) {
        List<NotesFileName> list = new ArrayList<>();
        NotesFileName notesFileName = new NotesFileName();
        Cursor cursor = db.rawQuery("select uuid, modifyTime, mTxtFileName, mPngFileName" +
                " from NotesInfo" + " where uuid = " + uuid, null);
        String txtFileName = "";
        String pngFileName = "";
        while (cursor.moveToNext()) {
            txtFileName = cursor.getString(TABLE_KEY_TXT_FILE_NAME);
            pngFileName = cursor.getString(TABLE_KEY_PNG_FILE_NAME);
            notesFileName.setTxtFileName(txtFileName);
            notesFileName.setPngFileName(pngFileName);
        }
        cursor.close();
        return notesFileName;
    }

    /**
     * Close the database link
     */
    public void getClose() {
        if (db != null) {
            db.close();
        }
    }
}
