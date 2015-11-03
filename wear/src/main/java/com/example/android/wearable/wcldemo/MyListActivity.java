/*
 * Copyright (C) 2015 Google Inc. All Rights Reserved.
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
 * imitations under the License.
 */

package com.example.android.wearable.wcldemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.devrel.wcl.WearManager;
import com.google.devrel.wcl.widgets.list.AbstractWearableListViewActivity;
import com.google.devrel.wcl.widgets.list.WearableListConfig;

import com.example.android.wearable.wcldemo.common.Constants;
import com.example.android.wearable.wcldemo.pages.DataExchangeActivity;
import com.example.android.wearable.wcldemo.pages.FileTransferActivity;
import com.example.android.wearable.wcldemo.pages.StockActivity;
import com.example.android.wearable.wcldemo.pages.StreamingVoiceActivity;

/**
 * The entry point to the application. This activity also serves as a demo of the
 * {@link AbstractWearableListViewActivity}; clients need to only implement two methods in this
 * class to have a fully functional ListView. In this activity, we also demo a different way to show
 * a WearableListView by calling {@link WearManager#showWearableList(Activity, WearableListConfig)};
 * that approach is useful when you want to present a list to the user and close the list after user
 * makes a selection; the result can be captured by
 * {@link Activity#onActivityResult(int, int, Intent)} as shown here.
 *
 * @see AbstractWearableListViewActivity
 */
public class MyListActivity extends AbstractWearableListViewActivity {

    private static final String TAG = "MyListActivity";
    private static final int REQUEST_CODE_LIST_DIALOG = 1;
    private static final String MOBILE_APP_CAPABILITY = "mobile_application";
    private WearManager mWearManager;
    private static final String MOBILE_APP_ACTIVITY_NAME
            = "com.example.android.wearable.wcldemo.MobileMainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWearManager = WearManager.getInstance();
    }

    @Override // AbstractWearableListViewActivity
    public void onItemClicked(int position, String value) {
        Log.d(TAG, "Item clicked: position=" + position + ", value=" + value);
        switch (position) {
            case 0:
                showActivity(DataExchangeActivity.class);
                break;
            case 1:
                showActivity(FileTransferActivity.class);
                break;
            case 2:
                showListDialog();
                break;
            case 3:
                showActivity(StockActivity.class);
                break;
            case 4:
                showActivity(StreamingVoiceActivity.class);
                break;
            case 5:
                launchMobileApp();
                break;
        }
    }

    @NonNull
    @Override // AbstractWearableListViewActivity
    public WearableListConfig getConfiguration() {
        String[] data = new String[]{
                getString(R.string.nav_open_data_exchange),
                getString(R.string.nav_open_file_transfer),
                getString(R.string.list_dialog),
                getString(R.string.nav_open_stock),
                getString(R.string.stream_voice),
                getString(R.string.launch_mobile)
        };

        int[] icons = new int[]{
                R.drawable.ic_import_export_24dp,
                R.drawable.ic_file_upload_24dp,
                R.drawable.ic_list_24dp,
                R.drawable.ic_language_24dp,
                R.drawable.ic_mic_24dp,
                R.drawable.ic_phone_android_24dp
        };
        // we build a configuration object that allows us to show a list of items where each row
        // is represented by a different icon. We also turn on the ambient mode for this page.
        return new WearableListConfig.Builder(data)
                .setIcons(icons)
                .setAmbient(true)
                .setHeader(getString(R.string.app_name))
                .build();
    }

    /**
     * This method shows how you can open a full screen list of options for user to make a
     * selection. The result of that selection will be captured by the {@code onActivityResult}
     * below.
     */
    private void showListDialog() {
        // some dummy values to show in the list
        String[] data = new String[]{"Option 1", "Option 2", "Option 3", "Option 4"};
        WearableListConfig config = new WearableListConfig.Builder(data)
                .setCheckedIndex(1) // index starts from 0
                .setRequestCode(REQUEST_CODE_LIST_DIALOG)
                .setIcon(R.drawable.ic_person_24dp) // here we use the same icon for all rows
                .setCheckable(true) // we declare that we can have "checked" state
                .build();
        WearApplication.sendNavMessage(Constants.TARGET_LIST_DIALOG);
        WearManager.getInstance().showWearableList(this, config);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            WearableListConfig.WearableListResult result
                    = new WearableListConfig.WearableListResult(data, requestCode);
            if (result.isHandled()) {
                Log.d(TAG, String.format("Position: %d, Value: %s",
                        result.getSelectedIndex(), result.getSelectedValue()));
                toastMessage(result.getSelectedValue());
            }
        }
    }

    /**
     * Opens the launcher activity of the companion app on the mobile device. We identify the mobile
     * device by the the capability that it provides: {@link #MOBILE_APP_CAPABILITY}.
     */
    private void launchMobileApp() {
        if (!mWearManager.launchAppOnNodes(MOBILE_APP_ACTIVITY_NAME, null, false,
                MOBILE_APP_CAPABILITY, null)) {
            toastMessage(R.string.failed_to_launch);
        }

    }

    private void showActivity(Class<?> activity) {
        Intent intent = new Intent(this, activity);
        startActivity(intent);
    }

    private void toastMessage(int resId) {
        toastMessage(getString(resId));
    }

    private void toastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        WearApplication.sendNavMessage(Constants.TARGET_LIST);
    }
}
