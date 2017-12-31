package com.example.jiashuai.myapplicationrxjava2.rxjava2case;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.example.jiashuai.myapplicationrxjava2.R;
import com.example.jiashuai.myapplicationrxjava2.adapter.AppListAdapter;
import com.example.jiashuai.myapplicationrxjava2.bean.AppInfoBean;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by JiaShuai on 2017/12/23.
 */

public class PhoneAppListActivity extends AppCompatActivity {
    private ListView app_list;
    private AppListAdapter appListAdapter;
    private List<AppInfoBean> beanList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phone_app_list);
        app_list = findViewById(R.id.app_list);
        beanList = new ArrayList<>();
        appListAdapter = new AppListAdapter(this, beanList);
        app_list.setAdapter(appListAdapter);
        List<ApplicationInfo> applicationInfoList = getPackageManager().getInstalledApplications(PackageManager.MATCH_UNINSTALLED_PACKAGES);

        Observable.fromIterable(applicationInfoList)
                .filter(new Predicate<ApplicationInfo>() {
                    @Override
                    public boolean test(ApplicationInfo applicationInfo) throws Exception {
                        //过滤系统app
                        return (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0;
                    }
                })
                .map(new Function<ApplicationInfo, AppInfoBean>() {
                    @Override
                    public AppInfoBean apply(ApplicationInfo applicationInfo) throws Exception {
                        AppInfoBean bean = new AppInfoBean();
                        bean.appIcon = applicationInfo.loadIcon(getPackageManager());
                        bean.appName = applicationInfo.loadLabel(getPackageManager()).toString();
                        return bean;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<AppInfoBean>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(AppInfoBean appInfoBean) {
                beanList.add(appInfoBean);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                appListAdapter.notifyDataSetChanged();
            }
        });

    }
}
