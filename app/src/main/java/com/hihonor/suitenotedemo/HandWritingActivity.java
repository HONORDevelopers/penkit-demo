/*
 * Copyright (c) Honor Technologies Co., Ltd. 2020-2024. All rights reserved.
 */

package com.hihonor.suitenotedemo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.BadParcelableException;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;

import com.hihonor.android.magicx.app.penengine.view.HnHandWritingView;
import com.hihonor.android.magicx.app.penengine.view.IPaintViewListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Handwriting Activity
 *
 * @author hihonor pencil engine
 * @since 2024-07-01
 */
public class HandWritingActivity extends Activity implements Constants {
    /**
     * table keys
     */
    private static final String[] TABLE_KEYS = {"uuid", "modifyTime", "mTxtFileName", "mPngFileName"};

    private static final String TAG = HandWritingActivity.class.getSimpleName();

    private static final int FAST_CLICK_DELAY_TIME = 1000;

    private static final int BITMAP_COMPRESS_QUALITY = 100;

    /**
     * custom maximum height of a thumbnail obtained
     */
    private static final int MAX_BITMAP_HEIGHT = 4000;

    private static final String TXT_FILE_FORMAT = ".txt";

    private static final String PNG_FILE_FORMAT = ".png";

    private static final String ON_PAUSE_FUNCTION = "onPause";

    private static final String TEXT_FILE_NAME_FROM_MAIN = "mTextFileNameFromMain";

    private static final String IS_FROM_EXIST_HANDWRIRING = "mIsFromExistHandwriting";

    private long lastClickTime = 0L;

    private String msg = "";

    private String uuid = "";

    private HnHandWritingView mHandWritingView;

    private UtilOperation mUtilOperation;

    private File mDir;

    private String mTextFileNameFromMain = "";

    private String mPngFileNameFromMain = "";

    private View mBack;

    private View mSave;

    private View mClear;

    private View mUndo;

    private View mRedo;

    private boolean mIsFromExistHandwriting = false;

    private boolean mIsBack = false;

    private File mSaveInfoFileToDir;

    private File mSavePngFileToDir;

    private ProgressBar mProgressBar;

