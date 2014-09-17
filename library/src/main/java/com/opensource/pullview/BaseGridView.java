/*
 * Copyright (C) 2014. The Android Open Source Project.
 *
 *         yinglovezhuzhu@gmail.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.opensource.pullview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;

/**
 * Usage
 *
 * @author yinglovezhuzhu@gmail.com
 */
public class BaseGridView extends LinearLayout {

    /**
     * The grid view.
     */
    protected GridView mGridView = null;

    /**
     * The header content layout.
     */
    private LinearLayout mHeaderLayout = null;

    /**
     * The footer content layout.
     */
    private LinearLayout mFooterLayout = null;

    /**
     * Instantiates a new ab grid view.
     *
     * @param context the context
     */
    public BaseGridView(Context context) {
        super(context);
    }


    /**
     * Instantiates a new ab grid view.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public BaseGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setOrientation(LinearLayout.VERTICAL);

        mHeaderLayout = new LinearLayout(context);
        mHeaderLayout.setOrientation(LinearLayout.VERTICAL);
        LayoutParams headerLp = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        addView(mHeaderLayout, headerLp);


        mGridView = new GridView(context);
        LayoutParams gridViewLp = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        gridViewLp.weight = 1;
        mGridView.setLayoutParams(gridViewLp);
        addView(mGridView);

        mFooterLayout = new LinearLayout(context);
        mFooterLayout.setOrientation(LinearLayout.VERTICAL);
        LayoutParams footerLp = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        addView(mFooterLayout, footerLp);
    }

    /**
     * Adds the header view.
     *
     * @param v the v
     */
    public void addHeaderView(View v) {
        LayoutParams headerContentLp = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        mHeaderLayout.addView(v, mHeaderLayout.getChildCount(), headerContentLp);
    }

    /**
     * Adds the footer view.
     *
     * @param v the v
     */
    public void addFooterView(View v) {
        LayoutParams footerContentLp = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        footerContentLp.topMargin = 2;
        mFooterLayout.addView(v, 0, footerContentLp);
    }

    /**
     * Gets the grid view.
     *
     * @return the grid view
     */
    public GridView getGridView() {
        return mGridView;
    }


    /**
     * Sets the grid view.
     *
     * @param mGridView the new grid view
     */
    public void setGridView(GridView mGridView) {
        this.mGridView = mGridView;
    }

}
