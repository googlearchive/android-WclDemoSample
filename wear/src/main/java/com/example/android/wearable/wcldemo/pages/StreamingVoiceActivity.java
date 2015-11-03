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

package com.example.android.wearable.wcldemo.pages;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.devrel.wcl.widgets.recording.WclRecorderActivity;

import com.example.android.wearable.wcldemo.R;
import com.example.android.wearable.wcldemo.WearApplication;
import com.example.android.wearable.wcldemo.common.Constants;

/**
 *
 */
public class StreamingVoiceActivity extends WearableActivity {

    private static final String TAG = "StreamingVoiceActivity";
    private static final int RECORDING_REQUEST_CODE = 1;
    private static final int PERMISSIONS_REQUEST_CODE = 2;
    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stream_voice);
        setAmbientEnabled();
        mButton = (Button) findViewById(R.id.button);
        checkPermissions();
    }

    public void onClick(View view) {

        Intent intent = new Intent(this, WclRecorderActivity.class);
        intent.putExtra(WclRecorderActivity.EXTRA_STREAMING, true);
        intent.putExtra(WclRecorderActivity.EXTRA_NODE_CAPABILITY,
                Constants.CAPABILITY_VOICE_PROCESSING);
        startActivityForResult(intent, RECORDING_REQUEST_CODE);
    }

    /**
     * Checks the permission that this app needs and if it has not been granted, it will
     * prompt the user to grant it, otherwise it shuts down the app.
     */
    private void checkPermissions() {
        boolean recordAudioPermissionGranted =
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                        == PackageManager.PERMISSION_GRANTED;

        if (recordAudioPermissionGranted) {
            enableButton();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSIONS_REQUEST_CODE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == RECORDING_REQUEST_CODE) {
            int reason = data.getIntExtra(WclRecorderActivity.EXTRA_RECORDING_STATUS, -1);
            if (reason == WclRecorderActivity.STATUS_ERROR_NODE_NOT_FOUND) {
                Toast.makeText(this, R.string.no_node_available, Toast.LENGTH_SHORT).show();
            }
            Log.d(TAG, "Activity finished, reason: " + reason);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
            @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableButton();
            } else {
                // Permission has been denied before. At this point we should show a dialog to
                // user and explain why this permission is needed and direct him to go to the
                // Permissions settings for the app in the System settings. For this sample, we
                // simply exit to get to the important part.
                Toast.makeText(this, R.string.exiting_for_permission, Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        WearApplication.setPage(Constants.TARGET_VOICE_STREAM);
    }

    private void enableButton() {
        mButton.setEnabled(true);
    }

}
