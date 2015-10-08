package com.opensource.pullview.demo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.opensource.pullview.OnRefreshListener;
import com.opensource.pullview.PullExpandableListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yinglovezhuzhu@gmail.com on 2015/10/8.
 */
public class PullExpandableListViewActivity extends Activity {

    private static final String TAG = "PullExpandableListViewActivity";

    private static final int MSG_REFLESH_DONE = 0x100;
    private static final int MSG_REFLESH_ERROR = 0x101;
    private static final int MSG_LOAD_DONE = 0x102;


    private PullExpandableListView mPullExpandableListView;
    private ExAdapter mAdapter;

    private MainHandler mHandler = new MainHandler();

    @SuppressLint("HandlerLeak")
    private class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REFLESH_DONE:
                    if(null != mPullExpandableListView) {
                        if(null != mAdapter) {
                            mAdapter.clear();
                            mAdapter.initData();
                            mAdapter.notifyDataSetChanged();
                        }
                        mPullExpandableListView.refreshCompleted();
//                        mPullExpandableListView.loadMoreCompleted(mDatas.size() < 50);
                        Log.e(TAG, "Refresh finished +=====================^_^");
                    }
                    break;
//                case MSG_LOAD_DONE:
//                    if(null != mPullExpandableListView) {
//                        if(null != mAdapter) {
//                            mAdapter.notifyDataSetChanged();
//                        }
//                        mPullExpandableListView.refreshCompleted();
//                        mPullExpandableListView.loadMoreCompleted(mDatas.size() < 50);
//                        Log.e(TAG, "Load more finished +=====================^_^");
//                    }
//                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pull_expandable_listview);

        mPullExpandableListView = (PullExpandableListView) findViewById(R.id.pull_expandable_listview);
        mAdapter = new ExAdapter(this);
        mPullExpandableListView.setAdapter(mAdapter);

        mPullExpandableListView.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
                mHandler.sendEmptyMessageDelayed(MSG_REFLESH_DONE, 5000);
                Log.e(TAG, "Start refresh+=====================^_^");
            }
        });

    }

    private class ExAdapter extends BaseExpandableListAdapter {

        private Context mmContext;
        private List<String> mmGroupData = new ArrayList<>();
        private List<List<String>> mmChildData = new ArrayList<>();

        public ExAdapter(Context context) {
            this.mmContext = context;
            initData();
        }

        public void initData() {
            for(int i = 0; i < 10; i++) {
                mmGroupData.add("Group--" + i);
                ArrayList<String> childs = new ArrayList<>();
                for(int j = 0; j < 20; j++) {
                    childs.add("Child--" + i + "<>" + j);
                }
                mmChildData.add(childs);
            }
        }

        public void clear() {
            mmGroupData.clear();
            mmChildData.clear();
//            notifyDataSetChanged();
        }

        @Override
        public int getGroupCount() {
            return mmGroupData.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return mmChildData.get(groupPosition).size();
        }

        @Override
        public String getGroup(int groupPosition) {
            return mmGroupData.get(groupPosition);
        }

        @Override
        public String getChild(int groupPosition, int childPosition) {
            return mmChildData.get(groupPosition).get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            TextView tv = null;
            if(null == convertView) {
                tv = new TextView(mmContext);
                tv.setPadding(10, 10, 10, 10);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                convertView = tv;
            } else {
                tv = (TextView) convertView;
            }
            tv.setText(mmGroupData.get(groupPosition));
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            TextView tv = null;
            if(null == convertView) {
                tv = new TextView(mmContext);
                tv.setPadding(10, 10, 10, 10);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                convertView = tv;
            } else {
                tv = (TextView) convertView;
            }
            tv.setText(mmChildData.get(groupPosition).get(childPosition));
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }
    }
}
