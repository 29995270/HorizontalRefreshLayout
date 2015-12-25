# HorizontalRefreshLayout
====
a android layout widget for horizontal drag refresh action (viewpager or recyclerview or horizontal Scrollview)
-----
![](https://github.com/29995270/HorizontalRefreshLayout/blob/master/art.gif "viewpager")
<br>
![](https://github.com/29995270/HorizontalRefreshLayout/blob/master/art2.gif "recyclerview")

<br>
how to use
-----
```xml
    <com.wq.freeze.horizontalrefreshlayout.lib.HorizontalRefreshLayout
        android:id="@+id/refresh"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="@android:color/white">
        <android.support.v4.view.ViewPager
            android:id="@+id/vp"
            android:layout_width="match_parent"
            android:layout_height="300dp"/>
    </com.wq.freeze.horizontalrefreshlayout.lib.HorizontalRefreshLayout>
```

```java
      refreshLayout.setEnable(true);
//      refreshLayout.setLeftHeadView(R.layout.widget_refresh_header);   a simple view to show as refresh view
//      refreshLayout.setRightHeadView(R.layout.widget_refresh_header);


      refreshLayout.setRefreshHeader(new SimpleRefreshHeader(this), HorizontalRefreshLayout.START);
      refreshLayout.setRefreshHeader(new SimpleRefreshHeader(this), HorizontalRefreshLayout.END);

      refreshLayout.setRefreshCallback(this);
        
        
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
```
