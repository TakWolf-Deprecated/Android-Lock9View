package com.takwolf.android.lock9view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import com.takwolf.android.lock9.Lock9View;

import butterknife.Bind;
import butterknife.ButterKnife;

public class NormalActivity extends AppCompatActivity {

    @Bind(R.id.lock_9_view)
    protected Lock9View lock9View;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal);
        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        lock9View.setCallBack(new Lock9View.CallBack() {

            @Override
            public void onFinish(String password) {
                Toast.makeText(NormalActivity.this, password, Toast.LENGTH_SHORT).show();
            }

        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
