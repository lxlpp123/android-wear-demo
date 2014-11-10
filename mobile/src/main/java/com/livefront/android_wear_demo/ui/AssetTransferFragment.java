package com.livefront.android_wear_demo.ui;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.livefront.android_wear_demo.R;

import java.io.ByteArrayOutputStream;

/**
 * Created by benjamin on 11/7/14.
 */
public class AssetTransferFragment extends Fragment implements View.OnClickListener {

    private GoogleApiClient mGoogleApiClient;
    private Button mWearItButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_asset_transfer, container, false);
        mWearItButton = (Button) view.findViewById(R.id.wear_it_button);
        mWearItButton.setOnClickListener(this);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity()).addApi(Wearable.API).build();
    }

    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onStop() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        if (v == mWearItButton) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) getActivity().getResources().getDrawable(R.drawable.sandworm);
            Bitmap bitmap = bitmapDrawable.getBitmap();
            Asset asset = createAssetFromBitmap(bitmap);
            PutDataMapRequest dataMapRequest = PutDataMapRequest.create("/image");
            dataMapRequest.getDataMap().putAsset("image", asset);
            PutDataRequest dataRequest = dataMapRequest.asPutDataRequest();
            Wearable.DataApi.putDataItem(mGoogleApiClient, dataRequest);
        }
    }

    private static Asset createAssetFromBitmap(Bitmap bitmap) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
    }
}
