package com.livefront.android_wear_demo.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.DelayedConfirmationView;
import android.view.View;
import android.widget.Toast;

import com.livefront.android_wear_demo.R;

public class DelayedConfirmationViewActivity extends Activity implements
        DelayedConfirmationView.DelayedConfirmationListener {

    private static final int CONFIRMATION_REQUEST_CODE = 0;

    private boolean mCanceled = false;
    private DelayedConfirmationView mDelayedView;
    private View mButton;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CONFIRMATION_REQUEST_CODE) {
            // Returning from ConfirmationActivity, finish this activity
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delayed_confirmation_view);

        mDelayedView = (DelayedConfirmationView)
                findViewById(R.id.delayed_confirm);

        // Programmatically hide due to visibility bug when setting value in XML. See the following
        // issue for more details:
        // https://code.google.com/p/android/issues/detail?id=90142
        mDelayedView.setVisibility(View.GONE);

        // Set the timer to 2 seconds
        mDelayedView.setTotalTimeMs(2000);

        // Set this activity as a listener
        mDelayedView.setListener(this);

        // Set click behavior for the button that will trigger the timer
        mButton = findViewById(R.id.trigger_button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateInDelayedViewAndStartTimer();
            }
        });
    }

    @Override
    public void onTimerFinished(View view) {
        if (mCanceled) {
            // Timer was cancelled, do nothing
            return;
        }

        // Show the success animation
        Intent intent = new Intent(this, ConfirmationActivity.class);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                ConfirmationActivity.SUCCESS_ANIMATION);
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE,
                getString(R.string.message_confirmed));
        startActivityForResult(intent, CONFIRMATION_REQUEST_CODE);
    }

    @Override
    public void onTimerSelected(View view) {
        // Indicate that the timer should do nothing when it finishes
        mCanceled = true;

        // Show a cancellation toast
        Toast.makeText(this, getString(R.string.message_canceled), Toast.LENGTH_SHORT).show();
        finish();
    }

    private void animateInDelayedViewAndStartTimer() {
        // We'll translate the views one full screen width
        int screenWidth = getResources().getDisplayMetrics().widthPixels;

        // Animate the trigger button off to the left
        ObjectAnimator buttonAnimator = ObjectAnimator.ofFloat(mButton, View.TRANSLATION_X, 0,
                -screenWidth);
        // Animate the delayed confirmation view in from the right
        ObjectAnimator delayedViewAnimator = ObjectAnimator.ofFloat(mDelayedView, View.TRANSLATION_X,
                screenWidth, 0);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(buttonAnimator, delayedViewAnimator);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                // Show the delayed confirmation view
                mDelayedView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                // Start the timer
                mDelayedView.start();
            }
        });
        set.setDuration(300);
        set.start();
    }
}
