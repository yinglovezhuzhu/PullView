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
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Scroller;

import com.opensource.pullview.utils.DateUtil;

/**
 * Usage A custom scroll view can be pull to refresh.<br>
 * 
 * <p> You can add child view use addView method.<br>
 * also you can add child view in layout xml file like this.<br>
 * 
 * @author yinglovezhuzhu@gmail.com
 */
public class PullScrollView_back extends ScrollView implements IPullView {

	/** The Constant SCROLLBACK_HEADER. */
	private final static int SCROLLBACK_HEADER = 0;

	/** The Constant SCROLL_DURATION. */
	private final static int SCROLL_DURATION = 400;

	/** The m last y. */
	private float mLastY = -1;

	/** The m scroller. */
	private Scroller mScroller;

	/** The m scroll layout. */
	private LinearLayout mScrollLayout;

	/** The m header view. */
	private PullHeaderView mHeaderView;

	/** The m header view height. */
	private int mHeaderViewHeight;

	/** The m enable pull refresh. */
	private boolean mEnablePullRefresh = false;

	/** The m pull refreshing. */
	private boolean mPullRefreshing = false;

	/** The m ab on refresh listener. */
	private OnRefreshListener mOnRefreshListener = null;

	/** The m scroll back. */
	private int mScrollBack;

	/** The m state. */
	private int mState = IDEL;

	private String mLastRefreshTime = "";

	/** The m rotate up anim. */
	private Animation mRotateUpAnim;

	/** The m rotate down anim. */
	private Animation mRotateDownAnim;

	/**
	 * Constructor
	 *
	 * @param context the context
	 */
	public PullScrollView_back(Context context) {
		super(context);
		initView(context);
	}

