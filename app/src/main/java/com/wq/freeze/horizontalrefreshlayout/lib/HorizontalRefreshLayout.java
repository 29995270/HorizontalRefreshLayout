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
    private static final int START = 0;
    private static final int END = 1;
    private int width;
    private int height;
    private int childWidth;
    private float leftHeadWidth = 200;
    private float rightHeadWidth = 200;

    private boolean enable = true;
    private Context context;
    private View leftHead;
    private View rightHead;


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
                if (!canChildScrollRight() && startX != 0 && startX - ev.getX() > 0) {
                    //end drag
                    dragState = END;
                    return true;
                }
                if (!canChildScrollLeft() && startX != 0 && startX - ev.getX() < 0) {
                    //start drag
                    dragState = START;
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
                dragState = -1;
                refreshStartX = 0;
                smoothRelease();
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
                if (child1.getTranslationX() + dX < 0) {
                    child1.setTranslationX(0);
                } else if (child1.getTranslationX() + dX > leftHeadWidth){
                    child1.setTranslationX(leftHeadWidth);
                } else {
                    child1.setTranslationX(child1.getTranslationX() + dX);
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
                if (child1.getTranslationX() + dX > 0) {
                    child1.setTranslationX(0);
                } else if (child1.getTranslationX() + dX < -rightHeadWidth){
                    child1.setTranslationX(-rightHeadWidth);
                } else {
                    child1.setTranslationX(child1.getTranslationX() + dX);
                }
                return true;
            }
        }
        return super.onTouchEvent(event);
    }

    private void smoothRelease(){
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

    public void setLeftHeadLayout(@LayoutRes int id) {
        leftHead = LayoutInflater.from(context).inflate(id, this, false);
        post(new Runnable() {
            @Override
            public void run() {
                leftHead.measure(width, height);
                leftHeadWidth = leftHead.getMeasuredWidth();
                ((LayoutParams) leftHead.getLayoutParams()).gravity = Gravity.START;
                requestLayout();
            }
        });
    }

    public void setRightHeadLayout(@LayoutRes int id) {
        rightHead = LayoutInflater.from(context).inflate(id, this, false);
        post(new Runnable() {
            @Override
            public void run() {
                rightHead.measure(width, height);
                rightHeadWidth = rightHead.getMeasuredWidth();
                ((LayoutParams) rightHead.getLayoutParams()).gravity = Gravity.END;
                requestLayout();
            }
        });
    }
}
