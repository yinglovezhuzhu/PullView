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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.opensource.pullview.OnLoadMoreListener;
import com.opensource.pullview.OnRefreshListener;
import com.opensource.pullview.PullListView;
import com.opensource.pullview.utils.DateUtil;

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

        mListView.setEnableOverScroll(false);
		
//		mListView.setOnRefreshListener(new OnRefreshListener() {
//
//			@Override
//			public void onRefresh() {
//				mHandler.sendEmptyMessageDelayed(MSG_REFLESH_DONE, 5000);
//				Log.e(TAG, "Start refresh+=====================^_^");
//			}
//        });
//
//		mListView.setOnLoadMoreListener(new OnLoadMoreListener() {
//
//			@Override
//			public void onLoadMore() {
//				mHandler.sendEmptyMessageDelayed(MSG_LOAD_DONE, 5000);
//				Log.e(TAG, "Start load more+=====================^_^");
//			}
//        });
		
		mListView.onFootLoading("正在加载");
		mHandler.sendEmptyMessageDelayed(MSG_LOAD_DONE, 3000);
	}


	private class PLAdapter extends BaseAdapter {

		private Context mmContext;
		private List<String> mDatas = new ArrayList<>();

		private void addAll(Collection<String> datas) {
			if(null == datas || datas.isEmpty()) {
				return;
			}
			mDatas.addAll(datas);
			notifyDataSetChanged();
		}


		@Override
		public int getCount() {
			int count = 0;
			int size = 0;
			if(mDatas.size() < 3) {
				count = mDatas.size();
			} else if(mDatas.size() < 8) {
				count = 3;
				size = mDatas.size() - count;
				count += size % 2 == 0 ? size / 2 : size / 2 + 1;
			} else if(mDatas.size() < 17) {
				count = 7;
				size = mDatas.size() - count;
				count += size % 3 == 0 ? size / 3 : size / 3 + 1;
			} else {
				count = 16;
				size = mDatas.size() - count;
				count += size % 4 == 0 ? size / 4 : size / 4 + 1;
			}
			return count;
		}

		@Override
		public String [] getItem(int position) {
			String [] item;
			if(mDatas.size() < 3) {
				item = new String [] {mDatas.get(position), };
			} else if(mDatas.size() < 8) {
				item = new String[2];
				int index = 3;
				for(int i = 0; i < 2; i++) {
					item[i] = mDatas.get(index);
					index++;
					if(index >= mDatas.size()) {
						break;
					}
				}
			} else if(mDatas.size() < 17) {
				item = new String[3];
			} else {
				item = new String[4];
			}
			return item;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return null;
		}
	}
}
