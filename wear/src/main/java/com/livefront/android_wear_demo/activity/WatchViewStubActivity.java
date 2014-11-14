package com.livefront.android_wear_demo.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.widget.TextView;

import com.livefront.android_wear_demo.R;

public class WatchViewStubActivity extends Activity {

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_view_stub);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                // If you want to update the TextView you'll have to do it here, otherwise you'll
                // get an NPE because the WatchViewStub doesn't immediately inflate its contents.
                mTextView = (TextView) stub.findViewById(R.id.text);
            }
        });

    }
}
