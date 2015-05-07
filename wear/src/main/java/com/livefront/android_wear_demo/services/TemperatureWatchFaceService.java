package com.livefront.android_wear_demo.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.view.SurfaceHolder;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.livefront.android_wear_demo.R;
import com.livefront.android_wear_demo.util.WatchFaceUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class TemperatureWatchFaceService extends CanvasWatchFaceService {
    private static final String TAG = TemperatureWatchFaceService.class.getSimpleName();

    /**
     * Update rate in milliseconds for interactive mode. Update twice a second to blink the colons.
     */
    private static final long UPDATE_TIME_RATE_MS = 500;
    private static final long UPDATE_TEMP_RATE_MS = 60 * 60 * 1000;

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    /* implement service callback methods */
    private class Engine extends CanvasWatchFaceService.Engine implements MessageApi.MessageListener,
            GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
            DataApi.DataListener {

        private static final int MSG_UPDATE_TIME = 0;
        private static final int MSG_UPDATE_TEMPS = 1;

        private final Handler mUpdateTimeHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_UPDATE_TIME:
                        // Invalidate to indicate we want to redraw
                        invalidate();

                        if (shouldTimerBeRunning()) {
                            long timeMs = System.currentTimeMillis();

                            // Trigger on the next half-second
                            long delayMs = UPDATE_TIME_RATE_MS - (timeMs % UPDATE_TIME_RATE_MS);
                            mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
                        }
                        break;

                    case MSG_UPDATE_TEMPS:
                        retrieveUpdatedTemps();
                        if (shouldTimerBeRunning()) {
                            long timeMs = System.currentTimeMillis();
                            long delayMs = UPDATE_TEMP_RATE_MS - (timeMs % UPDATE_TIME_RATE_MS);
                            mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TEMPS, delayMs);
                        }
                        break;
                }
            }
        };

        private final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Reset to correct time zone
                mTime.clear(intent.getStringExtra("time-zone"));
                mTime.setToNow();
            }
        };

        private GoogleApiClient mGoogleApiClient;

        private Time mTime;

        private Paint mBackgroundPaint;
        private Paint mTimePaint;
        private Paint mHiLoPaint;
        private Paint mHighTemperaturePaint;
        private Paint mLowTemperaturePaint;

        private int mAmbientTextColor;
        private int mAmbientBackgroundColor;
        private int mInteractiveDefaultColor;
        private int mInteractiveBackgroundColor;

        private Map<Integer, Integer> mTempMap = new HashMap<>();
        private Bitmap mBackgroundCold;
        private Bitmap mBackgroundMild;
        private Bitmap mBackgroundHot;
        private Bitmap mBackgroundUnknown;

        private float mXOffset;
        private float mYOffset;
        private float mLineHeight;
        private float mHorizontalMargin;

        private boolean mRegisteredTimeZoneReceiver = false;

        private Integer mHighTemperature;
        private Integer mLowTemperature;
        private Integer mCurrentTemperature;

        private String mHiString;
        private String mLowString;
        private String mDegreeFormat;
        private String mUnknownString;

        private String mFahrenheitString;
        private String mCelsiusString;

        private WatchFaceUtil.TemperatureUnits mCurrentUnits;
        private String mCity;

        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        private boolean mLowBitAmbient;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(TemperatureWatchFaceService.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .build());

            Resources resources = TemperatureWatchFaceService.this.getResources();

            mAmbientBackgroundColor = resources.getColor(R.color.ambient_background);
            mAmbientTextColor = resources.getColor(R.color.ambient_time);
            mInteractiveDefaultColor = resources.getColor(R.color.default_time);
            mInteractiveBackgroundColor = resources.getColor(R.color.watch_face_background);
            mXOffset = resources.getDimension(R.dimen.x_offset);
            mYOffset = resources.getDimension(R.dimen.y_offset);
            mLineHeight = resources.getDimension(R.dimen.text_size);

            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(mInteractiveBackgroundColor);

            mTimePaint = new Paint();
            mTimePaint.setColor(mInteractiveDefaultColor);
            mTimePaint.setAntiAlias(true);
            mTimePaint.setTextSize(resources.getDimension(R.dimen.text_size));

            mHiLoPaint = new Paint();
            mHiLoPaint.setColor(mInteractiveDefaultColor);
            mHiLoPaint.setAntiAlias(true);
            mHiLoPaint.setTextSize(resources.getDimension(R.dimen.temp_text_size));

            mHighTemperaturePaint = new Paint();
            mHighTemperaturePaint.setColor(mInteractiveDefaultColor);
            mHighTemperaturePaint.setAntiAlias(true);
            mHighTemperaturePaint.setTextSize(resources.getDimension(R.dimen.temp_text_size));

            mLowTemperaturePaint = new Paint();
            mLowTemperaturePaint.setColor(mInteractiveDefaultColor);
            mLowTemperaturePaint.setAntiAlias(true);
            mLowTemperaturePaint.setTextSize(resources.getDimension(R.dimen.temp_text_size));

            mHiString = resources.getString(R.string.hi);
            mLowString = resources.getString(R.string.lo);
            mDegreeFormat = resources.getString(R.string.degree);
            mUnknownString = resources.getString(R.string.unknown);
            mFahrenheitString = getString(R.string.fahrenheit);
            mCelsiusString = getString(R.string.celsius);

            mHorizontalMargin = resources.getDimension(R.dimen.horizontal_margin);

            mTime = new Time();

            mTempMap.put(-20, resources.getColor(R.color.negative_twenty));
            mTempMap.put(-10, resources.getColor(R.color.negative_ten));
            mTempMap.put(0, resources.getColor(R.color.zero));
            mTempMap.put(10, resources.getColor(R.color.ten));
            mTempMap.put(20, resources.getColor(R.color.twenty));
            mTempMap.put(30, resources.getColor(R.color.thirty));
            mTempMap.put(40, resources.getColor(R.color.forty));
            mTempMap.put(50, resources.getColor(R.color.fifty));
            mTempMap.put(60, resources.getColor(R.color.sixty));
            mTempMap.put(70, resources.getColor(R.color.seventy));
            mTempMap.put(80, resources.getColor(R.color.eighty));
            mTempMap.put(90, resources.getColor(R.color.ninety));
            mTempMap.put(100, resources.getColor(R.color.one_hundred));
            mTempMap.put(110, resources.getColor(R.color.one_hundred_ten));

            mBackgroundCold = BitmapFactory.decodeResource(resources, R.drawable.bg_cold);
            mBackgroundMild = BitmapFactory.decodeResource(resources, R.drawable.bg_mild);
            mBackgroundHot = BitmapFactory.decodeResource(resources, R.drawable.bg_hot);
            mBackgroundUnknown = BitmapFactory.decodeResource(resources, R.drawable.bg_unknown);

            updateTimer();

            mGoogleApiClient = new GoogleApiClient.Builder(TemperatureWatchFaceService.this.getBaseContext())
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            mGoogleApiClient.connect();
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TEMPS);

            if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
                Wearable.MessageApi.removeListener(mGoogleApiClient, this);
                mGoogleApiClient.disconnect();
            }

            super.onDestroy();
        }

        /*
        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);

            // Code can be added here to use the insets variable and detect whether or not the
            // device is square or round. This requires API level 21.
        }
        */

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);

            // Also possible to get whether burn-in protection is turned on
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();

            // Invalidate to update the watch
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);

            if (inAmbientMode) {
                mTimePaint.setColor(mAmbientTextColor);
                mHiLoPaint.setColor(mAmbientTextColor);
                mHighTemperaturePaint.setColor(mAmbientTextColor);
                mLowTemperaturePaint.setColor(mAmbientTextColor);
                mBackgroundPaint.setColor(mAmbientBackgroundColor);
            } else {
                setTimePaintColor(mTimePaint, mCurrentTemperature);
                setTimePaintColor(mHighTemperaturePaint, mHighTemperature);
                setTimePaintColor(mLowTemperaturePaint, mLowTemperature);
                mHiLoPaint.setColor(mInteractiveDefaultColor);

                mBackgroundPaint.setColor(mInteractiveBackgroundColor);
            }

            if (mLowBitAmbient) {
                mTimePaint.setAntiAlias(!inAmbientMode);
                mHiLoPaint.setAntiAlias(!inAmbientMode);
                mHighTemperaturePaint.setAntiAlias(!inAmbientMode);
                mLowTemperaturePaint.setAntiAlias(!inAmbientMode);
            }

            invalidate();

            updateTimer();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            mTime.setToNow();
            boolean shouldDrawColon = (System.currentTimeMillis() % 1000) < 500;

            // Draw the background.
            canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);

            if (mCurrentTemperature == null) {
                canvas.drawBitmap(mBackgroundCold, 0, 0, null);
            } else {
                if (mCurrentTemperature < 40) {
                    canvas.drawBitmap(mBackgroundCold, 0, 0, null);
                } else if (mCurrentTemperature < 80) {
                    canvas.drawBitmap(mBackgroundMild, 0, 0, null);
                } else {
                    canvas.drawBitmap(mBackgroundHot, 0, 0, null);
                }
            }

            String time;
            if (isInAmbientMode()) {
                time = mTime.format("%I:%M");
            } else if (shouldDrawColon) {
                time = mTime.format("%I:%M:%S");
            } else {
                time = mTime.format("%I %M %S");
            }

            canvas.drawText(time, mXOffset, mYOffset, mTimePaint);

            String amPm = mTime.format("%p");
            canvas.drawText(amPm, mXOffset, mYOffset + mLineHeight, mTimePaint);

            float offset = mXOffset + mTimePaint.measureText(amPm) + mHorizontalMargin;
            canvas.drawText(mHiString, offset,
                    mYOffset + mLineHeight / 2f, mHiLoPaint);
            canvas.drawText(mLowString, offset,
                    mYOffset + mLineHeight, mHiLoPaint);

            offset += mHorizontalMargin + mHiLoPaint.measureText(mLowString);
            String highString;
            if (mHighTemperature != null) {
                int displayedValue = mHighTemperature;
                if (mCurrentUnits.equals(WatchFaceUtil.TemperatureUnits.Celsius)) {
                    displayedValue = (int) ((displayedValue - 32) * 5f / 9f);
                }

                highString = String.format(mDegreeFormat, displayedValue);
            } else {
                highString = mUnknownString;
            }

            String lowString;
            if (mLowTemperature != null) {
                int displayedValue = mLowTemperature;
                if (mCurrentUnits.equals(WatchFaceUtil.TemperatureUnits.Celsius)) {
                    displayedValue = (int) ((displayedValue - 32) * 5f / 9f);
                }

                lowString = String.format(mDegreeFormat, displayedValue);
            } else {
                lowString = mUnknownString;
            }

            canvas.drawText(highString, offset, mYOffset + mLineHeight / 2f, mHighTemperaturePaint);
            canvas.drawText(lowString, offset, mYOffset + mLineHeight, mLowTemperaturePaint);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();

                // Update time zone
                mTime.clear(TimeZone.getDefault().getID());
                mTime.setToNow();
            } else {
                unregisterReceiver();
            }

            updateTimer();
        }

        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TEMPS);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TEMPS);
            }
        }

        private boolean shouldTimerBeRunning() {
            // If we're not visible or in ambient mode let onTimeTick handle any updates
            return isVisible() && !isInAmbientMode();
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            TemperatureWatchFaceService.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            TemperatureWatchFaceService.this.unregisterReceiver(mTimeZoneReceiver);
        }

        private void setTimePaintColor(Paint paint, Integer temperature) {
            paint.setColor(convertTemperatureToColor(temperature));
        }

        private int convertTemperatureToColor(Integer temperature) {
            if (temperature == null) {
                return mInteractiveDefaultColor;
            }

            if (temperature < -10) {
                return mTempMap.get(-20);
            } else if (temperature < 0) {
                return mTempMap.get(-10);
            } else if (temperature < 10) {
                return mTempMap.get(0);
            } else if (temperature < 20) {
                return mTempMap.get(10);
            } else if (temperature < 30) {
                return mTempMap.get(20);
            } else if (temperature < 40) {
                return mTempMap.get(30);
            } else if (temperature < 50) {
                return mTempMap.get(40);
            } else if (temperature < 60) {
                return mTempMap.get(50);
            } else if (temperature < 70) {
                return mTempMap.get(60);
            } else if (temperature < 80) {
                return mTempMap.get(70);
            } else if (temperature < 90) {
                return mTempMap.get(80);
            } else if (temperature < 100) {
                return mTempMap.get(90);
            } else if (temperature < 110) {
                return mTempMap.get(100);
            } else {
                return mTempMap.get(110);
            }
        }

        private void retrieveUpdatedTemps() {
            PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/weather");
            putDataMapReq.getDataMap().putString("city", mCity);
            putDataMapReq.getDataMap().putLong("timestamp", Calendar.getInstance().getTimeInMillis());
            PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
            Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
        }

        private void updateConfigDataItemAndUiOnStartup() {
            WatchFaceUtil.fetchConfigDataMap(mGoogleApiClient,
                    new WatchFaceUtil.FetchConfigDataMapCallback() {
                        @Override
                        public void onConfigDataMapFetched(DataMap startupConfig) {
                            setDefaultValuesForMissingConfigKeys(startupConfig);
                            WatchFaceUtil.putConfigDataItem(mGoogleApiClient, startupConfig);

                            updateUiForConfigDataMap(startupConfig);
                        }
                    }
            );
        }

        private void setDefaultValuesForMissingConfigKeys(DataMap config) {
            addStringKeyIfMissing(config, WatchFaceUtil.KEY_TEMPERATURE_UNITS,
                    getResources().getString(R.string.fahrenheit));
            addStringKeyIfMissing(config, WatchFaceUtil.KEY_WEATHER_LOCATION,
                    getResources().getString(R.string.default_city));
        }

        private void addStringKeyIfMissing(DataMap config, String key, String value) {
            if (!config.containsKey(key)) {
                config.putString(key, value);
            }
        }

        private void updateUiForConfigDataMap(final DataMap config) {
            boolean uiUpdated = false;
            for (String configKey : config.keySet()) {
                if (!config.containsKey(configKey)) {
                    continue;
                }

                String value = config.getString(configKey);
                if (updateUiForKey(configKey, value)) {
                    uiUpdated = true;
                }
            }

            if (uiUpdated) {
                invalidate();
            }
        }

        private boolean updateUiForKey(String configKey, String value) {
            switch (configKey) {
                case WatchFaceUtil.KEY_TEMPERATURE_UNITS:
                    if (value.equals(mFahrenheitString)) {
                        mCurrentUnits = WatchFaceUtil.TemperatureUnits.Fahrenheit;
                    } else if (value.equals(mCelsiusString)) {
                        mCurrentUnits = WatchFaceUtil.TemperatureUnits.Celsius;
                    }
                    break;
                case WatchFaceUtil.KEY_WEATHER_LOCATION:
                    mCity = value;
                    retrieveUpdatedTemps();
                    break;
                default:
                    return false;
            }

            return true;
        }

        @Override
        public void onConnected(Bundle bundle) {
            Wearable.MessageApi.addListener(mGoogleApiClient, this);
            Wearable.DataApi.addListener(mGoogleApiClient, this);
            updateConfigDataItemAndUiOnStartup();
        }

        @Override
        public void onConnectionSuspended(int i) {

        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {

        }

        @Override
        public void onMessageReceived(MessageEvent messageEvent) {
            if (messageEvent.getPath().equals("/weather-response")) {
                // Convert byte array into integer array containing the three temperatures
                byte[] payload = messageEvent.getData();
                IntBuffer intBuf = ByteBuffer.wrap(payload).asIntBuffer();
                int[] array = new int[intBuf.remaining()];
                intBuf.get(array);

                // Temperatures received come back as Fahrenheit
                mCurrentTemperature = array[0];
                mHighTemperature = array[1];
                mLowTemperature = array[2];

                if (!isInAmbientMode()) {
                    setTimePaintColor(mTimePaint, mCurrentTemperature);
                    setTimePaintColor(mHighTemperaturePaint, mHighTemperature);
                    setTimePaintColor(mLowTemperaturePaint, mLowTemperature);
                }

                invalidate();
            } else if (messageEvent.getPath().equals(WatchFaceUtil.PATH_WITH_FEATURE)) {
                byte[] payload = messageEvent.getData();
                DataMap configKeysToOverwrite = DataMap.fromByteArray(payload);
                WatchFaceUtil.overwriteKeysInConfigDataMap(mGoogleApiClient, configKeysToOverwrite);
            }
        }

        @Override
        public void onDataChanged(DataEventBuffer dataEvents) {
            try {
                for (DataEvent dataEvent : dataEvents) {
                    if (dataEvent.getType() != DataEvent.TYPE_CHANGED) {
                        continue;
                    }

                    DataItem dataItem = dataEvent.getDataItem();
                    if (!dataItem.getUri().getPath().equals(
                            WatchFaceUtil.PATH_WITH_FEATURE)) {
                        continue;
                    }

                    DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItem);
                    DataMap config = dataMapItem.getDataMap();
                    updateUiForConfigDataMap(config);
                }
            } finally {
                dataEvents.close();
            }
        }
    }
}
