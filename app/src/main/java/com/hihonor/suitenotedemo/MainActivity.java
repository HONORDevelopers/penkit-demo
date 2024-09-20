/*
 * Copyright (c) Honor Technologies Co., Ltd. 2020-2024. All rights reserved.
 */

package com.hihonor.suitenotedemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.hihonor.android.magicx.app.penengine.HnPenEngineManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main Activity of this sample, GridView for HnHandWritingView
 *
 * @author hihonor pencil engine
 * @since 2024-07-01
 */
public class MainActivity extends Activity
    implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final double DOUBLE_CLICK_TIME = 1000;

    private static final double DEFAULT_STATE = -1;

    private GridView mGrid;

    private List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();

    private SimpleAdapter simpleAdapter;

    private UtilOperation utilOperation;

    private double firstOnclick = DEFAULT_STATE;

    private double secondOnclick = DEFAULT_STATE;

    private int[] addNewNoteIcon = {R.drawable.add_new_note};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setBackgroundDrawable(null);

        mGrid = findViewById(R.id.grid_notes);
    }

    private void initPic() {
        dataList = getData();
        String[] from = {"image", "text", "modifyTime", "uuid"};
        int[] to = {R.id.image};
        simpleAdapter = new SimpleAdapter(this, dataList, R.layout.adapter_grid_view, from, to);
        mGrid.setAdapter(simpleAdapter);

        // Check whether the suite is available for use
        if (HnPenEngineManager.isEngineRuntimeAvailable(this)) {
            mGrid.setOnItemClickListener(this);
            mGrid.setOnItemLongClickListener(this);
        } else {
            Log.i(TAG, "pencil engine is unavailable.");
            return;
        }
    }

    private List<Map<String, Object>> getData() {
        utilOperation = new UtilOperation(this);
        dataList.clear();
        List<NotesFileName> notesInfo = new ArrayList<>();
        notesInfo = utilOperation.inquireData();
        for (NotesFileName noteFileName : notesInfo) {
            String uuid = noteFileName.getUuid();
            String modifyTime = noteFileName.getModifyTime();
            String mTxtFileName = noteFileName.getTxtFileName();
            String mPngFileName = noteFileName.getPngFileName();
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("uuid", uuid);
            map.put("modifyTime", modifyTime);
            map.put("text", mTxtFileName);
            String mFilePath = getExternalFilesDir(null).getAbsolutePath();
            File pngFile = new File(mFilePath + "/" + mPngFileName);
            map.put("image", pngFile);
            dataList.add(map);
        }
        Map<String, Object> map1 = new HashMap<String, Object>();
        map1.put("image", addNewNoteIcon[0]);
        dataList.add(map1);
        Collections.reverse(dataList);
        return dataList;
    }

    @Override
    public void onResume() {
        super.onResume();
        initPic();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        secondOnclick = System.currentTimeMillis();
        if (secondOnclick - firstOnclick < DOUBLE_CLICK_TIME) {
            Log.i(TAG, "DoubleClick: enter into HandWritingActivity， position is : " + position);
            Map<String, Object> map = (Map<String, Object>) dataList.get(position);
            if (isFirstPosition(position)) {
                return;
            } else {
                if (map.get("uuid") != null) {
                    String uuid = String.valueOf(map.get("uuid"));
                    Intent intent = new Intent(MainActivity.this, HandWritingActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra("msg", uuid);
                    startActivity(intent);
                }
            }
        } else {
            if (isFirstPosition(position)) {
                Intent intent = new Intent(MainActivity.this, HandWritingActivity.class);
                intent.putExtra("msg", "add_new_handwriting");
                startActivity(intent);
                Log.e(TAG, "map.get(text) is null");
            }
        }
        firstOnclick = secondOnclick;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (!isFirstPosition(position)) {
            showWarnDialog(position);
        }
        return true;
    }

    private boolean isFirstPosition(int position) {
        if (position == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Show a warning message for deleting files
     *
     * @param position the grid id
     */
    public void showWarnDialog(final int position) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.delete_warning);
        dialog.setMessage(R.string.warning);
        dialog.setCancelable(false);
        dialog.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            Map<String, Object> map;

            File pngFile;

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this, R.string.delete, Toast.LENGTH_SHORT).show();
                if (dataList == null || position < 0) {
                    Log.e(TAG, "datalist is null");
                    return;
                }
                if (dataList.get(position) instanceof Map) {
                    map = (Map<String, Object>) dataList.get(position);
                }
                utilOperation = new UtilOperation(MainActivity.this);
                String tableId = String.valueOf(map.get("uuid"));
                String textFileName = String.valueOf(map.get("text"));
                if (map.get("image") instanceof File) {
                    pngFile = (File) map.get("image");
                }
                String mFilePath = getExternalFilesDir(null).getAbsolutePath();
                FileUtils.deleteSingleFile(mFilePath + "/" + textFileName);
                FileUtils.deleteSingleFile(mFilePath + "/" + pngFile.getName());
                utilOperation.delData("uuid = ?", new String[] {tableId});
                dataList.remove(position);
                simpleAdapter.notifyDataSetChanged();
            }
        });
        dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this, R.string.cancel, Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }
}