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
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageEvent;
import com.google.devrel.wcl.WearManager;
import com.google.devrel.wcl.callbacks.AbstractWearConsumer;

import com.example.android.wearable.wcldemo.MobileApplication;
import com.example.android.wearable.wcldemo.R;
import com.example.android.wearable.wcldemo.common.Constants;

/**
 * A simple fragment that shows the exchange of data between the phone and wear apps. When this
 * activity is in front on the mobile device, user will see messages as the user navigates between
 * different pages of the companion wear application.
 */
public class DataExchangeFragment extends Fragment {

    private WearManager mWearManager;
    private AbstractWearConsumer mWearConsumer;
    private TextView mMessageTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWearManager = WearManager.getInstance();

        // We register a listener to be notified when messages arrive while we are on this page.
        // We then filter messages based on their path to identify the ones that report on the
        // navigation in the companion wear app. When such message is discovered, we write the name
        // of new page to the view.
        mWearConsumer = new AbstractWearConsumer() {
            @Override
            public void onWearableMessageReceived(MessageEvent messageEvent) {
                if (!Constants.NAVIGATION_PATH_WEAR.equals(messageEvent.getPath())) {
                    return;
                }
                DataMap dataMap = DataMap.fromByteArray(messageEvent.getData());
                int currentPage  = dataMap.getInt(Constants.KEY_PAGE, Constants.TARGET_INTRO);
                writeMessage(currentPage);
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.data_exchange, container, false);
        mMessageTextView = (TextView) view.findViewById(R.id.message);
        return view;
    }

    /**
     * Mapping the id of page to its name and displaying the name
     */
    private void writeMessage(final int page) {
        String pageName = null;
        switch (page) {
            case Constants.TARGET_INTRO:
                break;
            case Constants.TARGET_DATA:
                pageName = getString(R.string.page_data_exchange);
                break;
            case Constants.TARGET_FILE_TRANSFER:
                pageName = getString(R.string.page_file_transfer);
                break;
            case Constants.TARGET_STOCK:
                pageName = getString(R.string.page_stock);
                break;
            case Constants.TARGET_LIST:
                pageName = getString(R.string.page_list);
                break;
            case Constants.TARGET_LIST_DIALOG:
                pageName = getString(R.string.page_list_dialog);
                break;
            case Constants.TARGET_VOICE_STREAM:
                pageName = getString(R.string.page_stream_voice);
                break;
            default:
                pageName = getString(R.string.unknown);
        }
        final String text = getString(R.string.page_navigation_info, pageName);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMessageTextView.setText(text);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mWearManager.addWearConsumer(mWearConsumer);
        MobileApplication.setPage(Constants.TARGET_DATA);
    }

    @Override
    public void onPause() {
        mWearManager.removeWearConsumer(mWearConsumer);
        super.onPause();
    }
}
