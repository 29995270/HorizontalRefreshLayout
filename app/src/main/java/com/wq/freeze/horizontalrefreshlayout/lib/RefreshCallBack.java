package com.wq.freeze.horizontalrefreshlayout.lib;

import android.view.View;

/**
 * Created by wangqi on 2015/12/24.
 */
public interface RefreshCallBack {
    /**
     * @param dragPosition  HorizontalRefreshLayout.START or HorizontalRefreshLayout.END
     */
    void onStart(int dragPosition, View refreshHead);

    void onRefreshing(View refreshHead);
}
