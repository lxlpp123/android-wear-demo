package com.livefront.android_wear_demo.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.DismissOverlayView;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.livefront.android_wear_demo.R;

public class DismissOverlayViewActivity extends Activity {

    private DismissOverlayView mDismissOverlay;
    private GestureDetector mDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dismiss_overlay_view);

        mDismissOverlay = (DismissOverlayView) findViewById(R.id.dismiss_overlay);

        // If this is the first time we've entered this activity, show a message indicating that
        // we can use a long press to dismiss the activity
        mDismissOverlay.setIntroText(getString(R.string.dismiss_intro_text));
        mDismissOverlay.showIntroIfNecessary();

        // Trigger the dismiss overlay view on a long press
        mDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            public void onLongPress(MotionEvent ev) {
                mDismissOverlay.show();
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // Always check for long presses before dispatching any event. Note that if we do this
        // check in onTouchEvent instead of here when swipeToDismiss is enabled, the long press
        // event would be consumed by the system before ever reaching the gesture detector.
        return mDetector.onTouchEvent(ev) || super.dispatchTouchEvent(ev);
    }
}
