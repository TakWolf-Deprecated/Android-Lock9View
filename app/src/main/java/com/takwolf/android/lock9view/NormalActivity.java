package com.takwolf.android.lock9view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.takwolf.android.lock9.Lock9View;

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

        lock9View.setGestureCallback(new Lock9View.GestureCallback() {

            @Override
            public void onNodeConnected(@NonNull int[] numbers) {
                ToastUtils.with(NormalActivity.this).show("+ " + numbers[numbers.length - 1]);
            }

            @Override
            public void onGestureFinished(@NonNull int[] numbers) {
                StringBuilder builder = new StringBuilder();
                for (int number : numbers) {
                    builder.append(number);
                }
                ToastUtils.with(NormalActivity.this).show("= " + builder.toString());
            }

        });
    }

}
