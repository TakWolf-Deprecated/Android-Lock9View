package com.takwolf.android.lock9view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.takwolf.android.lock9.Lock9View;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NormalActivity extends AppCompatActivity {

    @BindView(R.id.lock_9_view)
    Lock9View lock9View;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal);
        ButterKnife.bind(this);

        lock9View.setCallBack(new Lock9View.CallBack() {


            @Override
            public void onFinish(List<String> passwordList) {
                for (String s : passwordList) {
                    System.out.println(s);
                }
            }

            @Override
            public void onPassedPoint(List<String> nodeList) {
                for (String s : nodeList) {
                    System.out.println(s);
                }
            }
        });
    }

}
