package com.takwolf.app.demo;

import android.app.Activity;
import android.os.Bundle;

import com.takwolf.android.lock9.Lock9View;

public class MainActivity extends Activity {

	private Lock9View lock9View;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		lock9View = (Lock9View) findViewById(R.id.lock_9_view);
	}

}
