package com.wq.freeze.horizontalrefreshlayout;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.wq.freeze.horizontalrefreshlayout.lib.HorizontalRefreshLayout;
import com.wq.freeze.horizontalrefreshlayout.lib.RefreshCallBack;
import com.wq.freeze.horizontalrefreshlayout.refreshhead.MaterialRefreshHeader;
import com.wq.freeze.horizontalrefreshlayout.refreshhead.SimpleRefreshHeader;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements RefreshCallBack {

    private HorizontalRefreshLayout refreshLayout;
    private Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("ViewPager");
        ViewPager vp = (ViewPager) findViewById(R.id.vp);
        refreshLayout = (HorizontalRefreshLayout) findViewById(R.id.refresh);

        refreshLayout.setEnable(true);
//        refreshLayout.setLeftHeadView(R.layout.widget_refresh_header);   a simple view to show as refresh view
//        refreshLayout.setRightHeadView(R.layout.widget_refresh_header);

        refreshLayout.setRefreshMode(HorizontalRefreshLayout.MODE_ABOVE);
        refreshLayout.setRefreshHeader(new MaterialRefreshHeader(HorizontalRefreshLayout.START),
                HorizontalRefreshLayout.START);
        refreshLayout.setRefreshHeader(new MaterialRefreshHeader(HorizontalRefreshLayout.END),
                HorizontalRefreshLayout.END);

        refreshLayout.setRefreshCallback(this);

        adapter = new Adapter(this, 5);
        vp.setAdapter(adapter);

        refreshLayout.startAutoRefresh(true);
    }

    public void gotoRecycler(View v) {
        startActivity(new Intent(this, RecyclerViewActivity.class));
    }

    public void gotoScrollView(View v) {
        startActivity(new Intent(this, ScrollViewActivity.class));
    }

    @Override
    public void onLeftRefreshing() {
        refreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter.onRefresh();
                refreshLayout.onRefreshComplete();
            }
        }, 1000);
    }

    @Override
    public void onRightRefreshing() {
        refreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter.onLoadMore();
                refreshLayout.onRefreshComplete();
            }
        }, 1000);
    }

    static class Adapter extends PagerAdapter{

        private int count;
        private Random random;
        private Context context;

        public Adapter(Context context, int count){
            this.context = context;
            this.count = count;
            this.random = new Random();
        }

        public void onRefresh() {
            notifyDataSetChanged();
        }

        public void onLoadMore() {
            count += 5;
            notifyDataSetChanged();
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return count;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            TextView view = new TextView(context);
            view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            int rgb = Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
            view.setBackgroundColor(rgb);
            view.setGravity(Gravity.CENTER);
            view.setText("position  " + position);
            view.setTextSize(20);
            view.setTextColor(Color.WHITE);
            container.addView(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "click", Toast.LENGTH_SHORT).show();
                }
            });
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
