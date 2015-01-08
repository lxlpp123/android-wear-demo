package com.livefront.android_wear_demo.adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.view.Gravity;

import com.livefront.android_wear_demo.activity.PickerActivity;

import java.util.List;

public class QuoteGridPagerAdapter extends FragmentGridPagerAdapter {

    private static final float MAXIMUM_CARD_EXPANSION_FACTOR = 3.0f;

    private Context mContext;
    private List<PickerActivity.QuoteList> mData;

    public QuoteGridPagerAdapter(Context context, List<PickerActivity.QuoteList> quoteLists, FragmentManager fm) {
        super(fm);
        mContext = context;
        mData = quoteLists;
    }

    @Override
    public Fragment getFragment(int row, int column) {
        PickerActivity.QuoteList quoteList = mData.get(row);
        CardFragment fragment = CardFragment.create(quoteList.getTitle(column), quoteList.getText(column));
        fragment.setCardGravity(Gravity.BOTTOM);
        fragment.setExpansionEnabled(true);
        fragment.setExpansionDirection(CardFragment.EXPAND_DOWN);
        fragment.setExpansionFactor(MAXIMUM_CARD_EXPANSION_FACTOR);
        return fragment;
    }

    @Override
    public int getRowCount() {
        return mData.size();
    }

    @Override
    public int getColumnCount(int row) {
        return mData.get(row).getPageCount();
    }

    @Override
    public Drawable getBackgroundForPage(int row, int column) {
        return mContext.getResources().getDrawable(mData.get(row).getImageResource());
    }
}
