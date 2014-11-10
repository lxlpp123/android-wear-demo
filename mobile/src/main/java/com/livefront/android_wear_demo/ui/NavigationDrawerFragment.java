package com.livefront.android_wear_demo.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.livefront.android_wear_demo.R;

/**
 * Created by benjamin on 11/7/14.
 */
public class NavigationDrawerFragment extends Fragment {

    public interface NavigationDrawerListener {
        public void onAssetMenuItemClicked();
    }

    private NavigationDrawerListener mListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
    }

    public void setNavigationDrawerListener(NavigationDrawerListener listener) {
        mListener = listener;
    }
}
