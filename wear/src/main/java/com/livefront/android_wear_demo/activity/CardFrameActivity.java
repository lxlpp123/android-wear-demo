package com.livefront.android_wear_demo.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.CardScrollView;
import android.view.Gravity;

import com.livefront.android_wear_demo.R;

public class CardFrameActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_frame);

        CardScrollView cardScrollView =
                (CardScrollView) findViewById(R.id.card_scroll_view);
        cardScrollView.setCardGravity(Gravity.BOTTOM);
    }

}
