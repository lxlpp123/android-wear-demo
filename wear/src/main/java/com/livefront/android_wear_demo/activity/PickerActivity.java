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
        // TODO Add data
    }

    private void setupGridViewPager() {
        final GridViewPager pager = (GridViewPager) findViewById(R.id.pager);
        pager.setAdapter(new QuoteGridPagerAdapter(mQuoteLists, getFragmentManager()));
    }

    public static class QuoteList {
        private int mImageResource;
        private List<Quote> mQuotes;
        private String mName;

        public QuoteList(String name, int imageResource, List<Quote> quotes) {
            mName = name;
            mImageResource = imageResource;
            mQuotes = quotes;
        }

        public String getName() {
            return mName;
        }

        public String getText(int page) {
            // First page is blank, other pages show an actual quote
            if (page == 0) {
                return null;
            } else {
                return mQuotes.get(page - 1).getText();
            }
        }

        public int getPageCount() {
            return mQuotes.size() + 1;
        }

        public int getImageResource() {
            return mImageResource;
        }
    }

    public static class Quote {
        private String mText;

        public Quote(String text) {
            mText = text;
        }

        public String getText() {
            return mText;
        }
    }

}