	/**
	 * Constructor
	 *
	 * @param context the context
	 * @param attrs the attrs
	 */
	public PullScrollView_back(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if(mEnablePullRefresh) {
			if (mLastY == -1) {
				mLastY = ev.getRawY();
			}
			
			switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mLastY = ev.getRawY();
				break;
			case MotionEvent.ACTION_MOVE:
				final float deltaY = ev.getRawY() - mLastY;
				mLastY = ev.getRawY();
				if ((mHeaderView.getVisiableHeight() > 0 || deltaY > 0)) {
					updateHeaderView(deltaY / OFFSET_RATIO);
				}
				break;
			case MotionEvent.ACTION_UP:
				mLastY = -1;
				updateHeaderHeight();
				if(mHeaderView.getVisiableHeight() >= mHeaderViewHeight) {
					startRefresh();
				}
				break;
			default:
				break;
			}
		}
		return super.onTouchEvent(ev);
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			if (mScrollBack == SCROLLBACK_HEADER) {
				mHeaderView.setVisiableHeight(mScroller.getCurrY());
                System.out.println(mScroller.getCurrY());
            }
			postInvalidate();
		}
		super.computeScroll();
	}

	@Override
	public void addView(View child) {
		if(getChildCount() > 0) {
			mScrollLayout.addView(child);
		} else {
			super.addView(child);
		}
	}
	
	@Override
	public void addView(View child, int index) {
		if(getChildCount() > 0) {
			mScrollLayout.addView(child, index);
		} else {
			super.addView(child, index);
		}
	}
	
	@Override
	public void addView(View child, int width, int height) {
		if(getChildCount() > 0) {
			mScrollLayout.addView(child, width, height);
		} else {
			super.addView(child, width, height);
		}
	}
	
	@Override
	public void addView(View child, android.view.ViewGroup.LayoutParams params) {
		if(getChildCount() > 0) {
			mScrollLayout.addView(child, params);
		} else {
			super.addView(child, params);
		}
	}
	
	@Override
	public void addView(View child, int index,
			android.view.ViewGroup.LayoutParams params) {
		if(getChildCount() > 0) {
			mScrollLayout.addView(child, index, params);
		} else {
			super.addView(child, index, params);
		}
	}

	/**
	 * Set Refresh Listener.
	 * 
	 * @param listener
	 */
	public void setOnRefreshListener(OnRefreshListener listener) {
		mOnRefreshListener = listener;
		setPullRefreshEnable(null != listener);
	}

	/**
	 * Refresh complete
	 */
	public void refreshComplete() {
//		if (mPullRefreshing == true) {
			mPullRefreshing = false;
			updateHeaderHeight();
			mLastRefreshTime = DateUtil.getSystemDate("yyyy-MM-dd HH:mm:ss");
			mHeaderView.setLabelText(getResources().getText(R.string.pull_view_refresh_time) + " " + mLastRefreshTime);
//		}
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
		if(visibility == View.INVISIBLE) {
			mHeaderView.setLabelVisibility(View.GONE); 
		}
		mHeaderView.setLabelVisibility(visibility);
	}
	
	/**
	 * Set last refresh time.
	 * @param time
	 */
	public void setLastRefreshTime(String time) {
		this.mLastRefreshTime = time;
	}
	
	/**
	 * Get header view
	 * 
	 * @return
	 * @throws
	 */
	public PullHeaderView getHeaderView() {
		return mHeaderView;
	}

	/**
	 * 
	 * Get Progress
	 * 
	 * @return
	 * @throws
	 */
	public ProgressBar getHeaderProgress() {
		return mHeaderView.getProgress();
	}

	/**
	 * Init the View.
	 * 
	 * @param context the context
	 */
	private void initView(Context context) {
		mScroller = new Scroller(context, new DecelerateInterpolator());

		//Add content layout
		LinearLayout.LayoutParams headerLp = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		mScrollLayout = new LinearLayout(context);
		mScrollLayout.setLayoutParams(headerLp);
		mScrollLayout.setOrientation(LinearLayout.VERTICAL);

		// init header view
		mHeaderView = new PullHeaderView(context);
		mHeaderView.setVisiableHeight(0);

		// init header height
		mHeaderViewHeight = mHeaderView.getViewHeight();
		mHeaderView.setGravity(Gravity.BOTTOM);
		mScrollLayout.addView(mHeaderView, headerLp);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT, Gravity.TOP);
		this.addView(mScrollLayout, lp);
		
		mRotateUpAnim = new RotateAnimation(0.0f, -180.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		mRotateUpAnim.setDuration(ROTATE_ANIMATION_DURATION);
		mRotateUpAnim.setFillAfter(true);
		mRotateDownAnim = new RotateAnimation(-180.0f, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		mRotateDownAnim.setDuration(ROTATE_ANIMATION_DURATION);
		mRotateDownAnim.setFillAfter(true);
		
		mLastRefreshTime = DateUtil.getSystemDate(getResources().getString(R.string.pull_view_date_format));
	}

	/**
	 * Refresh Header height.
	 */
	private void updateHeaderHeight() {
		int height = mHeaderView.getVisiableHeight();
		if (height < mHeaderViewHeight || !mPullRefreshing) {
			mScrollBack = SCROLLBACK_HEADER;
			mScroller.startScroll(0, height, 0, -1 * height, SCROLL_DURATION);
		} else if (height > mHeaderViewHeight || !mPullRefreshing) {
			mScrollBack = SCROLLBACK_HEADER;
			mScroller.startScroll(0, height, 0, -(height - mHeaderViewHeight),
					SCROLL_DURATION);
		}

		invalidate();
	}

	/**
	 * Update the visiable height of header view.
	 * 
	 * @param delta
	 */
	private void updateHeaderView(float delta) {
		int newHeight = (int) delta + mHeaderView.getVisiableHeight();
		mHeaderView.setVisiableHeight(newHeight);
		if (mEnablePullRefresh && !mPullRefreshing) {
			if (mHeaderView.getVisiableHeight() >= mHeaderViewHeight) {
				updateHeaderByState(RELEASE_TO_LOAD);
			} else {
				updateHeaderByState(PULL_TO_LOAD);
			}
		}
	}

	/**
	 * Enable or disable the ability of pull to refresh.
	 * 
	 * @param enable
	 */
	private void setPullRefreshEnable(boolean enable) {
		mEnablePullRefresh = enable;
		if (!mEnablePullRefresh) { // disable, hide the content
			mHeaderView.setVisibility(View.INVISIBLE);
		} else {
			mHeaderView.setVisibility(View.VISIBLE);
		}
	}
	
	/**
	 * Start refresh
	 */
	private void startRefresh() {
		if(mPullRefreshing) {
			//In the process of preventing refresh again when it was refreshing. 
			return;
		}
		updateHeaderByState(LOADING);
		if (mOnRefreshListener != null) {
			mOnRefreshListener.onRefresh();
		}
		mPullRefreshing = true;
	}
	
	/**
	 *  Update header view by state.
	 *
	 * @param state the new state
	 */
	public void updateHeaderByState(int state) {
		if (state == mState) {
			return ;
		}
		
		if (state == LOADING) {	
			mHeaderView.startArrowAnimation(null);
			mHeaderView.setArrowVisibility(View.GONE);
			mHeaderView.setProgressVisibility(View.VISIBLE);
			
		} else {	
			mHeaderView.setArrowVisibility(View.VISIBLE);
			mHeaderView.setProgressVisibility(View.GONE);
		}
		
		switch(state){
			case PULL_TO_LOAD:
				if (mState == RELEASE_TO_LOAD) {
					mHeaderView.startArrowAnimation(mRotateDownAnim);
				}
				if (mState == LOADING) {
					mHeaderView.startArrowAnimation(null);
				}
				mHeaderView.setTitleText(R.string.pull_view_pull_to_refresh);
				mHeaderView.setLabelText(getResources().getText(R.string.pull_view_refresh_time) + " " + mLastRefreshTime);
				break;
			case RELEASE_TO_LOAD:
				if (mState != RELEASE_TO_LOAD) {
					mHeaderView.startArrowAnimation(mRotateUpAnim);
					mHeaderView.setTitleText(R.string.pull_view_release_to_refresh);
				}
				break;
			case LOADING:
				mHeaderView.setTitleText(R.string.pull_view_refreshing);
				break;
				default:
			}
		
		mState = state;
	}
}
