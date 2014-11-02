package com.livefront.android_wear_demo.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.GridViewPager;

import com.livefront.android_wear_demo.R;
import com.livefront.android_wear_demo.adapter.QuoteGridPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class PickerActivity extends Activity {

    private List<QuoteList> mQuoteLists = new ArrayList<QuoteList>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picker);
        setupData();
        setupGridViewPager();
    }

    private void setupData() {
        setupDataDrax();
        setupDataGamora();
        setupDataGroot();
        setupDataRocket();
        setupDataStarLord();
    }

    private void setupDataDrax() {
        List<String> quotes = new ArrayList<String>();
        quotes.add(getString(R.string.picker_drax_quote_1));
        mQuoteLists.add(new QuoteList(getString(R.string.picker_drax), R.drawable.drax, quotes));
    }

    private void setupDataGamora() {
        List<String> quotes = new ArrayList<String>();
        quotes.add(getString(R.string.picker_gamora_quote_1));
        mQuoteLists.add(new QuoteList(getString(R.string.picker_gamora), R.drawable.gamora, quotes));
    }

    private void setupDataGroot() {
        List<String> quotes = new ArrayList<String>();
        quotes.add(getString(R.string.picker_groot_quote_1));
        quotes.add(getString(R.string.picker_groot_quote_1));
        quotes.add(getString(R.string.picker_groot_quote_1));
        mQuoteLists.add(new QuoteList(getString(R.string.picker_groot), R.drawable.groot, quotes));
    }

    private void setupDataRocket() {
        List<String> quotes = new ArrayList<String>();
        quotes.add(getString(R.string.picker_rocket_quote_1));
        mQuoteLists.add(new QuoteList(getString(R.string.picker_rocket), R.drawable.rocket, quotes));
    }

    private void setupDataStarLord() {
        List<String> quotes = new ArrayList<String>();
        quotes.add(getString(R.string.picker_star_lord_quote_1));
        mQuoteLists.add(new QuoteList(getString(R.string.picker_star_lord), R.drawable.star_lord, quotes));
    }

    private void setupGridViewPager() {
        final GridViewPager pager = (GridViewPager) findViewById(R.id.pager);
        pager.setAdapter(new QuoteGridPagerAdapter(mQuoteLists, getFragmentManager()));
    }

    public static class QuoteList {
        private int mImageResource;
        private List<String> mQuotes;
        private String mName;

        public QuoteList(String name, int imageResource, List<String> quotes) {
            mName = name;
            mImageResource = imageResource;
            mQuotes = quotes;
        }

        public String getTitle(int page) {
            // Only the first page has a title
            if (page == 0) {
                return mName;
            } else {
                return null;
            }
        }

        public String getText(int page) {
            // First has no text
            if (page == 0) {
                return null;
            } else {
                return mQuotes.get(page - 1);
            }
        }

        public int getPageCount() {
            return mQuotes.size() + 1;
        }

        public int getImageResource() {
            return mImageResource;
        }
    }
}
