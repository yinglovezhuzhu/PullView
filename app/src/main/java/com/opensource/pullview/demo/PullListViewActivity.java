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
package com.opensource.pullview.demo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;

import com.opensource.pullview.OnLoadMoreListener;
import com.opensource.pullview.OnRefreshListener;
import com.opensource.pullview.PullListView;
import com.opensource.pullview.utils.DateUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Usage 
 * 
 * @author yinglovezhuzhu@gmail.com
 */
public class PullListViewActivity extends Activity {
	
	private static final String TAG = "PullListViewActivity";

    private static final int MSG_REFLESH_DONE = 0x100;
	private static final int MSG_LOAD_DONE = 0x101;


	private PullListView mListView;
	private MainHandler mHandler = new MainHandler();
	private ArrayAdapter<String> mAdapter;
	private List<String> mDatas = new ArrayList<String>();
	
	@SuppressLint("HandlerLeak")
	private class MainHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
                case MSG_REFLESH_DONE:
                    if(null != mListView) {
                        if(null != mAdapter) {
                            mAdapter.clear();
                            for(int i = 0; i < 30; i++) {
                                mDatas.add("Item " + mDatas.size());
                            }
                            mAdapter.notifyDataSetChanged();
                        }
                        mListView.refreshCompleted();
                        mListView.loadMoreCompleted(mDatas.size() < 50);
                        Log.e(TAG, "Refresh finished +=====================^_^");
                    }
                    break;
                case MSG_LOAD_DONE:
                    if(null != mListView) {
                        if(null != mAdapter) {
                            for(int i = 0; i < 30; i++) {
                                mDatas.add("Item " + mDatas.size());
                            }
                            mAdapter.notifyDataSetChanged();
                        }
                        mListView.refreshCompleted();
                        mListView.loadMoreCompleted(mDatas.size() < 50);
                        Log.e(TAG, "Load more finished +=====================^_^");
                    }
                    break;
                default:
                    break;
			}
			super.handleMessage(msg);
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_pull_listview);
		
		mListView = (PullListView) findViewById(R.id.pull_list_view);
		mListView.setLoadMode(PullListView.LoadMode.PULL_TO_LOAD);
		mListView.setHeaderLabelVisibility(View.VISIBLE);
		mListView.setLastRefreshTime(DateUtil.getYesterdayDate(getString(R.string.pull_view_date_format)));
		
//		ImageView iv = new ImageView(this);
//		iv.setImageResource(com.opensource.pullview.R.drawable.ic_launcher);
//		mListView.addHeaderView(iv, null, false);
//		ImageView iv2 = new ImageView(this);
//		iv2.setImageResource(com.opensource.pullview.R.drawable.ic_launcher);
//		mListView.addHeaderView(iv2);
//		ImageView iv3 = new ImageView(this);
//		iv3.setImageResource(com.opensource.pullview.R.drawable.ic_launcher);
//		mListView.addHeaderView(iv3);
		
		mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mDatas);
		mListView.setAdapter(mAdapter);

        mListView.setEnableOverScroll(true);
		
		mListView.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				mHandler.sendEmptyMessageDelayed(MSG_REFLESH_DONE, 5000);
				Log.e(TAG, "Start refresh+=====================^_^");
			}
        });
		
		mListView.setOnLoadMoreListener(new OnLoadMoreListener() {

			@Override
			public void onLoadMore() {
				mHandler.sendEmptyMessageDelayed(MSG_LOAD_DONE, 5000);
				Log.e(TAG, "Start load more+=====================^_^");
			}
        });
		
		mListView.onFootLoading("正在加载");
		mHandler.sendEmptyMessageDelayed(MSG_LOAD_DONE, 3000);
	}
}
