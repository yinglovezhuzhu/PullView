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
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.opensource.pullview.utils.DateUtil;
import com.opensource.pullview.utils.ViewUtil;

/**
 * Usage The header view.
 * 
 * @author yinglovezhuzhu@gmail.com
 */
public class PullHeaderView3 extends LinearLayout {

	/** The rotate anim duration. */
	private final int ROTATE_ANIM_DURATION = 180;
	
	/** The Constant STATE_NORMAL. */
	public final static int STATE_NORMAL = 0;
	
	/** The Constant STATE_READY. */
	public final static int STATE_READY = 1;
	
	/** The Constant STATE_REFRESHING. */
	public final static int STATE_REFRESHING = 2;
	
	/** The header view. */
	private LinearLayout mHeaderView;
	
	/** The arrow image view. */
	private ImageView mArrowImageView;
	
	/** The header progress bar. */
	private ProgressBar mHeaderProgress;
	
	/** The tips textview. */
	private TextView mTipsTextview;
	
	/** The header time view. */
	private TextView mTimeTextView;
	
	/** The m state. */
	private int mState = -1;

	/** The m rotate up anim. */
	private Animation mRotateUpAnim;
	
	/** The m rotate down anim. */
	private Animation mRotateDownAnim;
	
	/** Last refresh time. */
	private String mLastRefreshTime = null;
	
	/** The head content height. */
	private int mHeaderViewHeight;

	/**
	 * Instantiates a new ab list view header.
	 *
	 * @param context the context
	 */
	public PullHeaderView3(Context context) {
		super(context);
		initView(context);
	}

	/**
	 * Instantiates a new ab list view header.
	 *
	 * @param context the context
	 * @param attrs the attrs
	 */
	public PullHeaderView3(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	/**
	 * Inits the view.
	 *
	 * @param context the context
	 */
	private void initView(Context context) {
		
		mHeaderView = new LinearLayout(context);
		mHeaderView.setOrientation(LinearLayout.HORIZONTAL);
		//setBackgroundColor(Color.rgb(225, 225,225));
		mHeaderView.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM); 
		mHeaderView.setPadding(0, 5, 0, 5);
		
		FrameLayout headImage =  new FrameLayout(context);
		mArrowImageView = new ImageView(context);
		mArrowImageView.setImageResource(R.drawable.pullview_down_arrow);
		
		//style="?android:attr/progressBarStyleSmall" default style
		mHeaderProgress = new ProgressBar(context,null,android.R.attr.progressBarStyle);
		mHeaderProgress.setVisibility(View.GONE);
		
		//Arrow icon and progress
		LayoutParams iconLp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		iconLp.gravity = Gravity.CENTER;
		iconLp.width = 50;
		iconLp.height = 50;
		headImage.addView(mArrowImageView,iconLp);
		headImage.addView(mHeaderProgress,iconLp);
		
		//Header text
		LinearLayout headTextLayout  = new LinearLayout(context);
		mTipsTextview = new TextView(context);
		mTimeTextView = new TextView(context);
		headTextLayout.setOrientation(LinearLayout.VERTICAL);
		headTextLayout.setGravity(Gravity.BOTTOM|Gravity.LEFT);
		headTextLayout.setPadding(12,0,0,0);
		LayoutParams tipLp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		headTextLayout.addView(mTipsTextview,tipLp);
		headTextLayout.addView(mTimeTextView,tipLp);
		mTipsTextview.setTextColor(Color.rgb(107, 107, 107));
		mTimeTextView.setTextColor(Color.rgb(107, 107, 107));
		mTipsTextview.setTextSize(15);
		mTimeTextView.setTextSize(14);
		
		LayoutParams contentLp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		contentLp.gravity = Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL;
		contentLp.bottomMargin = 5;
		contentLp.topMargin = 5;
		
		LinearLayout headerLayout = new LinearLayout(context);
		headerLayout.setOrientation(LinearLayout.HORIZONTAL);
		headerLayout.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM); 
		
		headerLayout.addView(headImage,contentLp);
		headerLayout.addView(headTextLayout,contentLp);
		
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		lp.gravity = Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL;
		
		mHeaderView.addView(headerLayout,lp);
		
		this.addView(mHeaderView,lp);
		//Get height of this header view.
		ViewUtil.measureView(this);
		mHeaderViewHeight = this.getMeasuredHeight();
		//Hide this header view.
		mHeaderView.setPadding(0, -1 * mHeaderViewHeight, 0, 0);
		
