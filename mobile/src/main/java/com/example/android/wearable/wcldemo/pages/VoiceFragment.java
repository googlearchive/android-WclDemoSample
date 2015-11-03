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
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.wearable.Channel;
import com.google.android.gms.wearable.WearableStatusCodes;
import com.google.devrel.wcl.WearManager;
import com.google.devrel.wcl.callbacks.AbstractWearConsumer;
import com.google.devrel.wcl.widgets.recording.WclSoundManager;

import com.example.android.wearable.wcldemo.MobileApplication;
import com.example.android.wearable.wcldemo.R;
import com.example.android.wearable.wcldemo.common.Constants;

import java.io.InputStream;

/**
 * The introductory fragment.
 */
public class VoiceFragment extends Fragment {

    private static final String TAG = "VoiceFragment";
    private WearManager mWearManager;
    private AbstractWearConsumer mWearConsumer;
    private WclSoundManager mSoundManager;
    private TextView mMessageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpWearListeners();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.voice_fragment, container, false);
        mMessageView = (TextView) view.findViewById(R.id.message);
        return view;
    }

    /**
     * Creates a listener to be called when a channel and an input stream is available to receive
     * sound stream.
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
                mMessageView.setText(R.string.voice_stream_started);
                mSoundManager = new WclSoundManager(getActivity());
                mSoundManager.play(inputStream, new WclSoundManager.OnVoicePlaybackFinishedListener() {
                    @Override
                    public void onPlaybackFinished(int reason, @Nullable String reasonMessage) {
                        Log.d(TAG, "Voice ended with reason: " + reason);
                        mMessageView.setText(R.string.voice_stream_ended);
                    }
                });
            }

        };
    }

    @Override
    public void onResume() {
        super.onResume();
        mWearManager.addWearConsumer(mWearConsumer);
        mWearManager.addCapabilities(Constants.CAPABILITY_VOICE_PROCESSING);
        MobileApplication.setPage(Constants.TARGET_VOICE_STREAM);
    }

    @Override
    public void onPause() {
        mWearManager.removeWearConsumer(mWearConsumer);
        mWearManager.removeCapabilities(Constants.CAPABILITY_VOICE_PROCESSING);
        if (mSoundManager != null) {
            mSoundManager.cleanUp();
        }
        super.onPause();
    }
}
