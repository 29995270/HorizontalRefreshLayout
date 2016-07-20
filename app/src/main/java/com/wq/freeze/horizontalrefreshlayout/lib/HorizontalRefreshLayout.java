package com.wq.freeze.horizontalrefreshlayout.lib;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.MainThread;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by Administrator on 2015/11/28.
 * like SwipeRefreshLayout, but motion in horizontal direction
 */
public class HorizontalRefreshLayout extends FrameLayout {

    private View mTarget;
    private float startX;
    private float refreshStartX;
    private int dragState = -1;
    public static final int START = 0;
    public static final int END = 1;
    private int width;
    private int height;
    private float leftHeadWidth;
    private float rightHeadWidth;

    private static final int REFRESH_STATE_IDLE = 0;
    private static final int REFRESH_STATE_START = 1;
    private static final int REFRESH_STATE_DRAGGING = 2;
    private static final int REFRESH_STATE_READY_TO_RELEASE = 3;
    private static final int REFRESH_STATE_REFRESHING = 4;

    private int refreshState = REFRESH_STATE_IDLE;
    private boolean enable = true;
    private Context context;
    private View leftHead;
    private View rightHead;

    private RefreshHeader leftRefreshHeader;
    private RefreshHeader rightRefreshHeader;
    private RefreshCallBack refreshCallback;
    private int commonMarginPx;

    private int commonMargin = 16;
    private boolean refreshHeadFollowDrag = true;

    public HorizontalRefreshLayout(Context context) {
        super(context);
        init(context, null);
    }

