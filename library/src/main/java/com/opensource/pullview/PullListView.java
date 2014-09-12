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

import android.annotation.SuppressLint;
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
import android.widget.Toast;
import com.opensource.pullview.utils.DateUtil;

/**
 * Usage A Custom ListView can be pull to refresh and load more<br>
 * <p>Off by default pull-to-refresh and load-more, but turn them on when<br>
 * call {@link #setOnRefreshListener(com.opensource.pullview.OnRefreshListener)} and {@link #setOnLoadMoreListener(OnLoadMoreListener)}<br><br>
 * 
 * <p>Pull-to-refresh and load-more can not doing at the same time.<br>
 * If pull-to-refresh is happening, you can't do load-more action befor pull-to refresh is finished.<br><br>
 * 
 * <p>You need to call {@link #refreshCompleted()} when refresh thread finished,<br>
 * Similarly, You also need to call {@link #loadMoreCompleted(boolean)} when load thread finished.<br>
 * 
 * @author yinglovezhuzhu@gmail.com
 */
public class PullListView extends ListView implements IPullView, AbsListView.OnScrollListener {
	
	private RotateAnimation mDownToUpAnimation;
	private RotateAnimation mUpToDownAnimation;
	
	//Make sure param mStartY only valued once in one touch event.
	private boolean mIsRecored;
	private int mStartY;
	private int mState;
	private boolean mIsBack;

	private PullHeaderView mHeaderView;
	private PullFooterView mFooterView;
	
	private int mHeaderViewHeight;
	private int mFooterViewHeight;

	private int mFirstItemIndex;
	private int mLastItemIndex;
	private int mTotalItemCount;
	
	/** Whether it can refresh. */
	private boolean mRefreshable = false;
	/** Whether it can load more data. */
	private boolean mLoadMoreable = false;
	/** Whether show tips when there is no more data to load **/
	private boolean mShowNoMoreDataTips = true;
	
	private String mLastRefreshTime = "";
	private int mHeaderLebelVisiblity = View.VISIBLE;
	
	private LoadMode mLoadMode = LoadMode.AUTO_LOAD;

	private OnRefreshListener mRefreshListener;
	private OnLoadMoreListener mLoadMoreListener;
	private OnScrollListener mScrollListener;

	/**
	 * Constructor
	 * @param context
	 */
	public PullListView(Context context) {
		super(context);
		initView(context);
	}

