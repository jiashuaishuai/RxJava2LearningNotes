package com.example.jiashuai.myapplicationrxjava2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jiashuai.myapplicationrxjava2.R;
import com.example.jiashuai.myapplicationrxjava2.bean.AppInfoBean;

import java.util.List;

/**
 * Created by JiaShuai on 2017/12/23.
 */

public class AppListAdapter extends BaseAdapter {
    private List<AppInfoBean> appInfoBeanList;
    private LayoutInflater inflater;

    public AppListAdapter(Context context, List<AppInfoBean> appInfoBeanList) {
        this.appInfoBeanList = appInfoBeanList;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return appInfoBeanList == null ? 0 : appInfoBeanList.size();
    }

    @Override
    public AppInfoBean getItem(int position) {
        return appInfoBeanList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AppInfoBean bean = getItem(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.appIcon.setImageDrawable(bean.appIcon);
        viewHolder.appName.setText(bean.appName);

        return convertView;
    }

    class ViewHolder {
        public ImageView appIcon;
        public TextView appName;

        public ViewHolder(View convertView) {
            appIcon = convertView.findViewById(R.id.app_icon);
            appName = convertView.findViewById(R.id.app_name);
        }

    }

}
