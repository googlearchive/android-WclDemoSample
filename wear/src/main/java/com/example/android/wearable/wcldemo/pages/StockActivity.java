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

import android.os.Bundle;
import android.os.Handler;
import android.support.wearable.activity.WearableActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.wearable.Node;
import com.google.devrel.wcl.Utils;
import com.google.devrel.wcl.WearManager;
import com.google.devrel.wcl.callbacks.AbstractWearConsumer;
import com.google.devrel.wcl.connectivity.WearHttpHelper;

import com.example.android.wearable.wcldemo.R;
import com.example.android.wearable.wcldemo.WearApplication;
import com.example.android.wearable.wcldemo.common.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * A simple activity that makes an HTTP request to get the GOOGLE stock price and if successful,
 * presents the result to the user. The HTTP request is made using the {@link WearHttpHelper}
 * helper class.
 */
public class StockActivity extends WearableActivity
        implements WearHttpHelper.OnHttpResponseListener {

    private static final String TAG = "StockActivity";
    private static final String url =
            "http://finance.google.com/finance/info?client=ig&q=NASDAQ%3aGOOG";
    private TextView mSymbol;
    private TextView mValue;
    private TextView mTime;
    private Button mSubmit;
    private Handler mHandler;
    private ProgressBar mProgressBar;
    private AbstractWearConsumer mConsumer;
    private WearManager mWearManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
        setContentView(R.layout.stock);
        setAmbientEnabled();
        mWearManager = WearManager.getInstance();
        mConsumer = new AbstractWearConsumer() {
            @Override
            public void onWearableApiConnected() {
                updateButton(true);
            }

            @Override
            public void onWearableApiConnectionFailed() {
                Log.d(TAG, "Connection failed");
                updateButton(false);
            }

            @Override
            public void onWearableApiConnectionSuspended() {
                Log.d(TAG, "Connection suspended");
                updateButton(false);
            }
        };
        setupViews();
        updateButton(mWearManager.isConnected());
        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearValues();
                Set<Node> nodes = mWearManager
                        .getNodesForCapability(Constants.CAPABILITY_HTTP_HANDLER);
                Log.d(TAG, "available http handler nodes: " + nodes);
                String nodeId = null;
                Node node = Utils.filterForNearby(nodes);
                if (node != null) {
                    nodeId = node.getId();
                } else {
                    Toast.makeText(StockActivity.this, R.string.no_node_available,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                mProgressBar.setVisibility(View.VISIBLE);
                try {
                    new WearHttpHelper.Builder(url, StockActivity.this)
                            .setHttpMethod(WearHttpHelper.METHOD_GET) // optional, GET is default
                            .setTargetNodeId(nodeId)
                            .setHttpResponseListener(StockActivity.this)
                            // default timeout is 15000 ms = 15 seconds
                            .setTimeout(TimeUnit.SECONDS.toMillis(10))
                            .build()
                            .makeHttpRequest();
                } catch (IllegalStateException e) {
                    Log.e(TAG, "No Api Client Connection");
                    mProgressBar.setVisibility(View.GONE);
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Arguments are missing for the http call", e);
                    mProgressBar.setVisibility(View.GONE);
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        // register our listener
        mWearManager.addWearConsumer(mConsumer);
        WearApplication.setPage(Constants.TARGET_STOCK);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // remove our listener
        mWearManager.removeWearConsumer(mConsumer);
    }

    private void updateButton(boolean enabled) {
        mSubmit.setEnabled(enabled);
    }

    @Override
    public void onHttpResponseReceived(String requestId, int status, String response) {
        Log.d(TAG, "Request Id: " + requestId + " Status: " + status + ", response: " + response);
        int toastMessageResource = 0;
        switch (status) {
            case HttpURLConnection.HTTP_OK:
                response = response.substring(3);
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    // parse the json response
                    if (jsonArray.length() == 1) {
                        JSONObject jsonObj = (JSONObject) jsonArray.get(0);
                        final String symbol = jsonObj.getString("t");
                        final double currentValue = jsonObj.getDouble("l_cur");
                        final String time = jsonObj.getString("lt");
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                updateValues(symbol, currentValue + "", time);
                            }
                        });
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing json", e);
                    toastMessageResource = R.string.error_request_failed;
                }
                break;
            case WearHttpHelper.ERROR_REQUEST_FAILED:
                Log.e(TAG, "Request failed");
                toastMessageResource = R.string.error_request_failed;
                break;
            case WearHttpHelper.ERROR_TIMEOUT:
                Log.e(TAG, "Timeout happened while waiting for response");
                toastMessageResource = R.string.error_timeout;
                break;
            default:
                Log.e(TAG, "A non-successful status code: " + status + " was received");
                toastMessageResource = R.string.error_request_failed;
        }
        final int messageResource = toastMessageResource;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mProgressBar.setVisibility(View.GONE);
                if (messageResource > 0) {
                    Toast.makeText(StockActivity.this, messageResource, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void clearValues() {
        updateValues("", "", "");
    }

    private void updateValues(String symbol, String value, String time) {
        mSymbol.setText(symbol);
        mValue.setText(value);
        SimpleDateFormat sdf = new SimpleDateFormat("LLL d, hh:mma zzz");
        try {
            if (TextUtils.isEmpty(time)) {
                mTime.setText("");
            } else {
                Date date = sdf.parse(time);
                sdf = new SimpleDateFormat("dd/MM hh:mm");
                mTime.setText(sdf.format(date));
            }
        } catch (ParseException e) {
            Log.e(TAG, "Failed to parse the date " + time, e);
            mTime.setText(time);
        }
    }

    private void setupViews() {
        mSubmit = (Button) findViewById(R.id.button);
        mSymbol = (TextView) findViewById(R.id.textView4);
        mValue = (TextView) findViewById(R.id.textView5);
        mTime = (TextView) findViewById(R.id.textView6);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
    }
}
