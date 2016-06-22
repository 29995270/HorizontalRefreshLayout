package com.wq.freeze.horizontalrefreshlayout.lib;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by Administrator on 2015/11/28.
 */
public class HorizontalRefreshLayout extends FrameLayout {

    private View child1;
    private float startX;
    private float refreshStartX;
    private int dragState = -1;
    public static final int START = 0;
    public static final int END = 1;
    private int width;
    private int height;
    private int childWidth;
    private float leftHeadWidth = 200;
    private float rightHeadWidth = 200;

    private static final int REFRESH_STATE_IDLE = 0;
    private static final int REFRESH_STATE_START = 1;
    private static final int REFRESH_STATE_DRAGGING = 2;
    private static final int REFRESH_STATE_READYTORELEASE = 3;
    private static final int REFRESH_STATE_REFRESHING = 4;

    private int refreshState = REFRESH_STATE_IDLE;
    private boolean enable = true;
    private Context context;
    private View leftHead;
    private View rightHead;

    private RefreshHeader leftRefreshHeader;
    private RefreshHeader rightRefreshHeader;
    private RefreshCallBack refreshCallback;


    public HorizontalRefreshLayout(Context context) {
        super(context);
    }

    public HorizontalRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public HorizontalRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs) {
        this.context = context;
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (child1 == null && getChildCount() != 1) {
            throw new RuntimeException("can only have one child");
        }

        if (child1 == null) {
            child1 = getChildAt(0);
            child1.post(new Runnable() {
                @Override
                public void run() {
                    childWidth = child1.getWidth();
                }
            });
        }

        if (leftHead != null && leftHead.getParent() == null) {
            addView(leftHead, 0);
        }
        if (rightHead != null && rightHead.getParent() == null) {
            addView(rightHead, 0);
        }
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (child1 == null || !isEnable()) {
            return super.onInterceptTouchEvent(ev);
        }
        final int action = MotionEventCompat.getActionMasked(ev);

        if (child1 != null && (dragState == START || dragState == END)) {
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
                    if (rightRefreshHeader != null) rightRefreshHeader.onStart(END, rightHead);
                    return true;
                }
                if (!canChildScrollLeft() && startX != 0 && startX - ev.getX() < 0
                        && refreshState != REFRESH_STATE_REFRESHING && leftRefreshHeader != null) {
                    //start drag
                    dragState = START;
                    refreshState = REFRESH_STATE_START;
//                    Log.v("AAA", "START start dragging");
                    if (leftRefreshHeader != null) leftRefreshHeader.onStart(START, leftHead);
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

        if (child1 == null || !isEnable()) return super.onTouchEvent(event);

        switch (event.getAction()){
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
//                child1.setTranslationX(0);

                if (dragState == START) {
                    if (child1.getTranslationX() < (leftHeadWidth - dp2px(context, 16))) {
                        smoothRelease();
                    } else {
                        smoothLocateToRefresh();
                    }
                } else if (dragState == END) {
                    if ((-child1.getTranslationX()) < (rightHeadWidth - dp2px(context, 16))) {
                        smoothRelease();
                    } else {
                        smoothLocateToRefresh();
                    }
                }
                return false;
        }

        if (dragState == START) {

            if (refreshStartX == 0) {
                refreshStartX = event.getX();
                return super.onTouchEvent(event);
            } else {
                float dX = event.getX() - refreshStartX;
                refreshStartX = event.getX();
                if (child1.getTranslationX() < 0) {
                    child1.setTranslationX(0);
                    event.setAction(MotionEvent.ACTION_CANCEL);
                    dragState = -1;
                    refreshStartX = 0;
                    return false;
                }

                float dampingDX = dX * (1 - Math.abs((child1.getTranslationX() / leftHeadWidth)));  //let drag action has resistance

                if (child1.getTranslationX() + dampingDX < 0) {
                    child1.setTranslationX(0);
                } else if (child1.getTranslationX() + dampingDX > leftHeadWidth){
                    child1.setTranslationX(leftHeadWidth);
                } else {
                    child1.setTranslationX(child1.getTranslationX() + dampingDX);
//                    Log.v("AAA", child1.getTranslationX() + "");
                    if (leftRefreshHeader != null) {
                        refreshState = REFRESH_STATE_DRAGGING;
                        leftRefreshHeader.onDragging(child1.getTranslationX(), leftHead);
                        if (child1.getTranslationX() >= (leftHeadWidth - dp2px(context, 16)) && refreshState != REFRESH_STATE_READYTORELEASE) {
                            refreshState = REFRESH_STATE_READYTORELEASE;
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
                if (child1.getTranslationX() > 0) {
                    child1.setTranslationX(0);
                    event.setAction(MotionEvent.ACTION_CANCEL);
                    dragState = -1;
                    refreshStartX = 0;
                    return false;
                }

                float dampingDX = dX * (1 - Math.abs((child1.getTranslationX() / rightHeadWidth)));  //let drag action has resistance

                if (child1.getTranslationX() + dampingDX > 0) {
                    child1.setTranslationX(0);
                } else if (child1.getTranslationX() + dampingDX < -rightHeadWidth){
                    child1.setTranslationX(-rightHeadWidth);
                } else {
                    child1.setTranslationX(child1.getTranslationX() + dampingDX);
//                    Log.v("AAA", child1.getTranslationX() + "");
                    if (rightRefreshHeader != null) {
                        refreshState = REFRESH_STATE_DRAGGING;
                        rightRefreshHeader.onDragging( - child1.getTranslationX(), rightHead);
                        if ((-child1.getTranslationX()) >= (rightHeadWidth - dp2px(context, 16)) && refreshState != REFRESH_STATE_READYTORELEASE) {
                            refreshState = REFRESH_STATE_READYTORELEASE;
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
        ValueAnimator animator = ValueAnimator.ofFloat(child1.getTranslationX(), 0);
        animator.setDuration(200)
                .addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        child1.setTranslationX(((Float) animation.getAnimatedValue()));
                    }
                });
        animator.start();
    }

    private void smoothLocateToRefresh() {
        refreshState = REFRESH_STATE_REFRESHING;
        dragState = -1;
        refreshStartX = 0;
        float translationX = child1.getTranslationX();

        if (leftRefreshHeader != null && translationX > 0) {
            leftRefreshHeader.onRefreshing(leftHead);
            if (refreshCallback != null) refreshCallback.onLeftRefreshing();
        }
        if (rightRefreshHeader != null && translationX < 0) {
            rightRefreshHeader.onRefreshing(rightHead);
            if (refreshCallback != null) refreshCallback.onRightRefreshing();
        }

        ValueAnimator animator = ValueAnimator.ofFloat(translationX, translationX > 0 ? leftHeadWidth - dp2px(context, 16) : -(rightHeadWidth - dp2px(context, 16)));
        animator.setDuration(150)
                .addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        child1.setTranslationX(((Float) animation.getAnimatedValue()));
                    }
                });
        animator.start();
    }

    private void setRefreshView(final View view, final int startOrEnd) {
        post(new Runnable() {
            @Override
            public void run() {
                view.measure(width, height);
                if (startOrEnd == START) {
                    leftHeadWidth = view.getMeasuredWidth() + dp2px(context, 16);
                    ((LayoutParams) view.getLayoutParams()).gravity = Gravity.START;
                } else if (startOrEnd == END) {
                    rightHeadWidth = view.getMeasuredWidth() + dp2px(context, 16);
                    ((LayoutParams) view.getLayoutParams()).gravity = Gravity.END;
                }
                requestLayout();
            }
        });
    }


    /**
     * @return Whether it is possible for the child1 view of this layout to
     *         scroll up. Override this if the child1 view is a custom view.
     */
    public boolean canChildScrollLeft() {
        return ViewCompat.canScrollHorizontally(child1, -1);
    }

    public boolean canChildScrollRight() {
        return ViewCompat.canScrollHorizontally(child1, 1);
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
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

    public int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal, context.getResources().getDisplayMetrics());
    }
}
