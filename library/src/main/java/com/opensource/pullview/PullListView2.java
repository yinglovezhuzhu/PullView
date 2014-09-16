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
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * Usage A Custom ListView can be pull to refresh and load more<br>
 * <p>Off by default pull-to-refresh and load-more, but turn them on when<br>
 * call {@link #setOnRefreshListener(OnRefreshListener)} and {@link #setOnLoadMoreListener(com.opensource.pullview.OnLoadMoreListener)}<br><br>
 * <p/>
 * <p>Pull-to-refresh and load-more can not doing at the same time.<br>
 * If pull-to-refresh is happening, you can't do load-more action befor pull-to refresh is finished.<br><br>
 * <p/>
 * <p>You need to call {@link #refreshCompleted()}  when refresh thread finished,<br>
 * Similarly, You also need to call {@link #loadMoreCompleted(boolean)} when load thread finished.<br>
 *
 * @author yinglovezhuzhu@gmail.com
 */

public class PullListView2 extends BasePullListView {

    private static final int DEFAULT_MIN_PULL_DOWN_REFRESH_DISTANCE = 80;

    private PullHeaderView2 mHeaderView;

    private int mHeaderViewVisibleHeight;
    private int mHeaderViewStateHeight;

    /**
     * The distance pull down to refresh *
     */
    private int mMinPullDownDist = DEFAULT_MIN_PULL_DOWN_REFRESH_DISTANCE;


    /**
     * Constructor
     *
     * @param context
     */
    public PullListView2(Context context) {
        super(context);
        initView(context);
    }

    /**
     * Constructor
     *
     * @param context
     */
    public PullListView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    /**
     * Constructor
     *
     * @param context
     */
    public PullListView2(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    private int mStartY = 0;
    private boolean mRecording = false;
    private boolean mIsBack = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartY = (int) event.getY();
                if (!mRecording) {
                    mRecording = mFirstItemIndex == 0;
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

                    // Ensure that the process of setting padding, current position has always been at the header,
                    // or if when the list exceeds the screen, then, when the push, the list will scroll at the same time
                    switch (mState) {
                        case RELEASE_TO_LOAD: // Release to load data
                            setSelection(mFirstItemIndex);
                            // Slide up, header part was covered, but not all be covered(Pull up to cancel)
                            if (moveY > 0 && (scrollY < mMinPullDownDist)) {
                                mState = PULL_TO_LOAD;
                            } else if (moveY <= 0) {
                                // Slide to the top
                                mState = IDEL;
                            }

                            updateHeaderViewByState(mHeaderViewVisibleHeight - mHeaderViewHeight + scrollY);
                            break;
                        case PULL_TO_LOAD:
                            setSelection(mFirstItemIndex);
                            // Pull down to the state can enter RELEASE_TO_REFRESH
                            if (moveY <= 0) {
                                mState = IDEL;
                            } else if (scrollY >= mMinPullDownDist) {
                                mState = RELEASE_TO_LOAD;
                                mIsBack = true;
                            }
                            updateHeaderViewByState(mHeaderViewVisibleHeight - mHeaderViewHeight + scrollY);
                            break;
                        case LOADING:
                            if (moveY > 0) {
                                updateHeaderViewByState(mHeaderViewVisibleHeight - mHeaderViewHeight + scrollY);
                            }
                            break;
                        case IDEL:
                            if (moveY > 0) {
                                mState = PULL_TO_LOAD;
                            }
                            updateHeaderViewByState(mHeaderViewVisibleHeight - mHeaderViewHeight);
                            break;
                        default:
                            break;
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
                            break;
                        case RELEASE_TO_LOAD:
                            if (mEnablePullRefresh) {
                                //Release to refresh.
                                refresh();
                                mState = LOADING;
                            } else {
                                mState = IDEL;
                            }
                            break;
                        case LOADING:
                            break;
                        default:
                            break;
                    }
                    updateHeaderViewByState(mHeaderViewVisibleHeight - mHeaderViewHeight);
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
    protected void loadMore() {
        super.loadMore();
        mHeaderView.setStateContentVisibility(View.INVISIBLE);
    }

    @Override
    protected void refresh() {
        super.refresh();
        mHeaderView.setStateContentVisibility(View.VISIBLE);
    }

    @Override
    public void refreshCompleted() {
        super.refreshCompleted();
        mHeaderView.setStateContentVisibility(View.VISIBLE);
        updateHeaderViewByState(mHeaderViewVisibleHeight - mHeaderViewHeight);
    }

    @Override
    public void loadMoreCompleted(boolean canLoadmore) {
        super.loadMoreCompleted(canLoadmore);
        mHeaderView.setStateContentVisibility(View.VISIBLE);
    }

    /**
     * Sets header background image to ImageView
     *
     * @param resid
     */
    public void setHeaderBackgroundImage(int resid) {
        mHeaderView.setBackgroundResource(resid);
    }

    /**
     * Sets header background image to ImageView
     *
     * @param bm
     */
    public void setHeaderBackgroundImage(Bitmap bm) {
        mHeaderView.setBackgroundImage(bm);
    }

    /**
     * Gets header background ImageView
     *
     * @return
     */
    public ImageView getHeaderBackgroundImageView() {
        return mHeaderView.getBackgroudImageView();
    }

    /**
     * Sets background view<br/><br/>
     * <p/>
     * If you use this method to set background view, it will replace default background ImageView.
     *
     * @param backgroundView
     */
    public void setHeaderBackgroundView(View backgroundView) {
        mHeaderView.setBackgroundView(backgroundView);
    }

    /**
     * Sets background view<br/><br/>
     * <p/>
     * If you use this method to set background view, it will replace default background ImageView.
     *
     * @param layoutId
     */
    public void setHeaderBackgroundView(int layoutId) {
        mHeaderView.setBackgroudView(layoutId);
    }

    /**
     * Init views
     *
     * @param context
     */
    private void initView(Context context) {

        mHeaderView = new PullHeaderView2(context);
        mHeaderViewHeight = mHeaderView.getViewHeight();
        mHeaderViewVisibleHeight = mHeaderView.getVisibleHeight();
        mHeaderViewStateHeight = mHeaderView.getStateViewHeight();
        mMinPullDownDist = mHeaderViewStateHeight > DEFAULT_MIN_PULL_DOWN_REFRESH_DISTANCE
                ? mHeaderViewStateHeight : DEFAULT_MIN_PULL_DOWN_REFRESH_DISTANCE; //下拉刷新需要滑动的距离
        mHeaderView.setStateContentVisibility(mEnablePullRefresh ? View.VISIBLE : View.INVISIBLE);
        addHeaderView(mHeaderView, null, false);

        mState = IDEL;
        updateHeaderViewByState(mHeaderViewVisibleHeight - mHeaderViewHeight);
    }

    private void updateHeaderViewByState(int paddingTop) {
        switch (mState) {
            case RELEASE_TO_LOAD:
                mHeaderView.setStateContentPadding(0, -paddingTop, 0, 0);
                break;
            case PULL_TO_LOAD:
                mHeaderView.setStateContentPadding(0, mHeaderViewHeight - mHeaderViewVisibleHeight - mHeaderViewStateHeight, 0, 0);
                break;
            case LOADING:
                mHeaderView.setStateContentPadding(0, -paddingTop, 0, 0);
                break;
            case IDEL:
                mHeaderView.setStateContentPadding(0, -paddingTop - mHeaderViewStateHeight, 0, 0);
                break;
            default:
                break;
        }
        mHeaderView.setStateContentVisibility(mEnablePullRefresh ? View.VISIBLE : View.INVISIBLE);
        mHeaderView.setPadding(0, paddingTop, 0, 0);
    }
}