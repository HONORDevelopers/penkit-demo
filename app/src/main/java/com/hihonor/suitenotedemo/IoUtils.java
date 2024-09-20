/*
 * Copyright (c) Honor Technologies Co., Ltd. 2020-2024. All rights reserved.
 */

package com.hihonor.suitenotedemo;

import android.util.Log;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

/**
 * IO tools
 *
 * @author hihonor pencil engine
 * @since 2024-07-01
 */
public class IoUtils implements Constants {
    private static final String TAG = IoUtils.class.getSimpleName();

    private IoUtils() {
    }

    /**
     * Close outputStream
     *
     * @param outputStream outputStream
     */
    public static void closeOutputStream(OutputStream outputStream) {
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                Log.e(TAG, READ_ERROR);
            }
        }
    }

    /**
     * Close tool class
     *
     * @param closeable tool class
     */
    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                Log.e(TAG, READ_ERROR);
            }
        }
    }
}
