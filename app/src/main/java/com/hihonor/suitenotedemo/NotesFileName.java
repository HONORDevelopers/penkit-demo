/*
 * Copyright (c) Honor Technologies Co., Ltd. 2020-2024. All rights reserved.
 */

package com.hihonor.suitenotedemo;

/**
 * File operation class
 *
 * @author hihonor pencil engine
 * @since 2024-07-01
 */
public class NotesFileName {
    private String name;

    private String uuid;

    private String modifyTime;

    private String mTxtFileName;

    private String mPngFileName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(String modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * Get the file name
     *
     * @return mTxtFileName file name
     */
    public String getTxtFileName() {
        return mTxtFileName;
    }

    /**
     * Set the file name
     *
     * @param mTxtFileName file name
     */
    public void setTxtFileName(String mTxtFileName) {
        this.mTxtFileName = mTxtFileName;
    }

    /**
     * Get the thumbnail file name
     *
     * @return mPngFileName the file name of a thumbnail
     */
    public String getPngFileName() {
        return mPngFileName;
    }

    /**
     * Set the file path of the thumbnail
     *
     * @param mPngFileName the file path of the thumbnail
     */
    public void setPngFileName(String mPngFileName) {
        this.mPngFileName = mPngFileName;
    }
}