    public HorizontalRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public HorizontalRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        this.context = context;
        commonMarginPx = dp2px(context, commonMargin);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view == leftHead) {
                leftHeadWidth = view.getMeasuredWidth() + commonMarginPx;
            } else if (view == rightHead) {
                rightHeadWidth = view.getMeasuredWidth() + commonMarginPx;
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        width = getMeasuredWidth();
        height = getMeasuredHeight();
        if (getChildCount() == 0) {
            return;
        }
        if (mTarget == null) {
            ensureTarget();
        }
        if (mTarget == null) {
            return;
        }
        super.onLayout(changed, l, t, r, b);
    }

    private void ensureTarget() {
        if (mTarget == null) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (!child.equals(leftHead) && !child.equals(rightHead)) {
                    mTarget = child;
                    break;
                }
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mTarget == null || !isEnable()) {
            return super.onInterceptTouchEvent(ev);
        }
        final int action = MotionEventCompat.getActionMasked(ev);

        if (mTarget != null && (dragState == START || dragState == END)) {
            return true;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                startX = ev.getX();
                break;

            case MotionEvent.ACTION_MOVE:
                if (!canChildScrollRight() && startX != 0 && startX - ev.getX() > 0
                        && refreshState != REFRESH_STATE_REFRESHING && rightRefreshHeader != null) {
                    //end drag
                    dragState = END;
                    refreshState = REFRESH_STATE_START;
//                    Log.v("AAA", "END start dragging");
                    rightRefreshHeader.onStart(END, rightHead);
                    return true;
                }
                if (!canChildScrollLeft() && startX != 0 && startX - ev.getX() < 0
                        && refreshState != REFRESH_STATE_REFRESHING && leftRefreshHeader != null) {
                    //start drag
                    dragState = START;
                    refreshState = REFRESH_STATE_START;
//                    Log.v("AAA", "START start dragging");
                    leftRefreshHeader.onStart(START, leftHead);
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                startX = 0;
                break;
        }

        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mTarget == null || !isEnable()) return super.onTouchEvent(event);

        switch (event.getAction()){
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
//                mTarget.setTranslationX(0);

                final float targetTranslationX = mTarget.getTranslationX();

                if (dragState == START) {
                    if (targetTranslationX < (leftHeadWidth - commonMarginPx)) {
                        smoothRelease();
                    } else {
                        smoothLocateToRefresh();
                    }
                } else if (dragState == END) {
                    if ((-targetTranslationX) < (rightHeadWidth - commonMarginPx)) {
                        smoothRelease();
                    } else {
                        smoothLocateToRefresh();
                    }
                }
                return false;
        }

        final float targetTranslationX = mTarget.getTranslationX();

        if (dragState == START) {

            if (refreshStartX == 0) {
                refreshStartX = event.getX();
                return super.onTouchEvent(event);
            } else {
                float dX = event.getX() - refreshStartX;
                refreshStartX = event.getX();
                if (targetTranslationX < 0) {
                    mTarget.setTranslationX(0);
                    event.setAction(MotionEvent.ACTION_CANCEL);
                    dragState = -1;
                    refreshStartX = 0;
                    return false;
                }

                float dampingDX = dX * (1 - Math.abs((targetTranslationX / leftHeadWidth)));  //let drag action has resistance

                if (targetTranslationX + dampingDX < 0) {
                    mTarget.setTranslationX(0);
                } else if (targetTranslationX + dampingDX > leftHeadWidth){
                    mTarget.setTranslationX(leftHeadWidth);
                } else {
                    mTarget.setTranslationX(targetTranslationX + dampingDX);
//                    Log.v("AAA", mTarget.getTranslationX() + "");
                    if (leftRefreshHeader != null) {
                        if (refreshHeadFollowDrag) {
                            leftHead.setTranslationX(targetTranslationX + dampingDX - (leftHeadWidth - commonMarginPx));
                        }

                        refreshState = REFRESH_STATE_DRAGGING;
                        leftRefreshHeader.onDragging(mTarget.getTranslationX(), Math.abs(mTarget.getTranslationX()/(leftHeadWidth - commonMarginPx)), leftHead);
                        if (mTarget.getTranslationX() > (leftHeadWidth - commonMarginPx) && refreshState != REFRESH_STATE_READY_TO_RELEASE) {
                            refreshState = REFRESH_STATE_READY_TO_RELEASE;
                            leftRefreshHeader.onReadyToRelease(leftHead);
                        }
                    }
                }
                return true;
            }
        } else if (dragState == END) {
            if (refreshStartX == 0) {
                refreshStartX = event.getX();
                return super.onTouchEvent(event);
            } else {
                float dX = event.getX() - refreshStartX;
                refreshStartX = event.getX();
                if (targetTranslationX > 0) {
                    mTarget.setTranslationX(0);
                    event.setAction(MotionEvent.ACTION_CANCEL);
                    dragState = -1;
                    refreshStartX = 0;
                    return false;
                }

                float dampingDX = dX * (1 - Math.abs((targetTranslationX / rightHeadWidth)));  //let drag action has resistance

                if (targetTranslationX + dampingDX > 0) {
                    mTarget.setTranslationX(0);
                } else if (targetTranslationX + dampingDX < -rightHeadWidth){
                    mTarget.setTranslationX(-rightHeadWidth);
                } else {
                    mTarget.setTranslationX(targetTranslationX + dampingDX);

//                    Log.v("AAA", mTarget.getTranslationX() + "");
                    if (rightRefreshHeader != null) {
                        if (refreshHeadFollowDrag) {
                            rightHead.setTranslationX(targetTranslationX + dampingDX + (rightHeadWidth - commonMarginPx));
                        }

                        refreshState = REFRESH_STATE_DRAGGING;
                        rightRefreshHeader.onDragging(-mTarget.getTranslationX(), Math.abs(mTarget.getTranslationX()/(rightHeadWidth - commonMarginPx)), rightHead);
                        if ((-mTarget.getTranslationX()) > (rightHeadWidth - commonMarginPx) && refreshState != REFRESH_STATE_READY_TO_RELEASE) {
                            refreshState = REFRESH_STATE_READY_TO_RELEASE;
                            rightRefreshHeader.onReadyToRelease(rightHead);
                        }
                    }
                }
                return true;
            }
        }
        return super.onTouchEvent(event);
    }

    private void smoothRelease(){
        dragState = -1;
        refreshStartX = 0;
        mTarget.animate().translationX(0).setDuration(200).start();
        if (leftRefreshHeader != null) {
            if (refreshHeadFollowDrag) leftHead.animate().translationX(-leftHeadWidth).setDuration(200).start();
        }
        if (rightRefreshHeader != null) {
            if (refreshHeadFollowDrag) rightHead.animate().translationX(rightHeadWidth).setDuration(200).start();
        }
    }

    private void smoothLocateToRefresh() {
        refreshState = REFRESH_STATE_REFRESHING;
        dragState = -1;
        refreshStartX = 0;
        float translationX = mTarget.getTranslationX();

        if (leftRefreshHeader != null && translationX > 0) {
            leftRefreshHeader.onRefreshing(leftHead);
            if (refreshHeadFollowDrag) leftHead.animate().translationX(0).setDuration(150).start();
            if (refreshCallback != null) refreshCallback.onLeftRefreshing();
        }
        if (rightRefreshHeader != null && translationX < 0) {
            rightRefreshHeader.onRefreshing(rightHead);
            if (refreshHeadFollowDrag) rightHead.animate().translationX(0).setDuration(150).start();
            if (refreshCallback != null) refreshCallback.onRightRefreshing();
        }

        float dX = (translationX > 0 ? leftHeadWidth - commonMarginPx : -(rightHeadWidth - commonMarginPx)) - translationX;
        mTarget.animate().translationXBy(dX).setDuration(150).start();
    }

    @MainThread
    private void setRefreshView(final View view, final int startOrEnd) {

        view.measure(width, height);
        if (startOrEnd == START) {
            ((LayoutParams) view.getLayoutParams()).gravity = Gravity.START;
        } else if (startOrEnd == END) {
            ((LayoutParams) view.getLayoutParams()).gravity = Gravity.END;
        }

        if (view.getParent() == null) {
            addView(view, 0);
        }
    }


    /**
     * @return Whether it is possible for the mTarget view of this layout to
     *         scroll up. Override this if the mTarget view is a custom view.
     */
    public boolean canChildScrollLeft() {
        return ViewCompat.canScrollHorizontally(mTarget, -1);
    }

    public boolean canChildScrollRight() {
        return ViewCompat.canScrollHorizontally(mTarget, 1);
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public int getCommonMargin() {
        return commonMargin;
    }

    public void setCommonMargin(int commonMargin) {
        if (commonMargin < 0) return;
        this.commonMargin = commonMargin;
    }

    public void setRefreshHeadFollowDrag(boolean refreshHeadFollowDrag) {
        this.refreshHeadFollowDrag = refreshHeadFollowDrag;
    }

    public void onRefreshComplete(){
        refreshState = REFRESH_STATE_IDLE;
        smoothRelease();
        if (leftRefreshHeader != null) leftRefreshHeader.onStart(START, leftHead); //reset view style
        if (rightRefreshHeader != null) rightRefreshHeader.onStart(END, rightHead); //reset view style
    }

    public void setLeftHeadView(@LayoutRes int id) {
        leftHead = LayoutInflater.from(context).inflate(id, this, false);
        setRefreshView(leftHead, START);
    }

    public void setLeftHeadView(View view) {
        leftHead = view;
        setRefreshView(leftHead, START);
    }

    public void setRightHeadView(@LayoutRes int id) {
        rightHead = LayoutInflater.from(context).inflate(id, this, false);
        setRefreshView(rightHead, END);
    }

    public void setRightHeadView(View view) {
        rightHead = view;
        setRefreshView(rightHead, END);
    }

    public void setRefreshHeader(RefreshHeader header, int startOrEnd) {
        if (startOrEnd == START) {
            leftRefreshHeader = header;
            setLeftHeadView(leftRefreshHeader.getView(this));
        } else if (startOrEnd == END) {
            rightRefreshHeader = header;
            setRightHeadView(rightRefreshHeader.getView(this));
        }
    }

    public void setRefreshCallback(RefreshCallBack callback) {
        refreshCallback = callback;
    }

    public static int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal, context.getResources().getDisplayMetrics());
    }
}