	/**
	 * Constructor
	 * @param context
	 */
	public PullListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	/**
	 * Constructor
	 * @param context
	 */
    public PullListView(Context context, AttributeSet attrs, int defStyle) {
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
			if(mLoadMoreable) {
				if(mLoadMode == LoadMode.AUTO_LOAD) {
					mState = LOADING;
					updateFooterViewByState();
					setSelection(mTotalItemCount);
					loadMore();
				}
			} else {
				//TODO 不能加载更多
				if(mShowNoMoreDataTips) {
					Toast.makeText(getContext(), getResources().getString(R.string.no_more_data), Toast.LENGTH_SHORT).show();
				}
			}
		}
		if(null != mScrollListener) {
			mScrollListener.onScrollStateChanged(view, scrollState);
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mStartY = (int) event.getY();
			if(!mIsRecored) {
				if(mRefreshable && mFirstItemIndex == 0) {
					mIsRecored = true;
				} else if(mLoadMoreable && mLastItemIndex == mTotalItemCount) {
					mIsRecored = true;
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			if(mState != LOADING) {
				if(mRefreshable && mFirstItemIndex == 0) {
					switch (mState) {
					case IDEL:
						//Do nothing.
						break;
					case PULL_TO_LOAD:
						//Pull to refresh.
						mState = IDEL;
						updateHeaderViewByState();
						break;
					case RELEASE_TO_LOAD:
						//Release to refresh.
						mState = LOADING;
						updateHeaderViewByState();
						refresh();
						break;
					default:
						break;
					}
				} else if(mLoadMode == LoadMode.PULL_TO_LOAD && mLoadMoreable && mLastItemIndex == mTotalItemCount) {
					switch (mState) {
					case IDEL:
						//Do nothing.
						break;
					case PULL_TO_LOAD:
						//Pull to load more data.
						mState = IDEL;
						updateFooterViewByState();
						break;
					case RELEASE_TO_LOAD:
						//Release to load more data.
						mState = LOADING;
						updateFooterViewByState();
						loadMore();
						break;
					default:
						break;
					}
				}
			} 
			mIsRecored = false;
			mIsBack = false;
			break;
		case MotionEvent.ACTION_MOVE:
			int tempY = (int) event.getY();
			if(mRefreshable && mFirstItemIndex == 0) {
				if (!mIsRecored) {
					mIsRecored = true;
					mStartY = tempY;
				}
				if (mState != LOADING && mIsRecored) {
					// Ensure that the process of setting padding, current position has always been at the header, 
					// or if when the list exceeds the screen, then, when the push, the list will scroll at the same time
					switch (mState) {
					case RELEASE_TO_LOAD: // Release to load data
						setSelection(0);
						// Slide up, header part was covered, but not all be covered(Pull up to cancel)
						if (((tempY - mStartY) / OFFSET_RATIO < mHeaderViewHeight) && (tempY - mStartY) > 0) {
							mState = PULL_TO_LOAD;
							updateHeaderViewByState();
						} else if (tempY - mStartY <= 0) {
							// Slide to the top
							mState = IDEL;
							updateHeaderViewByState();
						}
						mHeaderView.setPadding(0, -mHeaderViewHeight + (tempY - mStartY) / OFFSET_RATIO, 0, 0);
						break;
					case PULL_TO_LOAD:
						setSelection(0);
						// Pull down to the state can enter RELEASE_TO_REFRESH
						if ((tempY - mStartY) / OFFSET_RATIO >= mHeaderViewHeight) {
							mState = RELEASE_TO_LOAD;
							mIsBack = true;
							updateHeaderViewByState();
						} else if (tempY - mStartY <= 0) {
							mState = IDEL;
							updateHeaderViewByState();
						} else {
							mHeaderView.setPadding(0, (tempY - mStartY) / OFFSET_RATIO - mHeaderViewHeight, 0, 0);
						}
						break;
					case IDEL:
						if (tempY - mStartY > 0) {
							mState = PULL_TO_LOAD;
						}
						updateHeaderViewByState();
						break;
					default:
						break;
					}
				}
			} else if(mLoadMode == LoadMode.PULL_TO_LOAD && mLoadMoreable && mLastItemIndex == mTotalItemCount) {
				if (!mIsRecored) {
					mIsRecored = true;
					mStartY = tempY;
				}
				if (mState != LOADING && mIsRecored) {
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
	 * Show loading view on header<br>
	 * <br><p>Use this method when no header view was added on PullListView.
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
	 * The first time to load data with no block mode, then user header view to tell the user is loading data now.
	 * @param text the text to show.
	 * @deprecated Use onHeadLoading() or onFootLoading instead.
	 * @see {@link #onHeadLoading(CharSequence)}
	 * @see {@link #onHeadLoading(int)}
	 * @see {@link #onFootLoading(CharSequence)}
	 * @see {@link #onFootLoading(int)}
	 */
	public void onFirstLoadingData(CharSequence text) {
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
	 * The first time to load data with no block mode, then user header view to tell the user is loading data now.
	 * @param resId the text to show.
	 * @deprecated Use onHeadLoading() or onFootLoading instead.
	 * @see {@link #onHeadLoading(CharSequence)}
	 * @see {@link #onHeadLoading(int)}
	 * @see {@link #onFootLoading(CharSequence)}
	 * @see {@link #onFootLoading(int)}
	 */
	public void onFirstLoadingData(int resId) {
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
	 * Set the mode to load more data.<br>
	 * <p>can use value is {@link com.opensource.pullview.PullListView.LoadMode#AUTO_LOAD} and {@link com.opensource.pullview.PullListView.LoadMode#PULL_TO_LOAD}<br>
	 * default is {@link com.opensource.pullview.PullListView.LoadMode#AUTO_LOAD}
	 * @param mode
	 * @see {@link com.opensource.pullview.PullListView.LoadMode}
	 */
	public void setLoadMode(LoadMode mode) {
		this.mLoadMode = mode;
	}
	
	/**
	 * Set last refresh time<br>
	 * <p>The value of {@link #mLastRefreshTime} initialized to the time when create {@link com.opensource.pullview.PullListView} object.<br>
	 * You can set this value.
	 * @param time
	 */
	public void setLastRefreshTime(String time) {
		this.mLastRefreshTime = time;
	}
	
	/**
	 * Set header view label's visibility.<br>
	 * <p>You can set the value of {@link android.view.View#GONE}、{@link android.view.View#VISIBLE}<br>
	 * @param visibility
	 * 
	 * @see android.view.View#GONE
	 * @see android.view.View#VISIBLE
	 */
	public void setHeaderLabelVisibility(int visibility) {
		this.mHeaderLebelVisiblity = visibility;
		if(mHeaderLebelVisiblity == View.INVISIBLE) {
			mHeaderLebelVisiblity = View.GONE;
		}
		mHeaderView.setLabelVisibility(mHeaderLebelVisiblity);
	}
	
	/**
	 * Set show tips when there is no more data to load<br>
	 * Show is default.
	 * @param isShow
	 */
	public void setShowNoMoreDataTips(boolean isShow) {
		this.mShowNoMoreDataTips = isShow;
	}
	
	/**
	 * Refresh data complete
	 */
	public void refreshCompleted() {
		mState = IDEL;
		mLastRefreshTime = DateUtil.getSystemDate(getResources().getString(R.string.pull_view_date_format));
		updateHeaderViewByState();
	}
	
	/**
	 * Load more complete
	 */
	public void loadMoreCompleted(boolean loadMoreable) {
		mState = IDEL;
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
		
		mHeaderView = new PullHeaderView(context);
		mHeaderView.setLabelVisibility(View.VISIBLE);
		mHeaderViewHeight = mHeaderView.getViewHeight();
		mHeaderView.setPadding(0, -mHeaderViewHeight, 0, 0);
//		mHeaderView.invalidate();
		addHeaderView(mHeaderView, null, false);

		mFooterView = new PullFooterView(context);
		mFooterViewHeight = mFooterView.getViewHeight();
		mFooterView.setPadding(0, 0, 0, -mFooterViewHeight);
//		mFooterView.invalidate();
		addFooterView(mFooterView, null, false);
		
		mState = IDEL;
		super.setOnScrollListener(this);
		
		mLastRefreshTime = DateUtil.getSystemDate(getResources().getString(R.string.pull_view_date_format));
	}

	/**
	 * Update header view by state.
	 */
	private void updateHeaderViewByState() {
		switch (mState) {
		case RELEASE_TO_LOAD:
			mHeaderView.setArrowVisibility(View.VISIBLE);
			mHeaderView.setProgressVisibility(View.GONE);
			mHeaderView.setTitileVisibility(View.VISIBLE);
			mHeaderView.startArrowAnimation(mDownToUpAnimation);
			mHeaderView.setTitleText(R.string.pull_view_release_to_refresh);
			mHeaderView.setLabelText(getResources().getString(R.string.pull_view_refresh_time)
					+ mLastRefreshTime);
			break;
		case PULL_TO_LOAD:
			mHeaderView.setArrowVisibility(View.VISIBLE);
			mHeaderView.setProgressVisibility(View.GONE);
			mHeaderView.setTitileVisibility(View.VISIBLE);

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
			mHeaderView.setPadding(0, 0, 0, 0);
			mHeaderView.setArrowVisibility(View.GONE);
			mHeaderView.setProgressVisibility(View.VISIBLE);
			mHeaderView.setTitileVisibility(View.VISIBLE);
			mHeaderView.startArrowAnimation(null);
			mHeaderView.setTitleText(R.string.pull_view_refreshing);
			mHeaderView.setLabelText(getResources().getString(R.string.pull_view_refresh_time)
					+ mLastRefreshTime);
			break;
		case IDEL:
			mHeaderView.setPadding(0, -mHeaderViewHeight, 0, 0);
			mHeaderView.setProgressVisibility(View.GONE);
			mHeaderView.startArrowAnimation(null);
			mHeaderView.setTitleText(R.string.pull_view_pull_to_refresh);
			mHeaderView.setLabelText(getResources().getString(R.string.pull_view_refresh_time)
					+ mLastRefreshTime);
			break;
		default:
			break;
		}
		mHeaderView.setLabelVisibility(mHeaderLebelVisiblity);
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
			mLoadMoreListener.onLoadMore();
		}
	}
	
	/**
	 * Refresh
	 */
	private void refresh() {
		if (mRefreshListener != null) {
			mRefreshListener.onRefresh();
		}
	}
}
