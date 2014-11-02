package com.livefront.android_wear_demo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;

import com.livefront.android_wear_demo.R;
import com.livefront.android_wear_demo.adapter.DemoItemAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements WearableListView.ClickListener {

    private DemoItemAdapter mAdapter;
    private List<DemoItem> mData = new ArrayList<DemoItem>();
    private WearableListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupData();
        setupListView();
    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        DemoItem item = mData.get(viewHolder.getPosition());
        startActivity(item.getIntent());
    }

    @Override
    public void onTopEmptyRegionClick() {
        // For now, do nothing
    }

    public void setupData() {
        mData.add(new DemoItem(getString(R.string.demo_item_watch_view_stub),
                new Intent(this, WatchViewStubActivity.class)));
    }

    public void setupListView() {
        mListView = (WearableListView) findViewById(R.id.wearable_list);
        mAdapter = new DemoItemAdapter(mData);
        mListView.setAdapter(mAdapter);

        // Note that this is NOT setting an OnClickListener, but a ClickListener
        mListView.setClickListener(this);
    }

    public static class DemoItem {
        private String mName;
        private Intent mIntent;

        public DemoItem(String name, Intent intent) {
            mName = name;
            mIntent = intent;
        }

        public String getName() {
            return mName;
        }

        public Intent getIntent() {
            return mIntent;
        }
    }
}
