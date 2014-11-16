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

public class DemoItemLayout extends LinearLayout implements WearableListView.Item {

    private static final float PROXIMITY_MIN_VALUE = 1.0f;
    private static final float PROXIMITY_MAX_VALUE = 1.5f;

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
        mColorBlue = getResources().getColor(R.color.demo_item_circle_color_blue);
        mColorGreen = getResources().getColor(R.color.demo_item_circle_color_green);
    }

    // WearableListView.Item methods //

    @Override
    public float getProximityMinValue() {
        return PROXIMITY_MIN_VALUE;
    }

    @Override
    public float getProximityMaxValue() {
        return PROXIMITY_MAX_VALUE;
    }

    @Override
    public float getCurrentProximityValue() {
        return mScale;
    }

    @Override
    public void setScalingAnimatorValue(float v) {
        mScale = v;
        mCircle.setScaleX(mScale);
        mCircle.setScaleY(mScale);
        mText.setAlpha(mScale/PROXIMITY_MAX_VALUE);
    }

    @Override
    public void onScaleUpStart() {
        ((GradientDrawable) mCircle.getDrawable()).setColor(mColorBlue);
    }

    @Override
    public void onScaleDownStart() {
        ((GradientDrawable) mCircle.getDrawable()).setColor(mColorGreen);
    }
}
