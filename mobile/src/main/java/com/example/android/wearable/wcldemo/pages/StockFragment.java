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
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.devrel.wcl.WearManager;
import com.google.devrel.wcl.callbacks.AbstractWearConsumer;
import com.google.devrel.wcl.connectivity.WearHttpHelper;

import com.example.android.wearable.wcldemo.MobileApplication;
import com.example.android.wearable.wcldemo.R;
import com.example.android.wearable.wcldemo.common.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * A fragment that shows how a wear application can use the companion app on a phone to perform
 * network calls on its behalf. In this fragment, when we receive such a request from the wear
 * device, we show a bit of information about the request, such as the url for the request and the
 * nodeId of the node that sent the request. Then this fragment makes the network call and when it
 * has the response, it forwards the response to the originating node and show the status of the
 * response on the screen.
 */
public class StockFragment extends Fragment {

    private static final String TAG = "StockFragment";
    private WearManager mWearManager;
    private AbstractWearConsumer mWearConsumer;
    private TextView mMessageView;
    private Handler mHandler;

    // A callback to report the status of the response
    private ResultCallback<MessageApi.SendMessageResult> mResultCallback
            = new ResultCallback<MessageApi.SendMessageResult>() {
        @Override
        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
            String message;
            if (sendMessageResult.getStatus().isSuccess()) {
                message = "Response: sent the response to device";
            } else {
                message = "Response: failed to send the response";
            }
            writeMessage(message, true);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
        setUpWearListeners();
    }

    private void setUpWearListeners() {
        mWearManager = WearManager.getInstance();

        // Registering a listener to inform us when an http request is coming in from another node
        // and also when we know the status of the response sent back to the originating node
        mWearConsumer = new AbstractWearConsumer() {
            @Override
            public void onWearableSendMessageResult(int statusCode) {
                if (statusCode != CommonStatusCodes.SUCCESS) {
                    Log.d(TAG, "Failed to send message, statusCode: " + statusCode);
                    Toast.makeText(getContext(),
                            getString(R.string.failed_to_launch_wear_app),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onWearableHttpRequestReceived(String url, String method, String query,
                    String charset, String nodeId, String requestId) {
                String message = String.format("Node Id: %s\n\nUrl: %s\n", nodeId, url);
                writeMessage("An Http Request received:\n\n" + message, false);
                handleHttpRequest(url, method, query, charset, nodeId, requestId);
            }

        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.stock, container, false);
        mMessageView = (TextView) view.findViewById(R.id.message);

        return view;
    }


    private void handleHttpRequest(final String url, final String method, final String query,
            final String charset, final String nodeId, final String requestId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    makeHttpCall(url, method, query, charset, nodeId, requestId);
                } catch (IOException e) {
                    Log.e(TAG, "Failed to make the http call", e);
                    WearManager.getInstance()
                            .sendHttpResponse("", HttpURLConnection.HTTP_BAD_REQUEST, nodeId,
                                    requestId, mResultCallback);
                }
            }
        }).start();

    }


    /**
     * A simple method that handles making an http request. Although we simplify this example since
     * we know what type of request is coming in, we leave it at a more generic standing to show
     * how a more complicated case can be handled.
     *
     * @param url The target URL. For GET operations, all the query parameters need to be included
     * here directly. For POST requests, use the <code>query</code> parameter.
     * @param method Can be {@link com.google.devrel.wcl.connectivity.WearHttpHelper#METHOD_GET}
     * or {@link com.google.devrel.wcl.connectivity.WearHttpHelper#METHOD_POST}
     * @param query Used for POST requests. The format should be
     * <code>param1=value1&param2=value2&..</code>
     * and it <code>value1, value2, ...</code> should all be URLEncoded by the caller.
     *
     * @throws IOException
     */
    private void makeHttpCall(String url, String method, String query, String charset,
            String nodeId, String requestId) throws IOException {
        URLConnection urlConnection = new URL(url).openConnection();
        urlConnection.setRequestProperty("Accept-Charset", charset);
        // Note: the following if-clause will not be executed for this particular example
        if (WearHttpHelper.METHOD_POST.equals(method)) {
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded;charset=" + charset);
            if (!TextUtils.isEmpty(query)) {
                OutputStream output = urlConnection.getOutputStream();
                output.write(query.getBytes(charset));
            }
        }
        BufferedReader in = new BufferedReader(
                new InputStreamReader(urlConnection.getInputStream()));
        String inputLine;
        StringBuilder sb = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            sb.append(inputLine);
        }
        in.close();
        int statusCode = ((HttpURLConnection) urlConnection).getResponseCode();
        WearManager.getInstance().sendHttpResponse(sb.toString(), statusCode, nodeId, requestId,
                mResultCallback);
    }

    /**
     * Write a message to the display; it can append to the existing message if {@code append} is
     * {@code true}.
     */
    private void writeMessage(final String message, final boolean append) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                String msg = message;
                if (append) {
                    msg = mMessageView.getText().toString() + "\n" + msg;
                }
                mMessageView.setText(msg);
            }
        });
    }

    @Override
    public void onPause() {
        mWearManager.removeWearConsumer(mWearConsumer);
        mWearManager.removeCapabilities(Constants.CAPABILITY_HTTP_HANDLER);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mWearManager.addWearConsumer(mWearConsumer);
        mWearManager.addCapabilities(Constants.CAPABILITY_HTTP_HANDLER);
        MobileApplication.setPage(Constants.TARGET_STOCK);
        writeMessage("", false);
    }
}
