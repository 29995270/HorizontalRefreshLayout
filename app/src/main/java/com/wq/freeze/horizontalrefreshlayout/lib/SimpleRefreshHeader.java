package com.wq.freeze.horizontalrefreshlayout.lib;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wq.freeze.horizontalrefreshlayout.R;

/**
 * Created by wangqi on 2015/12/24.
 */
public class SimpleRefreshHeader implements RefreshHeader {

    private final Context context;
    private TextView textView;

    public SimpleRefreshHeader(Context context) {
        this.context = context;
    }

    @Override
    public View getView(ViewGroup container) {
        View view = LayoutInflater.from(context).inflate(R.layout.widget_refresh_header, container, false);
        textView = (TextView) view.findViewById(R.id.text);
        return view;
    }

    @Override
    public void onStart(int dragPosition, View refreshHead) {
        textView.setText("start Refresh");
    }

    @Override
    public void onDragging(float distance, View refreshHead) {
        textView.setText("dragging " + distance);
    }

    @Override
    public void onReadyToRelease(View refreshHead) {
        textView.setText("release to refresh");
    }

    @Override
    public void onRefreshing(View refreshHead) {
        textView.setText("refreshing");
    }
}
