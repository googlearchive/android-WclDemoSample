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

package com.example.android.wearable.wcldemo.common;

/**
 * A collection of constants that is used across both the mobile and wear modules.
 */
public class Constants {

    public static final String KEY_TARGET = "target";
    public static final int TARGET_INTRO = 0;
    public static final int TARGET_FILE_TRANSFER = 1;
    public static final int TARGET_STOCK = 2;
    public static final int TARGET_DATA = 3;
    public static final int TARGET_LIST_DIALOG = 4;
    public static final int TARGET_LIST = 5;
    public static final int TARGET_VOICE_STREAM = 6;

    public static final String NAVIGATION_PATH_MOBILE = "/navpath/mobile";
    public static final String NAVIGATION_PATH_WEAR = "/navpath/wear";
    public static final String KEY_PAGE = "key-page";

    /**
     * Various capabilities that the handheld app will provide
     */
    public static final String CAPABILITY_FILE_PROCESSOR = "file_processor";
    public static final String CAPABILITY_HTTP_HANDLER = "http_handler";
    public static final String CAPABILITY_VOICE_PROCESSING = "voice_processor";

}
