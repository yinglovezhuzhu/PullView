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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;

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
		List<String> datas = new ArrayList<>();
		for(int i = 0; i < 20; i++) {
			datas.add("Datas-->" + i);
		}

		PLAdapter adapter = new PLAdapter(this);
		adapter.addAll(datas);
		mListView.setAdapter(adapter);

//		mListView.setAdapter(mAdapter);

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


	private class PLAdapter extends BaseAdapter {

		private Context mmContext;
		private List<String> mDatas = new ArrayList<>();

		private View mItemView1;
		private View mItemView2;
		private View mItemView3;
		private View mItemView4;
		private View mItemView5;

		private int dmWidth;

		public PLAdapter(Context context) {
			this.mmContext = context;
			dmWidth = mmContext.getResources().getDisplayMetrics().widthPixels;
		}

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
			if(mDatas.size() < 3) { // 0~2 每行一张图片（二、三行有广告）
				item = new String [] {mDatas.get(position), };
			} else if(mDatas.size() < 8) { // 3~4 每行两张图片
				item = new String[2];
				int startIndex = (position - 3) * 2 + 3;
				for(int i = startIndex; i < startIndex + 2; i++) {
					item[i - startIndex] = mDatas.get(startIndex);
				}
			} else if(mDatas.size() < 17) {// 5~7 每行三张图片
				item = new String[3];
				int startIndex = (position - 5) * 3 + 7;
				for(int i = startIndex; i < startIndex + 3; i++) {
					item[i - startIndex] = mDatas.get(startIndex);
				}
			} else { // 8~无穷每行四张图片
				item = new String[4];
				int startIndex = (position - 8) * 4 + 16;
				for(int i = startIndex; i < startIndex + 3; i++) {
					item[i - startIndex] = mDatas.get(startIndex);
				}
			}
			return item;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(position == 0) { // 0行一张图片
				if(null == convertView || convertView.getId() != 1) {
					convertView = View.inflate(mmContext, R.layout.item_one_image_in_row, null);
					convertView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dmWidth));
					convertView.setId(1);
				}
//				if(null == mItemView1) {
//					mItemView1 = View.inflate(mmContext, R.layout.item_one_image_in_row, null);
//					mItemView1.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dmWidth));
//				}
//				convertView = mItemView1;
			} else if(position < 3) { // 1~2行一张图片+广告
				if(null == convertView || convertView.getId() != 2) {
					convertView = View.inflate(mmContext, R.layout.item_one_image_in_row_with_add, null);
					convertView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dmWidth / 4 * 3));
					convertView.setId(2);
				}
//				if(null == mItemView2) {
//					mItemView2 = View.inflate(mmContext, R.layout.item_one_image_in_row_with_add, null);
//					mItemView2.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dmWidth / 4 * 3));
//				}
//				convertView = mItemView2;

			} else if(position < 5) { // 3~4行两张图片
				if(null == convertView || convertView.getId() != 3) {
					convertView = View.inflate(mmContext, R.layout.item_two_image_in_row, null);
					convertView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dmWidth / 2));
					convertView.setId(3);
				}
//				if(null == mItemView3) {
//					mItemView3 = View.inflate(mmContext, R.layout.item_two_image_in_row, null);
//					mItemView3.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dmWidth / 2));
//				}
//				convertView = mItemView3;

			} else if(position < 8) { /// 5~7行三张图片
				if(null == convertView || convertView.getId() != 4) {
					convertView = View.inflate(mmContext, R.layout.item_three_image_in_row, null);
					convertView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dmWidth / 3));
					convertView.setId(4);
				}
//				if(null == mItemView4) {
//					mItemView4 = View.inflate(mmContext, R.layout.item_three_image_in_row, null);
//					mItemView4.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dmWidth / 3));
//				}
//				convertView = mItemView4;

			} else { //8~无穷行四张图片
				if(null == convertView || convertView.getId() != 5) {
					convertView = View.inflate(mmContext, R.layout.item_four_image_in_row, null);
					convertView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dmWidth / 4));
					convertView.setId(5);
				}
//				if(null == mItemView5) {
//					mItemView5 = View.inflate(mmContext, R.layout.item_four_image_in_row, null);
//					mItemView5.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dmWidth / 4));
//				}
//				convertView = mItemView5;

			}
			return convertView;
		}
	}
}
