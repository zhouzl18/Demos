package com.feng.recyclerviewdemo;

import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.feng.recyclerviewdemo.view.CountDownView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_count_down_view);

        init();
    }

    private void init() {
        final CountDownView countDownView = (CountDownView) findViewById(R.id.count_down_view);
        long mills = 5000;
        long interval = 100;
        CountDownTimer timer = new CountDownTimer(mills, interval) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.i(TAG, "onTick: millisUntilFinished = " + millisUntilFinished);
                long countSecond = millisUntilFinished / 1000 + (millisUntilFinished % 1000 != 0 ? 1 : 0);
                countDownView.setText(String.valueOf(countSecond));
                countDownView.setProgress(360);

            }

            @Override
            public void onFinish() {

            }
        };
        countDownView.setText("5");
    }
}
