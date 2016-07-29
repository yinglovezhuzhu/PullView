/*
 * Copyright (C)2016. The Android Open Source Project.
 *
 *          yinglovezhuzhu@gmail.com
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

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.opensource.pullview.OnLoadMoreListener;
import com.opensource.pullview.OnRefreshListener;
import com.opensource.pullview.PullListView2;

/**
 * Usage 
 * 
 * @author yinglovezhuzhu@gmail.com
 */
public class PullListView2Activity extends Activity {
	
	private static final String TAG = "PullListViewActivity";

    private static final int MSG_REFLESH_DONE = 0x100;
    private static final int MSG_REFLESH_ERROR = 0x101;
	private static final int MSG_LOAD_DONE = 0x102;


	private PullListView2 mListView;
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
                        boolean loadMoreable = mDatas.size() < 50;
                        mListView.loadMoreCompleted(loadMoreable);
                        Log.e(TAG, "Refresh finished +=====================^_^");
                    }
                    break;
                case MSG_REFLESH_ERROR:
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
                        boolean loadMoreable = mDatas.size() < 50;
                        mListView.loadMoreCompleted(loadMoreable);
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
		
		setContentView(R.layout.activity_pull_listview2);
		
		mListView = (PullListView2) findViewById(R.id.pull_list_view2);
		mListView.setLoadMode(PullListView2.LoadMode.PULL_TO_LOAD);
//		mListView.setLoadMode(PullListView2.LoadMode.AUTO_LOAD);
        mListView.setEnableOverScroll(true);

        mListView.setHeaderBackgroundImage(R.drawable.background_image);

//        View view = View.inflate(this, R.layout.layout_header_content, null);
////        mListView.setHeaderContentView(view);
//        mListView.setHeaderBackgroundView(view);

//        mListView.setHeaderBackgroundView(R.layout.layout_background_view);
//        View view = View.inflate(this, R.layout.layout_header_content, null);
//        mListView.setHeaderContentView(R.layout.layout_header_content);

        mListView.setHeaderContentView(R.layout.layout_header_content);

        mListView.setHeaderTopView(R.layout.layout_header_top);


		mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mDatas);
		mListView.setAdapter(mAdapter);


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
		mHandler.sendEmptyMessageDelayed(MSG_LOAD_DONE, 1000);
	}
}
