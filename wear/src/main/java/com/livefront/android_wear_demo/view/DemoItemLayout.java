package com.livefront.android_wear_demo.view;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.wearable.view.WearableListView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.livefront.android_wear_demo.R;

public class DemoItemLayout extends LinearLayout implements WearableListView.OnCenterProximityListener {

    private static final float ALPHA_MAX = 1.0f;
    private static final float ALPHA_MIN = 0.7f;
    private static final float SCALE_MAX = 1.0f;
    private static final float SCALE_MIN = 0.7f;


    private int mColorBlue;
    private int mColorGreen;
    private float mScale;

    private ImageView mCircle;
    private TextView mText;

    public DemoItemLayout(Context context) {
        super(context);
        init();
    }

    public DemoItemLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DemoItemLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_demo_item_layout_content, this, true);
        mCircle = (ImageView) findViewById(R.id.circle);
        mText = (TextView) findViewById(R.id.name);
        mColorBlue = getResources().getColor(R.color.demo_blue);
        mColorGreen = getResources().getColor(R.color.demo_green);
    }

    // WearableListView.Item methods //

    @Override
    public void onCenterPosition(boolean animate) {
        // TODO Add animations
        mCircle.setScaleX(SCALE_MAX);
        mCircle.setScaleY(SCALE_MAX);
        mText.setAlpha(ALPHA_MAX);
        ((GradientDrawable) mCircle.getDrawable()).setColor(mColorBlue);
    }

    @Override
    public void onNonCenterPosition(boolean animate) {
        // TODO Add animations
        mCircle.setScaleX(SCALE_MIN);
        mCircle.setScaleY(SCALE_MIN);
        mText.setAlpha(ALPHA_MIN);
        ((GradientDrawable) mCircle.getDrawable()).setColor(mColorGreen);
    }
}
