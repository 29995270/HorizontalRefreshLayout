package com.wq.freeze.horizontalrefreshlayout;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wq.freeze.horizontalrefreshlayout.lib.HorizontalRefreshLayout;
import com.wq.freeze.horizontalrefreshlayout.lib.RefreshCallBack;
import com.wq.freeze.horizontalrefreshlayout.refreshhead.SimpleRefreshHeader;

import java.util.Random;

/**
 * Created by wangqi on 2015/12/24.
 */
public class RecyclerViewActivity extends AppCompatActivity implements RefreshCallBack {

    private Adapter adapter;
    private HorizontalRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler);
        setTitle("RecyclerView");
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        refreshLayout = (HorizontalRefreshLayout) findViewById(R.id.refresh);

        refreshLayout.setEnable(true);
//        refreshLayout.setLeftHeadView(R.layout.widget_refresh_header);   a simple view to show as refresh view
//        refreshLayout.setRightHeadView(R.layout.widget_refresh_header);

        refreshLayout.setRefreshMode(HorizontalRefreshLayout.MODE_UNDER_FOLLOW_DRAG);
        refreshLayout.setRefreshHeader(new SimpleRefreshHeader(this), HorizontalRefreshLayout.START);
        refreshLayout.setRefreshHeader(new SimpleRefreshHeader(this), HorizontalRefreshLayout.END);

        refreshLayout.setRefreshCallback(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapter = new Adapter(this, 5);
        recyclerView.setAdapter(adapter);
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

    static class Adapter extends RecyclerView.Adapter<Holder>{

        private int count;
        private Random random;
        private Context context;

        public Adapter(Context context, int count){
            this.context = context;
            this.count = count;
            this.random = new Random();
        }

        public void onRefresh() {
            notifyItemRangeChanged(0, count);
        }

        public void onLoadMore() {
            count += 5;
            notifyItemRangeInserted(count - 5, 5);
        }

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView view = new TextView(parent.getContext());
            view.setLayoutParams(new ViewGroup.LayoutParams(150, ViewGroup.LayoutParams.MATCH_PARENT));

            view.setGravity(Gravity.CENTER);
//
            view.setTextSize(20);
            view.setTextColor(Color.WHITE);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(Holder holder, int position) {
            int rgb = Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
            ((TextView) holder.itemView).setBackgroundColor(rgb);
            ((TextView) holder.itemView).setText(position + "");
        }

        @Override
        public int getItemCount() {
            return count;
        }
    }

    static class Holder extends RecyclerView.ViewHolder{

        public Holder(View itemView) {
            super(itemView);
        }
    }
}
