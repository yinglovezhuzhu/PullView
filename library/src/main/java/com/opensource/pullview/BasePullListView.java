package com.opensource.pullview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ListView;

/**
 *
 * Created by yinglovezhuzhu@gmail.com
 */
public abstract class BasePullListView extends ListView implements IPullView, AbsListView.OnScrollListener {

    protected RotateAnimation mDownToUpAnimation;
    protected RotateAnimation mUpToDownAnimation;

    protected int mHeaderViewHeight;
    protected int mFooterViewHeight;

    protected int mFirstItemIndex;
    protected int mLastItemIndex;
    protected int mTotalItemCount;

    /** Whether it can refresh. */
    protected boolean mEnablePullRefresh = false;
    /** Whether it can load more data. */
    protected boolean mEnablePullLoad = false;
    /** Is refreshing data */
    protected boolean mRefreshing = false;
    /** Can be over scroll **/
    protected boolean mOverScrollable = true;

    protected LoadMode mLoadMode = LoadMode.AUTO_LOAD;

    protected int mStartY;
    protected int mState;
    protected boolean mRecording = false;
    protected boolean mIsBack = false;

    protected OnRefreshListener mRefreshListener;
    protected OnLoadMoreListener mLoadMoreListener;
    protected OnScrollListener mScrollListener;

    /**
     * Constructor
     *
     * @param context
     */
    public BasePullListView(Context context) {
        super(context);
        init();
    }

    /**
     * Constructor
     *
     * @param context
     */
    public BasePullListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Constructor
     *
     * @param context
     */
    public BasePullListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mFirstItemIndex = firstVisibleItem;
        mLastItemIndex = firstVisibleItem + visibleItemCount;
        mTotalItemCount = totalItemCount;
        if (null != mScrollListener) {
            mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE && mLastItemIndex == mTotalItemCount && mState == IDEL) {
            if (mEnablePullLoad && mLoadMode == LoadMode.AUTO_LOAD) {
                setSelection(mTotalItemCount);
                loadMore();
                mState = LOADING;
                updateFooterViewByState(0);
            }
        }
        if (null != mScrollListener) {
            mScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    @Override
    public void setOnScrollListener(OnScrollListener l) {
        this.mScrollListener = l;
    }

    /**
     * Do load more operation.
     */
    protected abstract void loadMore();

    /**
     * Do refresh operation.
     */
    protected abstract void refresh();

    /**
     * Update header view by state.
     * @param paddingTop
     */
    protected abstract void updateHeaderViewByState(int paddingTop);

    /**
     * Update footer view by state
     * @param paddingBottom
     */
    protected abstract void updateFooterViewByState(int paddingBottom);

    /**
     * Refresh data complete
     */
    public void refreshCompleted() {
        mState = IDEL;
        mRefreshing = false;
        mRecording = false;
    }

    /**
     * Load more complete
     */
    public void loadMoreCompleted(boolean loadMoreable) {
        mState = IDEL;
        this.mEnablePullLoad = loadMoreable;
    }

    /**
     * Sets listener to listen refresh action
     *
     * @param listener
     */
    public void setOnRefreshListener(OnRefreshListener listener) {
        this.mRefreshListener = listener;
        mEnablePullRefresh = null != listener;
    }

    /**
     * Sets listener to listen load more action
     *
     * @param listener
     */
    public void setOnLoadMoreListener(OnLoadMoreListener listener) {
        this.mLoadMoreListener = listener;
        mEnablePullLoad = null != listener;
    }

    /**
     * Sets the mode to load more data.<br>
     * <p>can use value is {@link LoadMode#AUTO_LOAD}
     * and {@link LoadMode#PULL_TO_LOAD}<br>
     * default is {@link LoadMode#AUTO_LOAD}
     *
     * @param mode
     * @see {@link com.opensource.pullview.IPullView.LoadMode}
     */
    public void setLoadMode(LoadMode mode) {
        this.mLoadMode = mode;
    }

    /**
     * Sets the pull listview can over scroll or not.
     * @param overScrollable
     */
    public void setOverScrollable(boolean overScrollable) {
        this.mOverScrollable = overScrollable;
    }

    /**
     * Gets it is refreshing
     * @return
     */
    public boolean isRefreshing() {
        return mRefreshing;
    }

    private void init() {
        mDownToUpAnimation = new RotateAnimation(0, -180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mDownToUpAnimation.setInterpolator(new LinearInterpolator());
        mDownToUpAnimation.setDuration(ROTATE_ANIMATION_DURATION);
        mDownToUpAnimation.setFillAfter(true);


        mUpToDownAnimation = new RotateAnimation(-180, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mUpToDownAnimation.setInterpolator(new LinearInterpolator());
        mUpToDownAnimation.setDuration(ROTATE_ANIMATION_DURATION);
        mUpToDownAnimation.setFillAfter(true);

        super.setOnScrollListener(this);
    }
}