    private IsLoadDataListener mLoadListener;

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState == null) {
            Log.e(TAG, "outState is error");
            return;
        }
        if (TextUtils.isEmpty(mTextFileNameFromMain)) {
            String modifyTime = String.valueOf(System.currentTimeMillis());
            mTextFileNameFromMain = modifyTime + TXT_FILE_FORMAT;
        }
        outState.putString(TEXT_FILE_NAME_FROM_MAIN, mTextFileNameFromMain);
        outState.putBoolean(IS_FROM_EXIST_HANDWRIRING, mIsFromExistHandwriting);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.handwriting);
        mHandWritingView = findViewById(R.id.hn_handwriting_view);
        mBack = findViewById(R.id.back_button);
        mSave = findViewById(R.id.save_button);
        mClear = findViewById(R.id.clear);
        mUndo = findViewById(R.id.undo);
        mRedo = findViewById(R.id.redo);
        mProgressBar = findViewById(R.id.pb_loading);
        mDir = getExternalFilesDir(null);
        if (savedInstanceState != null && !TextUtils.isEmpty(savedInstanceState.getString(TEXT_FILE_NAME_FROM_MAIN))) {
            mTextFileNameFromMain = savedInstanceState.getString(TEXT_FILE_NAME_FROM_MAIN);
            mIsFromExistHandwriting = savedInstanceState.getBoolean(IS_FROM_EXIST_HANDWRIRING);
        }
        setFileOperation();
        setNoteOperation();
        setViewOperation();
        updateRedoAndUndoView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = this.getIntent();
        if (TextUtils.isEmpty(msg) && intent != null) {
            try {
                msg = intent.getStringExtra("msg");
            } catch (BadParcelableException e) {
                Log.e(TAG, "getStringExtra error");
            }
        }
        mUtilOperation = new UtilOperation(this);
        NotesFileName fileName = new NotesFileName();
        if (!TextUtils.isEmpty(msg) && TextUtils.isEmpty(mTextFileNameFromMain)) {
            switch (msg) {
                case "add_new_handwriting":
                    mTextFileNameFromMain = "";
                    mPngFileNameFromMain = "";
                    break;
                default:
                    uuid = msg;
                    fileName = mUtilOperation.getTxtPathAccordingToId(uuid);
                    mTextFileNameFromMain = fileName.getTxtFileName();
                    mPngFileNameFromMain = fileName.getPngFileName();
                    mIsFromExistHandwriting = true;
                    break;
            }
        }
        mSaveInfoFileToDir = new File(mDir, String.valueOf(mTextFileNameFromMain));
    }

    private void setFileOperation() {
        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (System.currentTimeMillis() - lastClickTime < FAST_CLICK_DELAY_TIME || mHandWritingView.isEmpty()) {
                    return;
                }
                doSaveTextAndPng();
                lastClickTime = System.currentTimeMillis();
                updateRedoAndUndoView();
            }
        });
    }

    private void setNoteOperation() {
        mClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHandWritingView.clear();
            }
        });
        mUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mHandWritingView.canUndo()) {
                    mHandWritingView.undo();
                }
            }
        });
        mRedo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mHandWritingView.canRedo()) {
                    mHandWritingView.redo();
                }
            }
        });
    }

    private void setViewOperation() {
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIsBack = true;
                finish();
            }
        });
        // Initialize the Handwriting Suite callback function
        mHandWritingView.setPaintViewListener(new IPaintViewListener() {
            @Override
            public void onStepChanged(int step) {
                // add what you want to do after every step changed
                updateRedoAndUndoView();
            }

            @Override
            public void onEngineInit() {
                // add which note you want to load after the engine is init
                if (mSaveInfoFileToDir.exists()) {
                    try {
                        mHandWritingView.load(mSaveInfoFileToDir.getCanonicalPath());
                    } catch (IOException e) {
                        Log.e(TAG, INVALID_FILE);
                    }
                } else {
                    mHandWritingView.load();
                }
            }

            @Override
            public void onLoaded() {
                // add what you want to do after the note appears on the screen
            }
        });
    }

    private void updateRedoAndUndoView() {
        mUndo.setEnabled(mHandWritingView.canUndo());
        mRedo.setEnabled(mHandWritingView.canRedo());
        mSave.setEnabled(isCanBeSaved());
    }

    private boolean isCanBeSaved() {
        return mHandWritingView.isChanged() && !mHandWritingView.isEmpty();
    }

    /**
     * Callback for saving thumbnails
     *
     * @param dataComplete callback interface
     */
    public void setLoadDataComplete(IsLoadDataListener dataComplete) {
        this.mLoadListener = dataComplete;
    }

    private void saveGraffiti(String string, String functionName) {
        new AsyncTask<String, String, String>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (functionName.equals(ON_PAUSE_FUNCTION)) {
                    return;
                }
                showProgressBarForSave();
            }

            @Override
            protected String doInBackground(String[] s) {
                savePng(string);
                return "";
            }

            @Override
            protected void onPostExecute(String s) {
                if (mProgressBar != null && mProgressBar.isEnabled()) {
                    mProgressBar.setVisibility(View.GONE);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
                if (mLoadListener != null) {
                    mLoadListener.loadComplete();
                }
            }
        }.execute("");
    }

    private void showProgressBarForSave() {
        mProgressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void savePng(final String string) {
        mSavePngFileToDir = new File(mDir, string);
        Rect rect = mHandWritingView.getContentRange();
        if (rect == null) {
            Log.e(TAG, "rect is error");
            return;
        }
        if (rect.width() <= 0 || rect.height() <= 0) {
            return;
        }

        // obtain the thumbnail
        int extraHeight = getResources().getDimensionPixelSize(R.dimen.dimen_40dp);
        int currentHeight = rect.bottom + extraHeight;
        int bitmapHeight = currentHeight > MAX_BITMAP_HEIGHT ? MAX_BITMAP_HEIGHT : currentHeight;
        Bitmap bm = Bitmap.createBitmap(rect.width(), bitmapHeight, Bitmap.Config.ARGB_8888);
        RectF rectf = new RectF(rect.left, 0, rect.right, bitmapHeight);
        mHandWritingView.getThumbnail(bm, rectf);

        // compress the thumbnail
        if (bm != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            try {
                bm.compress(Bitmap.CompressFormat.PNG, BITMAP_COMPRESS_QUALITY, stream);
                byte[] byteArray = stream.toByteArray();
                bm.recycle();
                FileUtils.copyBytesToCache(HandWritingActivity.this, byteArray, mSavePngFileToDir.getName());
            } finally {
                IoUtils.closeOutputStream(stream);
            }
        }
    }

    private void doSaveTextAndPng() {
        doSaveTextAndPng("");
    }

    private void doSaveTextAndPng(String functionName) {
        String modifyTime = String.valueOf(System.currentTimeMillis());
        if (TextUtils.isEmpty(mTextFileNameFromMain)) {
            String txtFileName = modifyTime + TXT_FILE_FORMAT;
            String pngFileName = modifyTime + PNG_FILE_FORMAT;
            mTextFileNameFromMain = txtFileName;
            mPngFileNameFromMain = pngFileName;
        }
        String[] addNewValues = {null, modifyTime, mTextFileNameFromMain, mPngFileNameFromMain};
        String[] updateOldValues = {modifyTime, mTextFileNameFromMain, mPngFileNameFromMain, uuid};
        mUtilOperation = new UtilOperation(HandWritingActivity.this);
        if (mIsFromExistHandwriting) {
            FileUtils.deleteSingleFile(mDir.getAbsolutePath() + "/" + mTextFileNameFromMain);
            FileUtils.deleteSingleFile(mDir.getAbsolutePath() + "/" + mPngFileNameFromMain);
            mUtilOperation.update(updateOldValues);
        } else {
            mUtilOperation.addData(mUtilOperation.TABLE_NAME, TABLE_KEYS, addNewValues);
            mIsFromExistHandwriting = true;
        }
        try {
            mSaveInfoFileToDir = new File(mDir, mTextFileNameFromMain);
            boolean isSaveSuccess = mHandWritingView.save(mSaveInfoFileToDir.getCanonicalPath());
            if (isSaveSuccess) {
                saveGraffiti(mPngFileNameFromMain, functionName);
            }
        } catch (IOException e) {
            Log.e(TAG, INVALID_FILE);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mIsBack = true;
            finish();
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mIsBack || !isCanBeSaved()) {
            return;
        }
        doSaveTextAndPng(ON_PAUSE_FUNCTION);
    }
}
