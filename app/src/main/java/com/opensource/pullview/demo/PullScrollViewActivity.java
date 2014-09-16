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
package com.opensource.pullview.demo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.opensource.pullview.OnRefreshListener;
import com.opensource.pullview.PullScrollView;

/**
 * Usage 
 * 
 * @author yinglovezhuzhu@gmail.com
 */
public class PullScrollViewActivity extends Activity {
	
	private static final int MSG_REFRESH_DONE = 0x100;
	
	private PullScrollView mPullScrollView;
	private MainHandler mHandler = new MainHandler();
	
	@SuppressLint("HandlerLeak")
	private class MainHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_REFRESH_DONE:
				if(null != mPullScrollView) {
					mPullScrollView.refreshCompleted();
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
		
		setContentView(R.layout.activity_pull_scroll_view);
		
		mPullScrollView = (PullScrollView) findViewById(R.id.pull_scroll_view);
		mPullScrollView.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				mHandler.sendEmptyMessageDelayed(MSG_REFRESH_DONE, 20000);
			}

        });
	}

}
