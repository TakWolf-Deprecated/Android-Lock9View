# Android-Lock9View #

> An Android grid lock screen view with a callback interface. It is very simple to use.

## ScreenShot ##

![Screenshot](screenshot/1.png)

## Usage ##

### Layout ###

```xml
<com.takwolf.android.lock9.Lock9View
    android:id="@+id/lock_9_view"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```

### Activity ###

```java
Lock9View lock9View = (Lock9View) findViewById(R.id.lock_9_view);
lock9View.setCallBack(new CallBack() {
    @Override
    public void onFinish(String password) {
        Toast.makeText(MainActivity.this, password, Toast.LENGTH_SHORT).show();
    }
});
```

## Author ##

Please see [TakWolf](http://takwolf.com).

## License ##

Licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).