# Android Lock9View #

[![Download](https://api.bintray.com/packages/takwolf/maven/Android-Lock9View/images/download.svg)](https://bintray.com/takwolf/maven/Android-Lock9View/_latestVersion) [![API](https://img.shields.io/badge/API-1%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=1) [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Android--Lock9View-brightgreen.svg?style=flat)](http://android-arsenal.com/details/1/1704)

An Android grid lock screen view with a callback interface.

## ScreenShot ##

![Screenshot](art/screenshot.png)

## Demo ##

[![Google Play Store](art/git_it_on_google_play.png)](https://play.google.com/store/apps/details?id=com.takwolf.android.lock9)

## Usage ##

### Gradle ###

    compile 'com.takwolf.android:lock9view:0.0.10'

### Layout example 1 ###

    <com.takwolf.android.lock9.Lock9View
        android:id="@+id/lock_9_view"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        app:lock9_nodeSrc="@drawable/node_normal"
        app:lock9_nodeOnSrc="@drawable/node_active"
        app:lock9_nodeOnAnim="@anim/node_on_1"
        app:lock9_padding="28dp"
        app:lock9_spacing="28dp"
        app:lock9_lineColor="@color/blue_light"
        app:lock9_lineWidth="8dp"
        app:lock9_autoLink="false"
        app:lock9_enableVibrate="true"
        app:lock9_vibrateTime="20" />

![layout_1](art/layout_1.png)

### Layout example 2 ###

    <com.takwolf.android.lock9.Lock9View
        android:id="@+id/lock_9_view"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_margin="24dp"
        android:layout_gravity="center"
        app:lock9_nodeSrc="@drawable/node_small_normal"
        app:lock9_nodeOnSrc="@drawable/node_small_active"
        app:lock9_nodeOnAnim="@anim/node_on_2"
        app:lock9_nodeSize="16dp"
        app:lock9_nodeAreaExpand="24dp"
        app:lock9_lineColor="@color/blue_light"
        app:lock9_lineWidth="4dp"
        app:lock9_autoLink="true"
        app:lock9_enableVibrate="true"
        app:lock9_vibrateTime="20" />

![layout_2](art/layout_2.png)

**PS :**

**If use *"lock9_nodeSize"* , it will ignore *"lock9_padding"* and *"lock9_spacing"*.**

**The nodes will be layout on the center of 9 average areas.**

### Touch area ###

If you want to draw a small node with a bigger touch area, please user :

    app:lock9_nodeAreaExpand="24dp"  // default is 0

touchArea = lock9_nodeSize + lock9_nodeAreaExpand * 2

### Animation ###

    app:lock9_nodeOnAnim="@anim/node_on_2"

### AutoLink ###

![auto_link](art/auto_link.png)

    app:lock9_autoLink="true" // default is false

### Vibrate ###

    app:lock9_enableVibrate="true"  // default is false
    app:lock9_vibrateTime="20"      // default is 20 milliseconds
    
Also need :

    <uses-permission android:name="android.permission.VIBRATE" />

### Error status ###

// TODO

### Activity ###

    Lock9View lock9View = (Lock9View) findViewById(R.id.lock_9_view);
    lock9View.setCallBack(new CallBack() {

        @Override
        public void onFinish(String password) {
            Toast.makeText(MainActivity.this, password, Toast.LENGTH_SHORT).show();
        }

    });

## Author ##

TakWolf

[takwolf@foxmail.com](mailto:takwolf@foxmail.com)

[http://takwolf.com](http://takwolf.com)

## License ##

    Copyright 2015-2016 TakWolf
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.