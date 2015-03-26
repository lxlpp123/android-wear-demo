package com.livefront.android_wear_demo.ui;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.wearable.companion.WatchFaceCompanion;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.livefront.android_wear_demo.R;

/**
 * Configuration activity for the companion app. Heavily adapted from Google's
 * DigitalWatchFaceCompanionConfigActivity example.
 */
public class TemperatureWatchFaceCompanionConfigActivity extends Activity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<DataApi.DataItemResult> {
    private static final String TAG = "TemperatureWatchConfig";

    public static final String KEY_TEMPERATURE_UNITS = "TEMPERATURE_UNITS";
    public static final String KEY_WEATHER_LOCATION = "WEATHER_LOCATION";
    public static final String PATH_WITH_FEATURE = "/watch_face_config/Temperature";

    private GoogleApiClient mGoogleApiClient;
    private String mPeerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature_watch_face_config);

        mPeerId = getIntent().getStringExtra(WatchFaceCompanion.EXTRA_PEER_ID);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
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
    public void onConnected(Bundle connectionHint) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onConnected: " + connectionHint);
        }

        if (mPeerId != null) {
            Uri.Builder builder = new Uri.Builder();
            Uri uri = builder.scheme("wear").path(PATH_WITH_FEATURE).authority(mPeerId).build();
            Wearable.DataApi.getDataItem(mGoogleApiClient, uri).setResultCallback(this);
        }
    }

    @Override
    public void onResult(DataApi.DataItemResult dataItemResult) {
        if (dataItemResult.getStatus().isSuccess() && dataItemResult.getDataItem() != null) {
            DataItem configDataItem = dataItemResult.getDataItem();
            DataMapItem dataMapItem = DataMapItem.fromDataItem(configDataItem);
            DataMap config = dataMapItem.getDataMap();
            setupFields(config);
        } else {
            // If DataItem with the current config can't be retrieved, select the default items
            setupFields(null);
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onConnectionSuspended: " + cause);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onConnectionFailed: " + result);
        }
    }

    private void setupFields(DataMap config) {
        EditText locationField = (EditText) findViewById(R.id.edit_location);
        locationField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sendConfigUpdateMessage(KEY_WEATHER_LOCATION, s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        RadioButton fahrenheitRadio = (RadioButton) findViewById(R.id.radio_fahrenheit);
        fahrenheitRadio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sendConfigUpdateMessage(KEY_TEMPERATURE_UNITS, getString(R.string.fahrenheit));
                }
            }
        });
        RadioButton celsiusRadio = (RadioButton) findViewById(R.id.radio_celsius);
        celsiusRadio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sendConfigUpdateMessage(KEY_TEMPERATURE_UNITS, getString(R.string.celsius));
                }
            }
        });

        if (config == null) {
            locationField.setText(getString(R.string.default_location));
            fahrenheitRadio.setChecked(true);
        } else {
            String location = config.getString(KEY_WEATHER_LOCATION);
            locationField.setText(location);

            String units = config.getString(KEY_TEMPERATURE_UNITS);
            if (units.equals(getString(R.string.fahrenheit))) {
                fahrenheitRadio.setChecked(true);
            } else if (units.equals(getString(R.string.celsius))) {
                celsiusRadio.setChecked(true);
            }
        }
    }

    private void sendConfigUpdateMessage(String configKey, String value) {
        if (mPeerId != null) {
            DataMap config = new DataMap();
            config.putString(configKey, value);
            byte[] rawData = config.toByteArray();
            Wearable.MessageApi.sendMessage(mGoogleApiClient, mPeerId, PATH_WITH_FEATURE, rawData);
        }
    }
}

