package com.wq.freeze.horizontalrefreshlayout;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.freeze.horizontalrefreshlayout.lib.HorizontalRefreshLayout;
import com.freeze.horizontalrefreshlayout.lib.RefreshCallBack;
import com.freeze.horizontalrefreshlayout.lib.refreshhead.SimpleRefreshHeader;

/**
 * Created by wangqi on 2015/12/24.
 */
public class ScrollViewActivity extends AppCompatActivity implements RefreshCallBack {

    private LinearLayout linearLayout;
    private HorizontalRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrollview);
        setTitle("ScrollView");
        refreshLayout = (HorizontalRefreshLayout) findViewById(R.id.refresh);
        linearLayout = (LinearLayout) findViewById(R.id.container);

        refreshLayout.setEnable(true);

        refreshLayout.setRefreshHeader(new SimpleRefreshHeader(this), HorizontalRefreshLayout.START);
        refreshLayout.setRefreshHeader(new SimpleRefreshHeader(this), HorizontalRefreshLayout.END);

        refreshLayout.setRefreshCallback(this);
    }

    @Override
    public void onLeftRefreshing() {
        linearLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshLayout.onRefreshComplete();
                TextView textView = new TextView(ScrollViewActivity.this);
                textView.setLayoutParams(new LinearLayout.LayoutParams(200, LinearLayout.LayoutParams.MATCH_PARENT));
                textView.setGravity(Gravity.CENTER);
                textView.setText("new refresh data");
                textView.setTextColor(Color.BLACK);
                textView.setBackgroundColor(Color.GREEN);
                linearLayout.addView(textView, 0);
            }
        }, 1000);
    }

    @Override
    public void onRightRefreshing() {
        linearLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshLayout.onRefreshComplete();
                TextView textView = new TextView(ScrollViewActivity.this);
                textView.setLayoutParams(new LinearLayout.LayoutParams(200, LinearLayout.LayoutParams.MATCH_PARENT));
                textView.setText("new load more data");
                textView.setGravity(Gravity.CENTER);
                textView.setTextColor(Color.BLACK);
                textView.setBackgroundColor(Color.GREEN);
                linearLayout.addView(textView);
            }
        }, 1000);
    }
}
