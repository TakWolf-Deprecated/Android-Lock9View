package com.takwolf.android.lock9view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_normal)
    void onBtnNormalClick() {
        startActivity(new Intent(this, NormalActivity.class));
    }

    @OnClick(R.id.btn_l_style)
    void onBtnLStyleClick() {
        startActivity(new Intent(this, LStyleActivity.class));
    }

}
