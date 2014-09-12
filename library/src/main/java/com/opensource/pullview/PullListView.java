/*
 * Copyright (C) 2014  The Android Open Source Project.
 *
 *		yinglovezhuzhu@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.opensource.pullview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.opensource.pullview.utils.DateUtil;

/**
 * Usage A Custom ListView can be pull to refresh and load more<br>
 * <p>Off by default pull-to-refresh and load-more, but turn them on when<br>
 * call {@link #setOnRefreshListener(com.opensource.pullview.OnRefreshListener)} and {@link #setOnLoadMoreListener(OnLoadMoreListener)}<br><br>
 * <p/>
 * <p>Pull-to-refresh and load-more can not doing at the same time.<br>
 * If pull-to-refresh is happening, you can't do load-more action befor pull-to refresh is finished.<br><br>
 * <p/>
 * <p>You need to call {@link #refreshCompleted()} when refresh thread finished,<br>
 * Similarly, You also need to call {@link #loadMoreCompleted(boolean)} when load thread finished.<br>
 *
 * @author yinglovezhuzhu@gmail.com
 */
public class PullListView extends BasePullListView {


    private PullHeaderView mHeaderView;
    private PullFooterView mFooterView;

    private String mLastRefreshTime = "";
    private int mHeaderLebelVisiblity = View.VISIBLE;

    /**
     * Constructor
     *
     * @param context
     */
    public PullListView(Context context) {
        super(context);
        initView(context);
    }

