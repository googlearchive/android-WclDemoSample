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

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.devrel.wcl.WearManager;

import com.example.android.wearable.wcldemo.common.Constants;
import com.example.android.wearable.wcldemo.pages.DataExchangeFragment;
import com.example.android.wearable.wcldemo.pages.FileTransferFragment;
import com.example.android.wearable.wcldemo.pages.IntroFragment;
import com.example.android.wearable.wcldemo.pages.StockFragment;
import com.example.android.wearable.wcldemo.pages.VoiceFragment;

/**
 * The main activity for this mobile application. It opens up with an introductory page and offers
 * a drawer that can take user to different pages; each page show cases one feature of the library.
 */
public class MobileMainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MobileMainActivity";
    private static final String WEAR_APP_CAPABILITY = "wear_app_capability";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mobile_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchWatchActivity(MobileApplication.getPage());
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        IntroFragment fragment = new IntroFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, fragment).commit();

        if (getIntent() != null && getIntent().getExtras() != null) {
            Bundle bundle = getIntent().getExtras();
            int target = bundle.getInt(Constants.KEY_TARGET);
            Log.d(TAG, "target = " + target);
            navigateTo(target);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_open_menu:
                navigateTo(Constants.TARGET_INTRO);
                break;
            case R.id.nav_open_stock:
                navigateTo(Constants.TARGET_STOCK);
                break;
            case R.id.nav_open_data:
                navigateTo(Constants.TARGET_DATA);
                break;
            case R.id.nav_open_file_transfer:
                navigateTo(Constants.TARGET_FILE_TRANSFER);
                break;
            case R.id.nav_open_voice_stream:
                navigateTo(Constants.TARGET_VOICE_STREAM);
                break;

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void navigateTo(int target) {
        if (MobileApplication.getPage() == target) {
            return;
        }
        Fragment fragment = null;
        switch (target) {
            case Constants.TARGET_INTRO:
                fragment = new IntroFragment();
                break;
            case Constants.TARGET_FILE_TRANSFER:
                fragment = new FileTransferFragment();
                break;
            case Constants.TARGET_STOCK:
                fragment = new StockFragment();
                break;
            case Constants.TARGET_DATA:
                fragment = new DataExchangeFragment();
                break;
            case Constants.TARGET_VOICE_STREAM:
                fragment = new VoiceFragment();
                break;
        }

        if (fragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);

            transaction.commit();
        }
    }

    /**
     * Launches various activities of the wear companion app.
     */
    private void launchWatchActivity(int target) {
        WearManager wearManager = WearManager.getInstance();
        if (!wearManager.isConnected()) {
            toastMessage(R.string.api_client_not_connected);
            return;
        }
        String activityName = null;
        switch (target) {
            case Constants.TARGET_INTRO:
                activityName = ".MyListActivity";
                break;
            case Constants.TARGET_FILE_TRANSFER:
                activityName = ".pages.FileTransferActivity";
                break;
            case Constants.TARGET_STOCK:
                activityName = ".pages.StockActivity";
                break;
            case Constants.TARGET_DATA:
                activityName = ".pages.DataExchangeActivity";
                break;
            case Constants.TARGET_VOICE_STREAM:
                activityName = ".pages.StreamingVoiceActivity";
                break;
        }
        if (!wearManager
                .launchAppOnNodes(MobileApplication.getPackage() + activityName, null, false,
                        WEAR_APP_CAPABILITY, null)) {
            toastMessage(R.string.no_wearable_device);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobileApplication.setPage(Constants.TARGET_INTRO);
    }

    private void toastMessage(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }
}
