package com.livefront.android_wear_demo.services;

import android.net.Uri;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.livefront.android_wear_demo.tasks.RetrieveCityWeatherTask;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class DemoWearableListenerService extends WearableListenerService {

    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onPeerConnected(Node peer) {
        super.onPeerConnected(peer);
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        super.onPeerDisconnected(peer);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo("/weather") == 0) {
                    Uri uri = item.getUri();
                    final String host = uri.getHost();

                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    String city = dataMap.getString("city");

                    RetrieveCityWeatherTask.TaskCaller caller = new RetrieveCityWeatherTask.TaskCaller() {
                        @Override
                        public void onComplete(int[] temps) {
                            sendMessage(host, temps);
                        }
                    };
                    RetrieveCityWeatherTask task = new RetrieveCityWeatherTask(caller);
                    task.execute(city);
                }
            }
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
    }

    private void sendMessage(String nodeId, int[] temps) {
        if (!mGoogleApiClient.isConnected()) {
            return;
        }

        // Send a message back to the node containing the three temperatures
        ByteBuffer byteBuffer = ByteBuffer.allocate(temps.length * 4);
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(temps);

        byte[] payload = byteBuffer.array();
        Wearable.MessageApi.sendMessage(mGoogleApiClient, nodeId,
                "/weather-response", payload);
    }
}
