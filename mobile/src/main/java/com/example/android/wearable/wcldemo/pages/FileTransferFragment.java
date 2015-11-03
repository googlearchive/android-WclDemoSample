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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.wearable.Channel;
import com.google.android.gms.wearable.WearableStatusCodes;
import com.google.devrel.wcl.WearManager;
import com.google.devrel.wcl.callbacks.AbstractWearConsumer;

import com.example.android.wearable.wcldemo.MobileApplication;
import com.example.android.wearable.wcldemo.R;
import com.example.android.wearable.wcldemo.common.Constants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * A fragment that receives the files transferred from the wear app. There are two files that are
 * transferred; one is a text file and an image. When the text file is transferred, this fragment
 * reads the content of the file and presents that to the user in a text box. When the image starts
 * to its transfer, this fragment shows a spinner and when the transfer is complete, it shows the
 * image.
 */
public class FileTransferFragment extends Fragment {

    private static final String TAG = "FileTransferFragment";
    private WearManager mWearManager;
    private AbstractWearConsumer mWearConsumer;
    private ImageView mImageView;
    private TextView mTextView;
    private AsyncTask<Void, Void, Bitmap> mAsyncTask;
    private ProgressBar mProgressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpWearListeners();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.file_transfer, container, false);
        mImageView = (ImageView) view.findViewById(R.id.image);
        mTextView = (TextView) view.findViewById(R.id.text);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress);
        view.findViewById(R.id.clear_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageView.setImageResource(R.drawable.ic_photo_200dp);
            }
        });
        view.findViewById(R.id.clear_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextView.setText(R.string.text_file_here);
            }
        });

        return view;
    }

    /**
     * Creates two listeners to be called when the transfer of the text file is completed and when
     * a channel and an input stream is available to receive the image file. In some cases, it is
     * desired to be transfer files even if the application at the receiving node is not in front.
     * In those cases, one can define the same {@link com.google.devrel.wcl.callbacks.WearConsumer}
     * in the application instance; then the WearableListener that the WCL library provides will be
     * able to handle the transfer.
     */
    private void setUpWearListeners() {
        mWearManager = WearManager.getInstance();
        mWearConsumer = new AbstractWearConsumer() {

            @Override
            public void onWearableInputStreamForChannelOpened(int statusCode, String requestId,
                    final Channel channel, final InputStream inputStream) {
                if (statusCode != WearableStatusCodes.SUCCESS) {
                    Log.e(TAG, "onWearableInputStreamForChannelOpened(): "
                            + "Failed to get input stream");
                    return;
                }
                Log.d(TAG, "Channel opened for path: " + channel.getPath());
                mAsyncTask = new AsyncTask<Void, Void, Bitmap>() {

                    @Override
                    protected void onPreExecute() {
                        mImageView.setImageResource(R.drawable.ic_photo_200dp);
                        mProgressBar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    protected Bitmap doInBackground(Void... params) {
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        closeStreams();
                        if (isCancelled()) {
                            return null;
                        }
                        return bitmap;
                    }

                    @Override
                    protected void onCancelled() {
                        mProgressBar.setVisibility(View.GONE);
                        mAsyncTask = null;
                        closeStreams();
                    }

                    @Override
                    protected void onPostExecute(Bitmap bitmap) {
                        mProgressBar.setVisibility(View.GONE);
                        mImageView.setImageBitmap(bitmap);
                        mAsyncTask = null;
                    }

                    public void closeStreams() {
                        try {
                            if (inputStream != null) {
                                inputStream.close();
                            }
                        } catch (IOException e) {
                            // no-op
                        }
                    }
                };
                mAsyncTask.execute();
            }

            @Override
            public void onWearableFileReceivedResult(int statusCode, String requestId,
                    File savedFile, String originalName) {
                Log.d(TAG, String.format(
                        "File Received: status=%d, requestId=%s, savedLocation=%s, originalName=%s",
                        statusCode, requestId, savedFile.getAbsolutePath(), originalName));
                String fileContent = getSimpleTextFileContent(savedFile);
                mTextView.setText(fileContent);
            }

        };
    }

    /**
     * A rudimentary method to read the content of the {@code file}.
     */
    private String getSimpleTextFileContent(File file) {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("file is null or doesn't exists!");
        }
        try {
            return new Scanner(file).useDelimiter("\\A").next();
        } catch (FileNotFoundException e) {
            // already captured
        }
        return null;
    }

    @Override
    public void onResume() {
        super.onResume();

        // register our listeners
        mWearManager.addWearConsumer(mWearConsumer);

        // add the local capability to handle file transfer
        mWearManager.addCapabilities(Constants.CAPABILITY_FILE_PROCESSOR);

        MobileApplication.setPage(Constants.TARGET_FILE_TRANSFER);
    }

    @Override
    public void onPause() {
        // unregister our listeners
        mWearManager.removeWearConsumer(mWearConsumer);

        // remove the capability to handle file transfer
        mWearManager.removeCapabilities(Constants.CAPABILITY_FILE_PROCESSOR);

        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
            mAsyncTask = null;
        }
        super.onPause();
    }
}
