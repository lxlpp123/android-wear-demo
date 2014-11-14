package com.livefront.android_wear_demo.activity;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.wearable.view.CardFragment;
import android.view.Gravity;

import com.livefront.android_wear_demo.R;

public class CardFragmentActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_fragment);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        CardFragment cardFragment = CardFragment.create(getString(R.string.card_fragment_title),
                getString(R.string.card_fragment_text),
                R.drawable.card_icon);
        cardFragment.setCardGravity(Gravity.BOTTOM);
        transaction.add(R.id.container, cardFragment);
        transaction.commit();
    }
}
