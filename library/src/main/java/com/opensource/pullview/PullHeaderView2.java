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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.opensource.pullview.utils.ViewUtil;

/**
 * Usage The header view.
 *
 * @author yinglovezhuzhu@gmail.com
 */
public class PullHeaderView2 extends LinearLayout {

    private LinearLayout mTopContent;

    private LinearLayout mBackgroundContent;

    private LinearLayout mViewContent;

    private LinearLayout mStateContent;

    private ImageView mIvBg;

    /**
     * The head content height.
     */
    private int mViewHeight;
    /**
     * The state view content height *
     */
    private int mStateViewHeight;
    /**
     * The visible height *
     */
    private int mVisibleHeight;

    /**
     * Instantiates a new ab list view header.
     *
     * @param context the context
     */
    public PullHeaderView2(Context context) {
        super(context);
        initView(context);
    }

    /**
     * Instantiates a new ab list view header.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public PullHeaderView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    /**
     * Inits the view.
     *
     * @param context the context
     */
    private void initView(Context context) {

        View.inflate(context, R.layout.layout_pullview_header2, this);

        mTopContent = (LinearLayout) findViewById(R.id.ll_pull_listview_header_top);
        mBackgroundContent = (LinearLayout) findViewById(R.id.ll_pull_listview_header_bg_content);
        mViewContent = (LinearLayout) findViewById(R.id.ll_pull_listview_header_view_content);
        mStateContent = (LinearLayout) findViewById(R.id.ll_pull_listview_header_state_content);
        mIvBg = (ImageView) findViewById(R.id.iv_pull_listview_header_bg);

        //这里进行对背景图片的ImageView进行宽度限制，是为了配合adjustViewBounds属性
        ViewGroup.LayoutParams bgLayoutParams = mIvBg.getLayoutParams();
        if (null == bgLayoutParams) {
            bgLayoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        bgLayoutParams.width = context.getResources().getDisplayMetrics().widthPixels;
        mIvBg.setLayoutParams(bgLayoutParams);

        ViewUtil.measureView(mViewContent);
        mVisibleHeight = mViewContent.getMeasuredHeight();

        ViewUtil.measureView(this);
        mViewHeight = this.getMeasuredHeight();

        ViewUtil.measureView(mStateContent);
        mStateViewHeight = mStateContent.getMeasuredHeight();
        mStateContent.setPadding(0, mViewHeight - mVisibleHeight - mStateViewHeight, 0, 0);

    }

    /**
     * Sets state view padding
     *
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    public void setStateContentPadding(int left, int top, int right, int bottom) {
        mStateContent.setPadding(left, top, right, bottom);
    }

    /**
     * Sets state view visibility
     *
     * @param visibility
     */
    public void setStateContentVisibility(int visibility) {
        mStateContent.setVisibility(visibility);
    }

    /**
     * Gets the header height.
     *
     * @return the header height
     */
    public int getViewHeight() {
        return mViewHeight;
    }

    /**
     * Gets visible height.
     *
     * @return
     */
    public int getVisibleHeight() {
        return mVisibleHeight;
    }

    /**
     * Gets state view height
     *
     * @return
     */
    public int getStateViewHeight() {
        return mStateViewHeight;
    }

    /**
     * Gets ImageView to set background image.
     *
     * @return
     */
    public ImageView getBackgroudImageView() {
        return mIvBg;
    }

    /**
     * Sets background image to ImageView
     *
     * @param resId
     */
    public void setBackgroudImage(int resId) {
        mIvBg.setImageResource(resId);
    }

    /**
     * Sets background image to ImageView
     *
     * @param bm
     */
    public void setBackgroundImage(Bitmap bm) {
        mIvBg.setImageBitmap(bm);
    }

    /**
     * Sets background view<br/><br/>
     * <p/>
     * If you use this method to set background view, it will replace default background ImageView.
     *
     * @param backgroundView
     */
    public void setBackgroundView(View backgroundView) {
        if (null == backgroundView) {
            return;
        }
        mBackgroundContent.removeAllViews();
        mBackgroundContent.addView(backgroundView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    /**
     * Sets background view<br/><br/>
     * <p/>
     * If you use this method to set background view, it will replace default background ImageView.
     *
     * @param layoutId
     */
    public void setBackgroudView(int layoutId) {
        if (0 == layoutId) {
            return;
        }
        mBackgroundContent.removeAllViews();
        View.inflate(getContext(), layoutId, mBackgroundContent);
    }

}