		mRotateUpAnim = new RotateAnimation(0.0f, -180.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		mRotateUpAnim.setDuration(ROTATE_ANIM_DURATION);
		mRotateUpAnim.setFillAfter(true);
		mRotateDownAnim = new RotateAnimation(-180.0f, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		mRotateDownAnim.setDuration(ROTATE_ANIM_DURATION);
		mRotateDownAnim.setFillAfter(true);
		
		setState(STATE_NORMAL);
	}

	/**
	 * Sets the state.
	 *
	 * @param state the new state
	 */
	public void setState(int state) {
		if (state == mState) return ;
		
		if (state == STATE_REFRESHING) {	
			mArrowImageView.clearAnimation();
			mArrowImageView.setVisibility(View.INVISIBLE);
			mHeaderProgress.setVisibility(View.VISIBLE);
		} else {	
			mArrowImageView.setVisibility(View.VISIBLE);
			mHeaderProgress.setVisibility(View.INVISIBLE);
		}
		
		switch(state){
			case STATE_NORMAL:
				if (mState == STATE_READY) {
					mArrowImageView.startAnimation(mRotateDownAnim);
				}
				if (mState == STATE_REFRESHING) {
					mArrowImageView.clearAnimation();
				}
				mTipsTextview.setText(R.string.pull_view_pull_to_refresh);
				
				if(mLastRefreshTime==null){
					mLastRefreshTime = DateUtil.getSystemDate("yyyy-MM-dd HH:mm:ss");
					mTimeTextView.setText(getResources().getText(R.string.pull_view_refresh_time) + " " + mLastRefreshTime);
				}else{
					mTimeTextView.setText(getResources().getText(R.string.pull_view_refresh_time) + " " + mLastRefreshTime);
				}
				
				break;
			case STATE_READY:
				if (mState != STATE_READY) {
					mArrowImageView.clearAnimation();
					mArrowImageView.startAnimation(mRotateUpAnim);
					mTipsTextview.setText(R.string.pull_view_release_to_refresh);
					mTimeTextView.setText(getResources().getText(R.string.pull_view_refresh_time) + " " + mLastRefreshTime);
					mLastRefreshTime = DateUtil.getSystemDate("yyyy-MM-dd HH:mm:ss");
					
				}
				break;
			case STATE_REFRESHING:
				mTipsTextview.setText(R.string.pull_view_refreshing);
				mTimeTextView.setText(getResources().getText(R.string.pull_view_refresh_time) + " " + mLastRefreshTime);
				break;
				default:
			}
		
		mState = state;
	}
	
	/**
	 * Sets the visiable height.
	 *
	 * @param height the new visiable height
	 */
	public void setVisiableHeight(int height) {
		if (height < 0) height = 0;
		LayoutParams lp = (LayoutParams) mHeaderView.getLayoutParams();
		lp.height = height;
		mHeaderView.setLayoutParams(lp);
	}

	/**
	 * Gets the visiable height.
	 *
	 * @return the visiable height
	 */
	public int getVisiableHeight() {
		LayoutParams lp = (LayoutParams)mHeaderView.getLayoutParams();
		return lp.height;
	}

	/**
	 * 描述：获取HeaderView.
	 *
	 * @return the header view
	 */
	public LinearLayout getHeaderView() {
		return mHeaderView;
	}
	
	/**
	 * set last refresh time.
	 *
	 * @param time the new refresh time
	 */
	public void setRefreshTime(String time) {
		mTimeTextView.setText(time);
	}

	/**
	 * Gets the header height.
	 *
	 * @return the header height
	 */
	public int getHeaderHeight() {
		return mHeaderViewHeight;
	}
	
	/**
	 * 
	 * Set Text Color
	 * @param color
	 * @throws 
	 */
	public void setTextColor(int color){
		mTipsTextview.setTextColor(color);
		mTimeTextView.setTextColor(color);
	}
	
	/**
	 * 
	 * Set Background color
	 * @param color
	 * @throws 
	 */
	public void setBackgroundColor(int color){
		mHeaderView.setBackgroundColor(color);
	}

	/**
	 * 
	 * Get progress
	 * @return
	 * @throws 
	 */
	public ProgressBar getHeaderProgress() {
		return mHeaderProgress;
	}

	/**
	 * 
	 * Set progress drawable
	 * @return
	 * @throws 
	 */
	public void setHeaderProgressBarDrawable(Drawable indeterminateDrawable) {
		mHeaderProgress.setIndeterminateDrawable(indeterminateDrawable);
	}
}
