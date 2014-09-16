package com.opensource.pullview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.LinearLayout;
import android.widget.ScrollView;

/**
 *
 * Created by xiaoying on 14-9-15.
 */
public abstract class BasePullScrollView extends ScrollView implements IPullView {

    /** The rotate up anim. */
    protected Animation mDownToUpAnimation;
    /** The rotate down anim. */
    protected Animation mUpToDownAnimation;

    /** The scroll layout. */
    protected LinearLayout mScrollLayout;

    /** The content layout */
    protected LinearLayout mContentLayout;

    /** The header view height. */
    protected int mHeaderViewHeight;

    protected int mTopScroll;

    /** Enable pull refresh. */
    protected boolean mEnablePullRefresh = false;

    /** Pull refreshing. */
    protected boolean mRefreshing = false;

    /** The listener on refresh data. */
    protected OnRefreshListener mOnRefreshListener = null;

    /** The state of the PullView. */
    protected int mState = IDEL;

    /**
     * Constructor
     *
     * @param context the context
     */
    public BasePullScrollView(Context context) {
        super(context);
        init(context);
    }

    /**
     * Constructor
     *
     * @param context the context
     * @param attrs the attrs
     */
    public BasePullScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
    }

    @Override
    public void addView(View child) {
        if(getChildCount() > 0) {
            mContentLayout.addView(child);
        } else {
            super.addView(child);
        }
    }

    @Override
    public void addView(View child, int index) {
        if(getChildCount() > 0) {
            mContentLayout.addView(child, index);
        } else {
            super.addView(child, index);
        }
    }

    @Override
    public void addView(View child, int width, int height) {
        if(getChildCount() > 0) {
            mContentLayout.addView(child, width, height);
        } else {
            super.addView(child, width, height);
        }
    }

    @Override
    public void addView(View child, android.view.ViewGroup.LayoutParams params) {
        if(getChildCount() > 0) {
            mContentLayout.addView(child, params);
        } else {
            super.addView(child, params);
        }
    }

    @Override
    public void addView(View child, int index,
                        android.view.ViewGroup.LayoutParams params) {
        if(getChildCount() > 0) {
            mContentLayout.addView(child, index, params);
        } else {
            super.addView(child, index, params);
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        mTopScroll = t;
    }

    /**
     * Update header view by state
     */
    protected abstract void updateHeaderViewByState(int paddingTop);

    /**
     * Refresh operation
     */
    protected abstract void refresh();

    /**
     * Refresh complete.
     */
    public void refreshComplete() {
        mState = IDEL;
        mRefreshing = false;
    }

    /**
     * Set Refresh Listener.
     *
     * @param listener
     */
    public void setOnRefreshListener(OnRefreshListener listener) {
        mOnRefreshListener = listener;
        mEnablePullRefresh = null != listener;
    }

    /**
     * Gets If it is refreshing
     * @return
     */
    public boolean isRefreshing() {
        return mRefreshing;
    }

    /**
     * Add header view to scroll view.
     * @param headerView
     */
    protected void addHeaderView(View headerView) {
        if(null == mContentLayout || null == headerView) {
            return;
        }
        LinearLayout.LayoutParams headerLp = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        mScrollLayout.addView(headerView, 0, headerLp);
    }

    /**
     * Init the View.
     *
     * @param context the context
     */
    private void init(Context context) {

        mDownToUpAnimation = new RotateAnimation(0f, -180f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mDownToUpAnimation.setInterpolator(new LinearInterpolator());
        mDownToUpAnimation.setDuration(ROTATE_ANIMATION_DURATION);
        mDownToUpAnimation.setFillAfter(true);


        mUpToDownAnimation = new RotateAnimation(-180f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mUpToDownAnimation.setInterpolator(new LinearInterpolator());
        mUpToDownAnimation.setDuration(ROTATE_ANIMATION_DURATION);
        mUpToDownAnimation.setFillAfter(true);


        mScrollLayout = new LinearLayout(context);
        mScrollLayout.setOrientation(LinearLayout.VERTICAL);

        //添加放View的容器
        mContentLayout = new LinearLayout(context);
        mContentLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams contentLp = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        contentLp.weight = 1;
        mScrollLayout.addView(mContentLayout, contentLp);

        LayoutParams scrollLp = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT, Gravity.TOP);
        this.addView(mScrollLayout, scrollLp);
    }
}