    /**
     * Constructor
     *
     * @param context
     */
    public PullListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    /**
     * Constructor
     *
     * @param context
     */
    public PullListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartY = (int) event.getY();
                if (!mRecording) {
                    mRecording = mFirstItemIndex == 0 || mLastItemIndex == mTotalItemCount;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                int tempY = (int) event.getY();
                if (mFirstItemIndex == 0) {
                    if (!mRecording) {
                        mRecording = true;
                        mStartY = tempY;
                    }
                    int moveY = tempY - mStartY;
                    int scrollY = moveY / OFFSET_RATIO;

                    if (mState != LOADING) {
                        // Ensure that the process of setting padding, current position has always been at the header,
                        // or if when the list exceeds the screen, then, when the push, the list will scroll at the same time
                        switch (mState) {
                            case RELEASE_TO_LOAD: // Release to load data
                                setSelection(mFirstItemIndex);
                                // Slide up, header part was covered, but not all be covered(Pull up to cancel)
                                if (moveY > 0 && (scrollY < mHeaderViewHeight)) {
                                    mState = PULL_TO_LOAD;
                                } else if (moveY <= 0) {
                                    // Slide to the top
                                    mState = IDEL;
                                }
                                updateHeaderViewByState(scrollY - mHeaderViewHeight);
                                break;
                            case PULL_TO_LOAD:
                                setSelection(mFirstItemIndex);
                                // Pull down to the state can enter RELEASE_TO_REFRESH
                                if (moveY <= 0) {
                                    mState = IDEL;
                                } else if (scrollY >= mHeaderViewHeight) {
                                    mState = RELEASE_TO_LOAD;
                                    mIsBack = true;
                                }
                                updateHeaderViewByState(scrollY - mHeaderViewHeight);
                                break;
                            case IDEL:
                                if (moveY > 0) {
                                    mState = PULL_TO_LOAD;
                                }
                                updateHeaderViewByState(-mHeaderViewHeight);
                                break;
                            default:
                                break;
                        }
                    }
                } else if (mLastItemIndex == mTotalItemCount) {
                    if (!mRecording) {
                        mRecording = true;
                        mStartY = tempY;
                    }
                    int moveY = mStartY - tempY;
                    int scrollY = moveY / OFFSET_RATIO;
                    if (mState != LOADING
                            && (mLoadMode == LoadMode.PULL_TO_LOAD && (mLoadMoreable || mOverScrollable)
                            || mLoadMode == LoadMode.AUTO_LOAD && !mLoadMoreable && mOverScrollable)) {
                        //可以向上pull的条件是
                        //1.mState != LOADING，即非LOADING状态下
                        //2.mLoadMode == LoadMode.PULL_TO_LOAD时有更多数据可加载或者可以过度滑动（OverScroll）
                        // 或者mLoadMode == LoadMode.AUTO_LOAD时没有更多数据可加载但可以过度滑动（OverScroll）

                        // Ensure that the process of setting padding, current position has always been at the footer,
                        // or if when the list exceeds the screen, then, when the push up, the list will scroll at the same time
                        switch (mState) {
                            case RELEASE_TO_LOAD: // release-to-load
                                setSelection(mTotalItemCount);
                                // Slide down, header part was covered, but not all be covered(Pull down to cancel)
                                if (moveY > 0 && scrollY <= mFooterViewHeight) {
                                    mState = PULL_TO_LOAD;
                                } else if (moveY <= 0) { //Slide up(Pull up to make footer to show)
                                    mState = IDEL;
                                }
                                updateFooterViewByState(scrollY - mFooterViewHeight);
                                break;
                            case PULL_TO_LOAD:
                                setSelection(mTotalItemCount);
                                // Pull up to the state can enter RELEASE_TO_REFRESH
                                if (scrollY > mFooterViewHeight) {
                                    mState = RELEASE_TO_LOAD;
                                    mIsBack = true;
                                } else if (moveY <= 0) {
                                    mState = IDEL;
                                }
                                updateFooterViewByState(scrollY - mFooterViewHeight);
                                break;
                            case IDEL:
                                if (moveY > 0) {
                                    mState = PULL_TO_LOAD;
                                }
                                updateFooterViewByState(-mFooterViewHeight);
                                break;
                            default:
                                break;
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mFirstItemIndex == 0) {
                    switch (mState) {
                        case IDEL:
                            //Do nothing.
                            break;
                        case PULL_TO_LOAD:
                            //Pull to refresh.
                            mState = IDEL;
                            updateHeaderViewByState(-mHeaderViewHeight);
                            break;
                        case RELEASE_TO_LOAD:
                            if (mRefreshable) {
                                //Release to refresh.
                                refresh();
                                mState = LOADING;
                                updateHeaderViewByState(0);
                            } else {
                                mState = IDEL;
                                updateHeaderViewByState(-mHeaderViewHeight);
                            }
                            break;
                        default:
                            updateHeaderViewByState(-mHeaderViewHeight);
                            break;
                    }
                } else if (mLastItemIndex == mTotalItemCount) {
                    switch (mState) {
                        case IDEL:
                            //Do nothing.
                            break;
                        case PULL_TO_LOAD:
                            //Pull to load more data.
                            mState = IDEL;
                            updateFooterViewByState(-mFooterViewHeight);
                            break;
                        case RELEASE_TO_LOAD:
                            if (mLoadMoreable) {
                                //Release to load more data.
                                loadMore();
                                mState = LOADING;
                                updateFooterViewByState(0);
                            } else {
                                mState = IDEL;
                                updateFooterViewByState(-mFooterViewHeight);
                            }
                            break;
                        default:
                            break;
                    }
                }
                mRecording = false;
                mIsBack = false;
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void updateHeaderViewByState(int paddingTop) {
        switch (mState) {
            case RELEASE_TO_LOAD:
                mHeaderView.setArrowVisibility(View.VISIBLE);
                mHeaderView.setProgressVisibility(View.GONE);
                mHeaderView.startArrowAnimation(mDownToUpAnimation);
                mHeaderView.setTitleText(R.string.pull_view_release_to_refresh);
                mHeaderView.setLabelText(getResources().getString(R.string.pull_view_refresh_time)
                        + mLastRefreshTime);
                break;
            case PULL_TO_LOAD:
                mHeaderView.setArrowVisibility(View.VISIBLE);
                mHeaderView.setProgressVisibility(View.GONE);

                if (mIsBack) {
                    mIsBack = false;
                    mHeaderView.startArrowAnimation(mUpToDownAnimation);
                }
                mHeaderView.setTitleText(R.string.pull_view_pull_to_refresh);
                mHeaderView.setLabelText(getResources().getString(
                        R.string.pull_view_refresh_time)
                        + mLastRefreshTime);
                break;
            case LOADING:
                mHeaderView.setArrowVisibility(View.GONE);
                mHeaderView.setProgressVisibility(View.VISIBLE);
                mHeaderView.startArrowAnimation(null);
                mHeaderView.setTitleText(R.string.pull_view_refreshing);
                mHeaderView.setLabelText(getResources().getString(R.string.pull_view_refresh_time)
                        + mLastRefreshTime);
                break;
            case IDEL:
                mHeaderView.setProgressVisibility(View.GONE);
                mHeaderView.startArrowAnimation(null);
                mHeaderView.setTitleText(R.string.pull_view_pull_to_refresh);
                mHeaderView.setLabelText(getResources().getString(R.string.pull_view_refresh_time)
                        + mLastRefreshTime);
                break;
            default:
                break;
        }
        mHeaderView.setVisibility(mRefreshable ? View.VISIBLE : View.INVISIBLE);
        mHeaderView.setLabelVisibility(mHeaderLebelVisiblity);
        mHeaderView.setPadding(0, paddingTop, 0, 0);
    }

    @Override
    protected void updateFooterViewByState(int paddingBottom) {
        switch (mState) {
            case RELEASE_TO_LOAD:
                mFooterView.setArrowVisibility(View.VISIBLE);
                mFooterView.setProgressVisibility(View.GONE);
                mFooterView.startArrowAnimation(mDownToUpAnimation);
                mFooterView.setTitleText(R.string.pull_view_release_to_load);
                break;
            case PULL_TO_LOAD:
                mFooterView.setArrowVisibility(View.VISIBLE);
                mFooterView.setProgressVisibility(View.GONE);

                if (mIsBack) {
                    mIsBack = false;
                    mFooterView.startArrowAnimation(mUpToDownAnimation);
                }
                mFooterView.setTitleText(R.string.pull_view_pull_to_load);
                break;
            case LOADING:
                mFooterView.setArrowVisibility(View.GONE);
                mFooterView.setProgressVisibility(View.VISIBLE);
                mFooterView.startArrowAnimation(null);
                mFooterView.setTitleText(R.string.pull_view_loading);
                break;
            case IDEL:
                mFooterView.setProgressVisibility(View.GONE);
                mFooterView.startArrowAnimation(null);
                mFooterView.setTitleText(R.string.pull_view_pull_to_load);
                break;
            default:
                break;
        }
        mFooterView.setVisibility(mLoadMoreable ? View.VISIBLE : View.INVISIBLE);
        mFooterView.setPadding(0, 0, 0, paddingBottom);
    }

    @Override
    protected void loadMore() {
        if (mLoadMoreListener != null) {
            mLoadMoreListener.onLoadMore();
        }
    }

    @Override
    protected void refresh() {
        if (mRefreshListener != null) {
            mRefreshListener.onRefresh();
        }
    }

    @Override
    public void refreshCompleted() {
        super.refreshCompleted();
        mLastRefreshTime = DateUtil.getSystemDate(getResources().getString(R.string.pull_view_date_format));
        updateHeaderViewByState(-mHeaderViewHeight);
    }

    @Override
    public void loadMoreCompleted(boolean loadMoreable) {
        super.loadMoreCompleted(null != mLoadMoreListener && loadMoreable);
        updateFooterViewByState(-mFooterViewHeight);
    }


    /**
     * Show loading view on header<br>
     * <br><p>Use this method when no header view was added on PullListView.
     *
     * @param text
     */
    public void onHeadLoading(CharSequence text) {
        mState = LOADING;
        mHeaderView.setPadding(0, 0, 0, 0);
        mHeaderView.setArrowVisibility(View.GONE);
        mHeaderView.setProgressVisibility(View.VISIBLE);
        mHeaderView.setTitileVisibility(View.VISIBLE);
        mHeaderView.setLabelVisibility(View.GONE);
        mHeaderView.startArrowAnimation(null);
        mHeaderView.setTitleText(text);
    }

    /**
     * Show loading view on head<br>
     * <br><p>Use this method when no header view was added on PullListView.
     *
     * @param resId
     */
    public void onHeadLoading(int resId) {
        mState = LOADING;
        mHeaderView.setPadding(0, 0, 0, 0);
        mHeaderView.setArrowVisibility(View.GONE);
        mHeaderView.setProgressVisibility(View.VISIBLE);
        mHeaderView.setTitileVisibility(View.VISIBLE);
        mHeaderView.setLabelVisibility(View.GONE);
        mHeaderView.startArrowAnimation(null);
        mHeaderView.setTitleText(resId);
    }

    /**
     * Show loading view on foot<br>
     * <br><p>Use this method when header view was added on PullListView.
     *
     * @param text
     */
    public void onFootLoading(CharSequence text) {
        mState = LOADING;
        mFooterView.setPadding(0, 0, 0, 0);
        mFooterView.setArrowVisibility(View.GONE);
        mFooterView.setProgressVisibility(View.VISIBLE);
        mFooterView.setTitileVisibility(View.VISIBLE);
        mFooterView.startArrowAnimation(null);
        mFooterView.setTitleText(text);
    }

    /**
     * Show loading view on foot<br>
     * <br><p>Use this method when header view was added on PullListView.
     *
     * @param resId
     */
    public void onFootLoading(int resId) {
        mState = LOADING;
        mFooterView.setPadding(0, 0, 0, 0);
        mFooterView.setArrowVisibility(View.GONE);
        mFooterView.setProgressVisibility(View.VISIBLE);
        mFooterView.setTitileVisibility(View.VISIBLE);
        mFooterView.startArrowAnimation(null);
        mFooterView.setTitleText(resId);
    }

    /**
     * Set last refresh time<br>
     * <p>The value of {@link #mLastRefreshTime} initialized to the time when create {@link com.opensource.pullview.PullListView} object.<br>
     * You can set this value.
     *
     * @param time
     */
    public void setLastRefreshTime(String time) {
        this.mLastRefreshTime = time;
    }

    /**
     * Set header view label's visibility.<br>
     * <p>You can set the value of {@link android.view.View#GONE}、{@link android.view.View#VISIBLE}<br>
     *
     * @param visibility
     * @see android.view.View#GONE
     * @see android.view.View#VISIBLE
     */
    public void setHeaderLabelVisibility(int visibility) {
        this.mHeaderLebelVisiblity = visibility;
        if (mHeaderLebelVisiblity == View.INVISIBLE) {
            mHeaderLebelVisiblity = View.GONE;
        }
        mHeaderView.setLabelVisibility(mHeaderLebelVisiblity);
    }

    /**
     * Init views
     *
     * @param context
     */
    private void initView(Context context) {

        mHeaderView = new PullHeaderView(context);
        mHeaderView.setLabelVisibility(View.VISIBLE);
        mHeaderViewHeight = mHeaderView.getViewHeight();
//		mHeaderView.invalidate();
        addHeaderView(mHeaderView, null, true);

        mFooterView = new PullFooterView(context);
        mFooterViewHeight = mFooterView.getViewHeight();
//		mFooterView.invalidate();
        addFooterView(mFooterView, null, true);

        mState = IDEL;
        updateHeaderViewByState(-mHeaderViewHeight);
        updateFooterViewByState(-mFooterViewHeight);
        mLastRefreshTime = DateUtil.getSystemDate(getResources().getString(R.string.pull_view_date_format));
    }
}
