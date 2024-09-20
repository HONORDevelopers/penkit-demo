/*
 * Copyright (c) Honor Technologies Co., Ltd. 2020-2024. All rights reserved.
 */

package com.hihonor.suitenotedemo;

import android.content.Context;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * File tools class
 *
 * @author hihonor pencil engine
 * @since 2024-07-01
 */
public class FileUtils implements Constants {
    private static final String TAG = FileUtils.class.getSimpleName();

    private static final int MIN_AVAILABLE_STORAGE = 10 * 1024 * 1024;

    private FileUtils() {
    }

    /**
     * Save data in the cache
     *
     * @param context context
     * @param bytes the bytes array
     * @param name the file name
     * @return the file path
     */
    public static String copyBytesToCache(Context context, byte[] bytes, String name) {
        if (context == null) {
            return "";
        }
        if (isStorageFull(context)) {
            return "";
        }
        if (bytes == null) {
            return "";
        }
        FileOutputStream out = null;
        File output = null;
        try {
            String defaultName;
            if (TextUtils.isEmpty(name)) {
                defaultName = new Date().toString();
            } else {
                defaultName = name;
            }
            output = new File(getImageFileDir(context), defaultName);
            out = new FileOutputStream(output);
            out.write(bytes, 0, bytes.length);
            output.setReadable(true, true);
            output.setWritable(true, true);
            return output.getCanonicalPath();
        } catch (FileNotFoundException fnfe) {
            Log.e(TAG, FILE_NOT_FOUND);
        } catch (IOException ioe) {
            Log.e(TAG, INVALID_FILE);
        } finally {
            IoUtils.closeQuietly(out);
        }
        return "";
    }

    private static File getImageFileDir(Context context) {
        return context.getExternalFilesDir(null);
    }

    /**
     * Check whether the storage is full
     *
     * @param context context
     * @return true - full
     */
    private static boolean isStorageFull(Context context) {
        if (context == null) {
            Log.e(TAG, "context is null");
            return false;
        }
        File path = context.getExternalFilesDir(null);
        StatFs stat = null;
        try {
            stat = new StatFs(path.getPath());
        } catch (IllegalArgumentException e) {
            Log.e(TAG, INVALID_FILE);
            return false;
        }
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        long availSize = availableBlocks * blockSize;
        return availSize < MIN_AVAILABLE_STORAGE;
    }

    /**
     * Delete the file
     *
     * @param filePathName file path name
     * @return true - delete success
     */
    public static boolean deleteSingleFile(String filePathName) {
        if (filePathName == null) {
            Log.i(TAG, "filePathName is null ");
            return false;
        }
        File file = new File(filePathName);
        if (file.exists() && file.isFile()) {
            return file.delete();
        } else {
            return false;
        }
    }
}
