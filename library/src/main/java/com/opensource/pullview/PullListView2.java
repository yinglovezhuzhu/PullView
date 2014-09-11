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
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * Usage A Custom ListView can be pull to refresh and load more<br>
 * <p>Off by default pull-to-refresh and load-more, but turn them on when<br>
 * call {@link #setOnRefreshListener(OnRefreshListener)} and {@link #setOnLoadMoreListener(com.opensource.pullview.OnLoadMoreListener)}<br><br>
 *
 * <p>Pull-to-refresh and load-more can not doing at the same time.<br>
 * If pull-to-refresh is happening, you can't do load-more action befor pull-to refresh is finished.<br><br>
 *
 * <p>You need to call {@link #refreshCompleted()}  when refresh thread finished,<br>
 * Similarly, You also need to call {@link #loadMoreCompleted(boolean)} when load thread finished.<br>
 *
 * @author yinglovezhuzhu@gmail.com
 */
public class PullListView2 extends ListView implements IPullView, AbsListView.OnScrollListener {

    private static final int DEFAULT_MIN_PULL_DOWN_REFRESH_DISTANCE = 80;

	private RotateAnimation mDownToUpAnimation;
	private RotateAnimation mUpToDownAnimation;

	//Make sure param mStartY only valued once in one touch event.
	private boolean mRecording;
	private int mStartY;
	private int mState;
	private boolean mIsBack;

	private PullHeaderView2 mHeaderView;
	private PullFooterView mFooterView;

    private int mHeaderViewHeight;
    private int mHeaderViewVisiableHeight;
    private int mHeaderViewStateHeight;

	private int mFooterViewHeight;

	private int mFirstItemIndex;
	private int mLastItemIndex;
	private int mTotalItemCount;

    /** The distance pull down to refresh **/
    private int mMinPullDownDist = DEFAULT_MIN_PULL_DOWN_REFRESH_DISTANCE;

	/** Whether it can refresh. */
	private boolean mRefreshable = false;
	/** Whether it can load more data. */
	private boolean mLoadMoreable = false;

	private LoadMode mLoadMode = LoadMode.AUTO_LOAD;

    private boolean mRefreshing = false;

	private OnRefreshListener mRefreshListener;
	private OnLoadMoreListener mLoadMoreListener;
	private OnScrollListener mScrollListener;

	/**
	 * The mode of load more.<br>
	 * <p>{@link com.opensource.pullview.PullListView2.LoadMode#PULL_TO_LOAD} pull-to-loadmore<br>
	 * You need to pull to load more data<br><br>
	 * <p>{@link com.opensource.pullview.PullListView2.LoadMode#AUTO_LOAD} auto-loadmore<br>
	 * When you scroll to the end of data list, it will auto load more data if has more data.
	 *
	 * @author yinglovezhuzhu@gmail.com
	 *
	 */
	public static enum LoadMode {
		PULL_TO_LOAD, AUTO_LOAD,
	}

	/**
	 * Constructor
	 * @param context
	 */
	public PullListView2(Context context) {
		super(context);
		initView(context);
	}

	/**
	 * Constructor
	 * @param context
	 */
	public PullListView2(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	/**
	 * Constructor
	 * @param context
	 */
    public PullListView2(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    @Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		mFirstItemIndex = firstVisibleItem;
		mLastItemIndex = firstVisibleItem + visibleItemCount;
		mTotalItemCount = totalItemCount;
		if(null != mScrollListener) {
			mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if(scrollState == SCROLL_STATE_IDLE && mLastItemIndex == mTotalItemCount && mState == IDEL) {
			if(mLoadMoreable && mLoadMode == LoadMode.AUTO_LOAD) {
                setSelection(mTotalItemCount);
                loadMore();
                mState = LOADING;
                updateFooterViewByState();
			}
		}
		if(null != mScrollListener) {
			mScrollListener.onScrollStateChanged(view, scrollState);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mStartY = (int) event.getY();
			if(!mRecording) {
                mRecording = mFirstItemIndex == 0 || mLastItemIndex == mTotalItemCount;
			}
            break;
        case MotionEvent.ACTION_MOVE:
            int tempY = (int) event.getY();
            if(mFirstItemIndex == 0) {
                if (!mRecording) {
                    mRecording = true;
                    mStartY = tempY;
                }
                int distY = tempY - mStartY;
                int trueDistY = distY / OFFSET_RATIO;
//                if (mState != LOADING && mRecording) {
                if (mRecording) {
                    // Ensure that the process of setting padding, current position has always been at the header,
                    // or if when the list exceeds the screen, then, when the push, the list will scroll at the same time
                    switch (mState) {
                        case RELEASE_TO_LOAD: // Release to load data
                            setSelection(mFirstItemIndex);
                            // Slide up, header part was covered, but not all be covered(Pull up to cancel)
                            if (distY > 0 && (trueDistY < mMinPullDownDist)) {
                                mState = PULL_TO_LOAD;
                            } else if (distY <= 0) {
                                // Slide to the top
                                mState = IDEL;
                            }

                            updateHeaderViewByState(mHeaderViewVisiableHeight - mHeaderViewHeight + trueDistY);
                            break;
                        case PULL_TO_LOAD:
                            setSelection(mFirstItemIndex);
                            // Pull down to the state can enter RELEASE_TO_REFRESH
                            if (distY <= 0) {
                                mState = IDEL;
                            } else if (trueDistY >= mMinPullDownDist) {
                                mState = RELEASE_TO_LOAD;
                                mIsBack = true;
                            }
                            updateHeaderViewByState(mHeaderViewVisiableHeight - mHeaderViewHeight + trueDistY);
                            break;
                        case LOADING:
                            if(distY > 0) {
                                updateHeaderViewByState(mHeaderViewVisiableHeight - mHeaderViewHeight + trueDistY);
                            }
                            break;
                        case IDEL:
                            if (distY > 0) {
                                mState = PULL_TO_LOAD;
                            }
                            updateHeaderViewByState(mHeaderViewVisiableHeight - mHeaderViewHeight);
                            break;
                        default:
                            break;
                    }
                }
//            } else if(mLastItemIndex == mTotalItemCount && mLoadMode == LoadMode.PULL_TO_LOAD && mLoadMoreable) {
            } else if(mLastItemIndex == mTotalItemCount) {
                if (!mRecording) {
                    mRecording = true;
                    mStartY = tempY;
                }
//                if (mState != LOADING && mRecording) {
                if (mRecording && mLoadMode == LoadMode.PULL_TO_LOAD) {
                    // Ensure that the process of setting padding, current position has always been at the footer,
                    // or if when the list exceeds the screen, then, when the push up, the list will scroll at the same time
                    switch (mState) {
                        case RELEASE_TO_LOAD: // release-to-load
                            setSelection(mTotalItemCount);
                            // Slide down, header part was covered, but not all be covered(Pull down to cancel)
                            if (((mStartY - tempY) / OFFSET_RATIO < mFooterViewHeight) && (mStartY - tempY) > 0) {
                                mState = PULL_TO_LOAD;
                                updateFooterViewByState();
                            } else if (mStartY - tempY <= 0) { //Slide up(Pull up to make footer to show)
                                mState = IDEL;
                                updateFooterViewByState();
                            } else {
                                mFooterView.setPadding(0, 0, 0, (mStartY - tempY) / OFFSET_RATIO - mFooterViewHeight);
                            }
                            break;
                        case PULL_TO_LOAD:
                            setSelection(mTotalItemCount);
                            // Pull up to the state can enter RELEASE_TO_REFRESH
                            if ((mStartY - tempY) / OFFSET_RATIO >= mFooterViewHeight) {
                                mState = RELEASE_TO_LOAD;
                                mIsBack = true;
                                updateFooterViewByState();
                            } else if (mStartY - tempY <= 0) {
                                mState = IDEL;
                                updateFooterViewByState();
                            } else {
                                mFooterView.setPadding(0, 0, 0, (mStartY - tempY) / OFFSET_RATIO - mFooterViewHeight);
                            }
                            break;
                        case LOADING:
                            if(mStartY - tempY > 0) {
                                mFooterView.setPadding(0, 0, 0, (mStartY - tempY) / OFFSET_RATIO - mFooterViewHeight);
                            }
                            break;
                        case IDEL:
                            if (mStartY - tempY > 0) {
                                mState = PULL_TO_LOAD;
                            }
                            updateFooterViewByState();
                            break;
                        default:
                            break;
                    }
                }
            }
            break;
		case MotionEvent.ACTION_UP:
//			if(mState != LOADING) {
//				if(mRefreshable && mFirstItemIndex == 0) {
				if(mFirstItemIndex == 0) {
					switch (mState) {
					case IDEL:
						//Do nothing.
//						updateHeaderViewByState(mHeaderViewVisiableHeight - mHeaderViewHeight);
						break;
					case PULL_TO_LOAD:
						//Pull to refresh.
						mState = IDEL;
						updateHeaderViewByState(mHeaderViewVisiableHeight - mHeaderViewHeight);
						break;
					case RELEASE_TO_LOAD:
						//Release to refresh.
						refresh();
						mState = LOADING;
						updateHeaderViewByState(mHeaderViewVisiableHeight - mHeaderViewHeight);
						break;
					default:
						updateHeaderViewByState(mHeaderViewVisiableHeight - mHeaderViewHeight);
						break;
					}
//				} else if(mLastItemIndex == mTotalItemCount && mLoadMode == LoadMode.PULL_TO_LOAD && mLoadMoreable) {
				} else if(mLastItemIndex == mTotalItemCount && mLoadMode == LoadMode.PULL_TO_LOAD) {
					switch (mState) {
					case IDEL:
						//Do nothing.
                        mFooterView.setPadding(0, 0, 0, 0);
						break;
					case PULL_TO_LOAD:
						//Pull to load more data.
						mState = IDEL;
						updateFooterViewByState();
						break;
					case RELEASE_TO_LOAD:
						//Release to load more data.
						loadMore();
						mState = LOADING;
						updateFooterViewByState();
						break;
					default:
						break;
					}
				}
//			}
			mRecording = false;
			mIsBack = false;
			break;
		default:
			break;
		}
		return super.onTouchEvent(event);
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		super.setAdapter(adapter);
	}

	@Override
	public void setOnScrollListener(OnScrollListener l) {
		this.mScrollListener = l;
	}

	/**
	 * Set the background color of HeaderView
	 * @param color
	 */
	public void setHeaderViewBackgroundColor(int color) {
		mHeaderView.setBackgroundColor(color);
	}

	/**
	 * Set the background color of FootView
	 * @param color
	 */
	public void setFootViewBackgroundColor(int color) {
		mFooterView.setBackgroundColor(color);
	}

	/**
	 * Set listener to listen refresh action
	 * @param listener
	 */
	public void setOnRefreshListener(OnRefreshListener listener) {
		this.mRefreshListener = listener;
		mRefreshable = null != listener;
        mHeaderView.setStateContentVisibility(mRefreshable ? View.VISIBLE : View.INVISIBLE);
	}

	/**
	 * Set listener to listen load more action
	 * @param listener
	 */
	public void setOnLoadMoreListener(OnLoadMoreListener listener) {
		this.mLoadMoreListener = listener;
		mLoadMoreable = null != listener;
	}


	/**
	 * Show loading view on foot<br>
	 * <br><p>Use this method when header view was added on PullListView.
	 * @param text
	 */
	public void onFootLoading(CharSequence text) {
		mState = LOADING;
        mHeaderView.setStateContentVisibility(View.INVISIBLE);
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
	 * @param resId
	 */
	public void onFootLoading(int resId) {
		mState = LOADING;
        mHeaderView.setStateContentVisibility(View.INVISIBLE);
		mFooterView.setPadding(0, 0, 0, 0);
		mFooterView.setArrowVisibility(View.GONE);
		mFooterView.setProgressVisibility(View.VISIBLE);
		mFooterView.setTitileVisibility(View.VISIBLE);
		mFooterView.startArrowAnimation(null);
		mFooterView.setTitleText(resId);
	}


	/**
	 * Set the mode to load more data.<br>
	 * <p>can use value is {@link com.opensource.pullview.PullListView2.LoadMode#AUTO_LOAD} and {@link com.opensource.pullview.PullListView2.LoadMode#PULL_TO_LOAD}<br>
	 * default is {@link com.opensource.pullview.PullListView2.LoadMode#AUTO_LOAD}
	 * @param mode
	 * @see {@link com.opensource.pullview.PullListView2.LoadMode}
	 */
	public void setLoadMode(LoadMode mode) {
		this.mLoadMode = mode;
	}

	/**
	 * Refresh data complete
	 */
	public void refreshCompleted() {
		mState = IDEL;
        mRefreshing = false;
        mRecording = false;
        mHeaderView.setStateContentVisibility(View.VISIBLE);
		updateHeaderViewByState(mHeaderViewVisiableHeight - mHeaderViewHeight);
	}

	/**
	 * Load more complete
	 */
	public void loadMoreCompleted(boolean loadMoreable) {
		mState = IDEL;
        mHeaderView.setStateContentVisibility( View.VISIBLE);
		updateFooterViewByState();
		this.mLoadMoreable = loadMoreable;
	}


	/**
	 * Init views
	 * @param context
	 */
	private void initView(Context context) {
		mDownToUpAnimation = new RotateAnimation(0, -180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		mDownToUpAnimation.setInterpolator(new LinearInterpolator());
		mDownToUpAnimation.setDuration(ROTATE_ANIMATION_DURATION);
		mDownToUpAnimation.setFillAfter(true);

		mUpToDownAnimation = new RotateAnimation(-180, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		mUpToDownAnimation.setInterpolator(new LinearInterpolator());
		mUpToDownAnimation.setDuration(ROTATE_ANIMATION_DURATION);
		mUpToDownAnimation.setFillAfter(true);
		
		mHeaderView = new PullHeaderView2(context);
        mHeaderViewHeight = mHeaderView.getViewHeight();
        mHeaderViewVisiableHeight = mHeaderView.getVisibleHeight();
		mHeaderView.setPadding(0, mHeaderViewVisiableHeight - mHeaderViewHeight, 0, 0);
        mHeaderViewStateHeight = mHeaderView.getStateViewHeight();
        mMinPullDownDist = mHeaderViewStateHeight > DEFAULT_MIN_PULL_DOWN_REFRESH_DISTANCE
                ? mHeaderViewStateHeight : DEFAULT_MIN_PULL_DOWN_REFRESH_DISTANCE; //下拉刷新需要滑动的距离
		mHeaderView.setStateContentVisibility(mRefreshable ? View.VISIBLE : View.INVISIBLE);
        addHeaderView(mHeaderView, null, false);
//		mHeaderView.invalidate();

		mFooterView = new PullFooterView(context);
		mFooterViewHeight = mFooterView.getViewHeight();
		mFooterView.setPadding(0, 0, 0, -mFooterViewHeight);
//		mFooterView.invalidate();
		addFooterView(mFooterView, null, false);
		
		mState = IDEL;
		super.setOnScrollListener(this);
		
	}

	/**
	 * Update header view by state.
	 */
	private void updateHeaderViewByState(int paddingTop) {
        switch (mState) {
            case RELEASE_TO_LOAD:
                mHeaderView.setStateContentPadding(0, -paddingTop, 0, 0);
                break;
            case PULL_TO_LOAD:
                mHeaderView.setStateContentPadding(0, mHeaderViewHeight - mHeaderViewVisiableHeight - mHeaderViewStateHeight, 0, 0);
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
        mHeaderView.setPadding(0, paddingTop, 0, 0);
	}
	
	/**
	 * Update footer view by state
	 */
	private void updateFooterViewByState() {
		switch (mState) {
		case RELEASE_TO_LOAD:
			mFooterView.setArrowVisibility(View.VISIBLE);
			mFooterView.setProgressVisibility(View.GONE);
			mFooterView.setTitileVisibility(View.VISIBLE);
			mFooterView.startArrowAnimation(mDownToUpAnimation);
			mFooterView.setTitleText(R.string.pull_view_release_to_load);
			break;
		case PULL_TO_LOAD:
			mFooterView.setArrowVisibility(View.VISIBLE);
			mFooterView.setProgressVisibility(View.GONE);
			mFooterView.setTitileVisibility(View.VISIBLE);

			if (mIsBack) {
				mIsBack = false;
				mFooterView.startArrowAnimation(mUpToDownAnimation);
			}
			mFooterView.setTitleText(R.string.pull_view_pull_to_load);
			break;
		case LOADING:
			mFooterView.setPadding(0, 0, 0, 0);
			mFooterView.setArrowVisibility(View.GONE);
			mFooterView.setProgressVisibility(View.VISIBLE);
			mFooterView.setTitileVisibility(View.VISIBLE);
			mFooterView.startArrowAnimation(null);
			mFooterView.setTitleText(R.string.pull_view_loading);
			break;
		case IDEL:
			mFooterView.setPadding(0, 0, 0, -mFooterViewHeight);
			mFooterView.setProgressVisibility(View.GONE);
			mFooterView.startArrowAnimation(null);
			mFooterView.setTitleText(R.string.pull_view_release_to_load);
			break;
		default:
			break;
		}
	}

	/**
	 * Load more
	 */
	private void loadMore() {
		if(mLoadMoreListener != null) {
            if(mState == LOADING) {
                if(mRefreshing) {
                    mLoadMoreListener.onError(OnRefreshListener.ERROR_CODE_REFRESHING);
                } else {
                    mLoadMoreListener.onError(OnRefreshListener.ERROR_CODE_LOADINGMORE);
                }
                return;
            }
			mLoadMoreListener.onLoadMore();
            mRefreshing = false;
            mHeaderView.setStateContentVisibility(View.INVISIBLE);
		}
	}

	/**
	 * Refresh
	 */
	private void refresh() {
		if (mRefreshListener != null) {
            if(mState == LOADING) {
                if(mRefreshing) {
                    mRefreshListener.onError(OnRefreshListener.ERROR_CODE_REFRESHING);
                } else {
                    mRefreshListener.onError(OnRefreshListener.ERROR_CODE_LOADINGMORE);
                }
                return;
            }
			mRefreshListener.onRefresh();
            mRefreshing = true;
            mHeaderView.setStateContentVisibility(View.VISIBLE);
		}
	}
}
