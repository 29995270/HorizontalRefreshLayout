package com.wq.freeze.horizontalrefreshlayout.lib;

import android.view.View;

/**
 * Created by wangqi on 2015/12/24.
 */
public interface RefreshHeader extends RefreshCallBack{

    /**
     * @param distance
     */
    void onDragging(float distance, View refreshHead);

    void onReadyToRelease(View refreshHead);

    View getView();
}
