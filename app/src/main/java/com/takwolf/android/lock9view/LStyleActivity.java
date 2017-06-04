package com.takwolf.android.lock9view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.takwolf.android.lock9.Lock9View;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LStyleActivity extends AppCompatActivity {

    @BindView(R.id.lock_9_view)
    Lock9View lock9View;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_l_style);
        ButterKnife.bind(this);

        lock9View.setCallBack(new Lock9View.CallBack() {

            @Override
            public void onFinish(String password) {
                Toast.makeText(LStyleActivity.this, password, Toast.LENGTH_SHORT).show();
            }

        });
    }

}
