package com.wq.freeze.horizontalrefreshlayout;

import android.content.Context;
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

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private HorizontalRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewPager vp = (ViewPager) findViewById(R.id.vp);
        refreshLayout = (HorizontalRefreshLayout) findViewById(R.id.refresh);

        refreshLayout.setEnable(true);
        refreshLayout.setLeftHeadView(R.layout.widget_refresh_header);
        refreshLayout.setRightHeadView(R.layout.widget_refresh_header);

        Adapter adapter = new Adapter(this);
        vp.setAdapter(adapter);
    }

    static class Adapter extends PagerAdapter{

        private Random random;
        private Context context;

        public Adapter(Context context){
            this.context = context;
            this.random = new Random();
        }

        @Override
        public int getCount() {
            return 5;
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
