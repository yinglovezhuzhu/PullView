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
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.opensource.pullview.utils.ViewUtil;

/**
 * Usage The header view.
 * 
 * @author yinglovezhuzhu@gmail.com
 */
public class PullHeaderView extends LinearLayout {

	/** The header view. */
	private LinearLayout mHeaderView;
	
	/** The arrow image view. */
	private ImageView mArrowImageView;
	
	/** The header progress bar. */
	private ProgressBar mProgress;
	
	/** The tips textview. */
	private TextView mTvTitle;
	
	/** The header time view. */
	private TextView mTvLabel;
	
	/** The head content height. */
	private int mHeaderViewHeight;

	/**
	 * Instantiates a new ab list view header.
	 *
	 * @param context the context
	 */
	public PullHeaderView(Context context) {
		super(context);
		initView(context);
	}

	/**
	 * Instantiates a new ab list view header.
	 *
	 * @param context the context
	 * @param attrs the attrs
	 */
	public PullHeaderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	/**
	 * Inits the view.
	 *
	 * @param context the context
	 */
	private void initView(Context context) {
		
//		mHeaderView = new LinearLayout(context);
//		mHeaderView.setOrientation(LinearLayout.HORIZONTAL);
//		//setBackgroundColor(Color.rgb(225, 225,225));
//		mHeaderView.setGravity(Gravity.CENTER);
//		mHeaderView.setPadding(0, 5, 0, 5);
//
//		FrameLayout headImage =  new FrameLayout(context);
//		mArrowImageView = new ImageView(context);
//		mArrowImageView.setImageResource(R.drawable.pullview_down_arrow);
//
//		//style="?android:attr/progressBarStyleSmall" default style
//		mProgress = new ProgressBar(context,null,android.R.attr.progressBarStyle);
//		mProgress.setVisibility(View.GONE);
//
//		//Arrow icon and progress
//		LayoutParams iconLp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//		iconLp.gravity = Gravity.CENTER;
//		iconLp.width = 50;
//		iconLp.height = 50;
//		headImage.addView(mArrowImageView,iconLp);
//		headImage.addView(mProgress,iconLp);
//
//		//Header text
//		LinearLayout headTextLayout  = new LinearLayout(context);
//		mTvTitle = new TextView(context);
//		mTvLabel = new TextView(context);
//		headTextLayout.setOrientation(LinearLayout.VERTICAL);
//		headTextLayout.setGravity(Gravity.CENTER_VERTICAL|Gravity.LEFT);
//		headTextLayout.setPadding(12,0,0,0);
//		LayoutParams textLp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//		headTextLayout.addView(mTvTitle,textLp);
//		headTextLayout.addView(mTvLabel,textLp);
//		mTvTitle.setTextColor(Color.argb(255, 50, 50, 50));
//		mTvLabel.setTextColor(Color.argb(255, 255, 110, 0));
//		mTvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
//		mTvLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
//
//		LayoutParams contentLp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//		contentLp.gravity = Gravity.CENTER;
//		contentLp.bottomMargin = 5;
//		contentLp.topMargin = 5;
//
//		LinearLayout headerLayout = new LinearLayout(context);
//		headerLayout.setOrientation(LinearLayout.HORIZONTAL);
//		headerLayout.setGravity(Gravity.CENTER);
//
//		headerLayout.addView(headImage,contentLp);
//		headerLayout.addView(headTextLayout,contentLp);
//
//		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
//		lp.gravity = Gravity.CENTER;
//
//		mHeaderView.addView(headerLayout,lp);
//
//		this.addView(mHeaderView,lp);

        View.inflate(context, R.layout.layout_pullview_header, this);

        mArrowImageView = (ImageView) findViewById(R.id.iv_pullview_header_arrow);
        mProgress = (ProgressBar) findViewById(R.id.pb_pullview_header_progress);
        mTvTitle = (TextView) findViewById(R.id.tv_pullview_header_title);
        mTvLabel = (TextView) findViewById(R.id.tv_pullview_header_label);

        //Get height of this header view.
		ViewUtil.measureView(this);
		mHeaderViewHeight = this.getMeasuredHeight();
		
	}

	/**
	 * Set arrow image visibility
	 * @param visibility
	 */
	public void setArrowVisibility(int visibility) {
		mArrowImageView.setVisibility(visibility);
	}
	
	/**
	 * Set progress visibility
	 * @param visibility
	 */
	public void setProgressVisibility(int visibility) {
		mProgress.setVisibility(visibility);
	}
	
	/**
	 * Set title text visibility.
	 * @param visibility
	 */
	public void setTitileVisibility(int visibility) {
		mTvTitle.setVisibility(visibility);
	}
	
	/**
	 * Set title text
	 * @param text
	 */
	public void setTitleText(CharSequence text) {
		mTvTitle.setText(text);
	}
	
	/**
	 * Set titme text.
	 * @param resid
	 * @return
	 */
	public void setTitleText(int resid) {
		mTvTitle.setText(resid);
	}
	
	/**
	 * Set label text visibility<br>
	 * <p> {@link android.view.View#VISIBLE} default.
	 * @param visibility
	 */
	public void setLabelVisibility(int visibility) {
		mTvLabel.setVisibility(visibility);
	}
	
	/**
	 * Set label text.
	 * @param text
	 */
	public void setLabelText(CharSequence text) {
		mTvLabel.setText(text);
	}
	
	/**
	 * Set label text.
	 * @param resid
	 */
	public void setLabelText(int resid) {
		mTvLabel.setText(resid);
	}
	
	/**
	 * Start animation of arrow image
	 * @param animation
	 */
	public void startArrowAnimation(Animation animation) {
		mArrowImageView.clearAnimation();
		if(null != animation) {
			mArrowImageView.startAnimation(animation);
		}
	}

	/**
	 * set last refresh time.
	 *
	 * @param time the new refresh time
	 */
	public void setRefreshTime(String time) {
		mTvLabel.setText(time);
	}

	/**
	 * Gets the header height.
	 *
	 * @return the header height
	 */
	public int getViewHeight() {
		return mHeaderViewHeight;
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
	 * 
	 * Set title text color
	 * @param color
	 * @throws 
	 */
	public void setTitleTextColor(int color){
		mTvTitle.setTextColor(color);
	}
	
	/**
	 * Set label text color
	 * @param color
	 */
	public void setLabelTextColor(int color) {
		mTvLabel.setTextColor(color);
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
	public ProgressBar getProgress() {
		return mProgress;
	}

	/**
	 * 
	 * Set progress drawable
	 * @return
	 * @throws 
	 */
	public void setHeaderProgressBarDrawable(Drawable indeterminateDrawable) {
		mProgress.setIndeterminateDrawable(indeterminateDrawable);
	}
}
