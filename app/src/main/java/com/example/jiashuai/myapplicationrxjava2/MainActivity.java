package com.example.jiashuai.myapplicationrxjava2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.jiashuai.myapplicationrxjava2.rxjava2case.PhoneAppListActivity;
import com.example.jiashuai.myapplicationrxjava2.rxjava2case.UpDatesUiActivity;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class MainActivity extends AppCompatActivity {
    private TextView my_tv;
    private EditText my_edit;
    private TextView my_tv_test_debounce;
    private ObservableEmitter<Integer> mEmitter;

    private PublishSubject<String> selectSubject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        my_tv = findViewById(R.id.my_tv);
        my_tv.setOnClickListener(v -> backpressure());

        my_edit = findViewById(R.id.my_edit);
        my_tv_test_debounce = findViewById(R.id.my_tv_test_debounce);

        selectptimization();
    }

    /**
     * 背压测试
     */
    public void backpressure() {
        Observable.interval(1, TimeUnit.SECONDS)
                .take(10)
                .doOnNext(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        System.out.println("-------" + aLong);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                Thread.sleep(3000);
                System.out.println(aLong);
            }
        });
    }


    /**
     *优化搜索联想功能
     */
    public void selectptimization(){


        selectSubject = PublishSubject.create();
        selectSubject
                .debounce(200, TimeUnit.MILLISECONDS)
                .switchMap(new Function<String, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(String query) throws Exception {
                if (TextUtils.isEmpty(query)) {
                    return Observable.just("");
                }
                return Observable.create(new ObservableOnSubscribe<String>() {
                    @Override
                    public void subscribe(ObservableEmitter<String> observableEmitter) throws Exception {
                        Log.d("SearchActivity", "开始请求，关键词为：" + query);
                        try {
                            Thread.sleep(100 + (long) (Math.random() * 500));
                        } catch (InterruptedException e) {
                            if (!observableEmitter.isDisposed()) {
                                observableEmitter.onError(e);
                            }
                        }
                        Log.d("SearchActivity", "结束请求，关键词为：" + query);
                        observableEmitter.onNext("完成搜索，关键词为：" + query);
                        observableEmitter.onComplete();

                    }
                }).subscribeOn(Schedulers.io());
            }
        })
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new DisposableObserver<String>() {
            @Override
            public void onNext(String s) {
                my_tv_test_debounce.setText(s);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
        my_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                selectSubject.onNext(s.toString());
            }
        });
    }


    public void goPhoneAppListActivity(View view) {
        startActivity(new Intent(this,PhoneAppListActivity.class));
    }
    public void goUpDatesUiActivity(View view){
        startActivity(new Intent(this,UpDatesUiActivity.class));
    }
}
