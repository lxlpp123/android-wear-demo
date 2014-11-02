package com.livefront.android_wear_demo.adapter;

import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.livefront.android_wear_demo.R;
import com.livefront.android_wear_demo.activity.MainActivity;

import java.util.List;

public class DemoItemAdapter extends WearableListView.Adapter {

    private List<MainActivity.DemoItem> mData;

    // Provide a reference to the type of views you're using
    public static class ItemViewHolder extends WearableListView.ViewHolder {
        private TextView text;
        public ItemViewHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.name);
        }
    }

    public DemoItemAdapter(List<MainActivity.DemoItem> demoItems) {
        mData = demoItems;
    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new ItemViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.view_demo_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder viewHolder, int position) {
        MainActivity.DemoItem item = mData.get(position);

        ItemViewHolder itemViewHolder = (ItemViewHolder) viewHolder;
        itemViewHolder.text.setText(item.getName());
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}
