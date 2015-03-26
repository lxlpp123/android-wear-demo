package com.livefront.android_wear_demo.activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Wearable;
import com.livefront.android_wear_demo.R;
import com.livefront.android_wear_demo.util.WatchFaceUtil;

/**
 * Allows the user to switch between Fahrenheit and Celsius on the watch. This and the utility
 * class are heavily adapted from Google's example DigitalWatchFaceWearableConfigActivity.
 */
public class TemperatureWatchFaceWearableConfigActivity extends Activity implements
        WearableListView.ClickListener, WearableListView.OnScrollListener {
    private final static String TAG = "TemperatureConfig";

    private GoogleApiClient mGoogleApiClient;
    private TextView mHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature_watch_face_config);

        mHeader = (TextView) findViewById(R.id.header);
        WearableListView listView = (WearableListView) findViewById(R.id.unit_picker);

        listView.setHasFixedSize(true);
        listView.setClickListener(this);
        listView.addOnScrollListener(this);

        String[] colors = getResources().getStringArray(R.array.temp_unit_array);
        listView.setAdapter(new TemperatureUnitListAdapter(colors));

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        if (Log.isLoggable(TAG, Log.DEBUG)) {
                            Log.d(TAG, "onConnected: " + connectionHint);
                        }
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {
                        if (Log.isLoggable(TAG, Log.DEBUG)) {
                            Log.d(TAG, "onConnectionSuspended: " + cause);
                        }
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        if (Log.isLoggable(TAG, Log.DEBUG)) {
                            Log.d(TAG, "onConnectionFailed: " + result);
                        }
                    }
                })
                .addApi(Wearable.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

        super.onStop();
    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        TemperatureUnitItemViewHolder tempUnitItemViewHolder = (TemperatureUnitItemViewHolder) viewHolder;
        updateConfigDataItem(tempUnitItemViewHolder.mTempUnitItem.getUnit());
        finish();
    }

    @Override
    public void onTopEmptyRegionClick() {}

    @Override
    public void onScroll(int scroll) {}

    @Override
    public void onAbsoluteScrollChange(int scroll) {
        float newTranslation = Math.min(-scroll, 0);
        mHeader.setTranslationY(newTranslation);
    }

    @Override
    public void onScrollStateChanged(int scrollState) {}

    @Override
    public void onCentralPositionChanged(int centralPosition) {}

    private void updateConfigDataItem(final String temperatureUnit) {
        DataMap configKeysToOverwrite = new DataMap();
        configKeysToOverwrite.putString(WatchFaceUtil.KEY_TEMPERATURE_UNITS,
                temperatureUnit);
        WatchFaceUtil.overwriteKeysInConfigDataMap(mGoogleApiClient, configKeysToOverwrite);
    }

    private class TemperatureUnitListAdapter extends WearableListView.Adapter {
        private final String[] mTempUnits;

        public TemperatureUnitListAdapter(String[] units) {
            mTempUnits = units;
        }

        @Override
        public TemperatureUnitItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new TemperatureUnitItemViewHolder(new TempUnitItem(parent.getContext()));
        }

        @Override
        public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
            TemperatureUnitItemViewHolder tempUnitItemViewHolder = (TemperatureUnitItemViewHolder) holder;
            String unitName = mTempUnits[position];
            tempUnitItemViewHolder.mTempUnitItem.setUnit(unitName);

            RecyclerView.LayoutParams layoutParams =
                    new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
            int colorPickerItemMargin = (int) getResources()
                    .getDimension(R.dimen.watch_face_config_margin);

            // Add margins to first and last item to make it possible for user to tap on them.
            if (position == 0) {
                layoutParams.setMargins(0, colorPickerItemMargin, 0, 0);
            } else if (position == mTempUnits.length - 1) {
                layoutParams.setMargins(0, 0, 0, colorPickerItemMargin);
            } else {
                layoutParams.setMargins(0, 0, 0, 0);
            }

            tempUnitItemViewHolder.itemView.setLayoutParams(layoutParams);
        }

        @Override
        public int getItemCount() {
            return mTempUnits.length;
        }
    }

    /** The layout of a color item including image and label. */
    private static class TempUnitItem extends FrameLayout implements
            WearableListView.OnCenterProximityListener {
        private static final float ALPHA_MAX = 1.0f;
        private static final float ALPHA_MIN = 0.7f;
        private static final float SCALE_MAX = 1.0f;
        private static final float SCALE_MIN = 0.7f;
        private static final int ANIMATION_DURATION = 100;

        private int mColorBlue;
        private int mColorGreen;

        private ImageView mCircle;
        private final TextView mLabel;

        public TempUnitItem(Context context) {
            super(context);
            View.inflate(context, R.layout.view_temperature_unit_item, this);

            mCircle = (ImageView) findViewById(R.id.circle);
            mLabel = (TextView) findViewById(R.id.label);

            mColorBlue = getResources().getColor(R.color.demo_blue);
            mColorGreen = getResources().getColor(R.color.demo_green);

            // Initialize view with non-center position values
            mCircle.setScaleX(SCALE_MIN);
            mCircle.setScaleY(SCALE_MIN);
            mLabel.setAlpha(ALPHA_MIN);
        }

        @Override
        public void onCenterPosition(boolean animate) {
            ((GradientDrawable) mCircle.getDrawable()).setColor(mColorBlue);
            if (animate) {
                mCircle.animate()
                        .scaleX(SCALE_MAX)
                        .scaleY(SCALE_MAX)
                        .setDuration(ANIMATION_DURATION)
                        .start();
                mLabel.animate()
                        .alpha(ALPHA_MAX)
                        .setDuration(ANIMATION_DURATION)
                        .start();
            } else {
                mCircle.setScaleX(SCALE_MAX);
                mCircle.setScaleY(SCALE_MAX);
                mLabel.setAlpha(ALPHA_MAX);
            }
        }

        @Override
        public void onNonCenterPosition(boolean animate) {
            ((GradientDrawable) mCircle.getDrawable()).setColor(mColorGreen);
            if (animate) {
                mCircle.animate()
                        .scaleX(SCALE_MIN)
                        .scaleY(SCALE_MIN)
                        .setDuration(ANIMATION_DURATION)
                        .start();
                mLabel.animate()
                        .alpha(ALPHA_MIN)
                        .setDuration(ANIMATION_DURATION)
                        .start();
            } else {
                mCircle.setScaleX(SCALE_MIN);
                mCircle.setScaleY(SCALE_MIN);
                mLabel.setAlpha(ALPHA_MIN);
            }
        }

        private void setUnit(String unitName) {
            mLabel.setText(unitName);
        }

        public String getUnit() {
            return mLabel.getText().toString();
        }
    }

    private static class TemperatureUnitItemViewHolder extends WearableListView.ViewHolder {
        private final TempUnitItem mTempUnitItem;

        public TemperatureUnitItemViewHolder(TempUnitItem tempUnitItem) {
            super(tempUnitItem);
            mTempUnitItem = tempUnitItem;
        }
    }
}
