package com.example.jiashuai.myapplicationrxjava2.rxjava2case;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.example.jiashuai.myapplicationrxjava2.R;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;


/**
 * Created by JiaShuai on 2017/12/30.
 */

public class UpDatesUiActivity extends AppCompatActivity {
    private TextView tv_thread_change_ui;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.thread_change_ui);
        tv_thread_change_ui = findViewById(R.id.tv_thread_change_ui);
        tv_thread_change_ui.setOnClickListener((view) -> changeStart());

    }

    private void changeStart() {
        Observable.interval(500, TimeUnit.MILLISECONDS).take(100).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {

                tv_thread_change_ui.setText("模拟下载进度：" + aLong);
            }
        });

    }
}
