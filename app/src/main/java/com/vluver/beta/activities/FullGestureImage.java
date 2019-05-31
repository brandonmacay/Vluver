package com.vluver.beta.activities;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import com.alexvasilkov.events.Events;
import com.alexvasilkov.gestures.Settings;
import com.alexvasilkov.gestures.animation.ViewPosition;
import com.alexvasilkov.gestures.commons.RecyclePagerAdapter;
import com.alexvasilkov.gestures.transition.GestureTransitions;
import com.alexvasilkov.gestures.transition.ViewsTransitionAnimator;
import com.alexvasilkov.gestures.transition.tracker.SimpleTracker;
import com.alexvasilkov.gestures.views.GestureImageView;
import com.vluver.beta.R;
import com.vluver.beta.adapter.PagerAdapterSwipe;
import com.vluver.beta.adapter.PostUserAdapter;
import com.vluver.beta.base.CrossEvents;
import com.vluver.beta.utils.GlideLoadImages;

import java.util.ArrayList;


public class FullGestureImage extends AppCompatActivity {
    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 0;
    private static final int UI_ANIMATION_DELAY = 0;
    private final Handler mHideHandler = new Handler();
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            image.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            pager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };
    private static final String EXTRA_POSITION = "position";
    private GestureImageView image;
    private View background;
    private RecyclerView list;
    private ViewPager pager;
    private PagerAdapterSwipe pagerAdapter;
    private ViewsTransitionAnimator<Integer> animator;
    int totalimg;
    public static void open(Context from, ViewPosition position, ArrayList<String> path, ArrayList<String> nameimg, int p, String thumb, int totalimg) {
        Intent intent = new Intent(from, FullGestureImage.class);
        intent.putExtra(EXTRA_POSITION, position.pack());
        intent.putStringArrayListExtra("path",path);
        intent.putStringArrayListExtra("nameimg",nameimg);
        intent.putExtra("positionimg",p);
        intent.putExtra("thumb",thumb);
        intent.putExtra("totalimg",totalimg);
        Bundle bundle = ActivityOptions.makeCustomAnimation(from, 0, 0).toBundle();
        from.startActivity(intent,bundle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_gesture_image);
        image = findViewById(R.id.single_image_to);
        background = findViewById(R.id.single_image_to_back);
        list = findViewById(R.id.recycler_list);
        pager = findViewById(R.id.recycler_pager);
        ArrayList<String> pathimg = getIntent().getStringArrayListExtra("path");
        ArrayList<String> nameimg = getIntent().getStringArrayListExtra("nameimg");
        String thumb = getIntent().getStringExtra("thumb");
        int p = getIntent().getIntExtra("positionimg",0);
        totalimg = getIntent().getIntExtra("totalimg",0);
        list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle();
            }
        });
        if (totalimg > 1){
            image.setVisibility(View.GONE);
            pager.setVisibility(View.VISIBLE);
            list.setVisibility(View.GONE);
            background.setVisibility(View.INVISIBLE);
            pagerAdapter = new PagerAdapterSwipe(pager, pathimg, nameimg,p,thumb);
            pager.setAdapter(pagerAdapter);
            pager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.view_pager_margin));
            final SimpleTracker listTracker = new SimpleTracker() {
                @Override
                public View getViewAt(int position) {
                    RecyclerView.ViewHolder holder = list.findViewHolderForLayoutPosition(position);
                    return holder == null ? null : PostUserAdapter.getImageView(holder);
                }
            };

            final SimpleTracker pagerTracker = new SimpleTracker() {
                @Override
                public View getViewAt(int position) {
                    RecyclePagerAdapter.ViewHolder holder = pagerAdapter.getViewHolder(position);
                    return holder == null ? null : PagerAdapterSwipe.getImageView(holder);
                }
            };
            animator = GestureTransitions.from(list, listTracker).into(pager, pagerTracker);
            // Setting up background animation during image transition
            animator.addPositionUpdateListener(this::applyImageAnimationStates);
            animator.enter( p,true);


        }else{
            list.setVisibility(View.GONE);
            image.setVisibility(View.INVISIBLE);
            background.setVisibility(View.INVISIBLE);
            GlideLoadImages.fullImage(this,pathimg,nameimg, p,image,thumb);

            image.getPositionAnimator().addPositionUpdateListener(this::applyImageAnimationState);
            // Starting enter image animation only once image is drawn for the first time to prevent
            // image blinking on activity start
            runAfterImageDraw(() -> {
                // Enter animation should only be played if activity is not created from saved state
                enterFullImage(savedInstanceState == null);

                // Hiding original image
                Events.create(CrossEvents.SHOW_IMAGE).param(false).post();
            });
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggle();
                }
            });
        }



    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(0);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        image.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
    private void applyImageAnimationStates(float position,boolean isLeaving) {
        boolean isFinished = position == 0f && isLeaving; // Exit animation is finished
        background.setAlpha(position);
        background.setVisibility(isFinished ? View.INVISIBLE : View.VISIBLE);
        if (isFinished){
            runOnNextFrames(() -> {
                finish();
                overridePendingTransition(0, 0);
            });
        }
    }
    private void enterFullImage(boolean animate) {
        // Updating gesture image settings
        // Playing enter animation from provided position
        image.getController().getSettings()
                .setMaxZoom(5f)
                .setDoubleTapEnabled(true)
                .setDoubleTapZoom(-1f)
                .setRotationEnabled(true)
                .setRestrictBounds(true)
                .setRestrictRotation(true)
                .setOverscrollDistance(0f,0f)
                .setFitMethod(Settings.Fit.INSIDE)
                .setFillViewport(true)
                .setGravity(Gravity.CENTER);
        ViewPosition position = ViewPosition.unpack(getIntent().getStringExtra(EXTRA_POSITION));
        image.getPositionAnimator().enter(position, animate);
    }

    private void applyImageAnimationState(float position, boolean isLeaving) {
        boolean isFinished = position == 0f && isLeaving; // Exit animation is finished

        background.setAlpha(position);
        background.setVisibility(isFinished ? View.INVISIBLE : View.VISIBLE);
        image.setVisibility(isFinished ? View.INVISIBLE : View.VISIBLE);
         if (isFinished) {
            // Showing back original image
            Events.create(CrossEvents.SHOW_IMAGE).param(true).post();

            // By default end of exit animation will return GestureImageView into
            // fullscreen state, this will make the image blink. So we need to hack this
            // behaviour and keep image in exit state until activity is finished.
            image.getController().getSettings().disableBounds();
            image.getPositionAnimator().setState(0f, false, false);

            runOnNextFrame(() -> {
                finish();
                overridePendingTransition(0, 0);
            });
        }
    }
    private void runAfterImageDraw(final Runnable action) {
        image.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                image.getViewTreeObserver().removeOnPreDrawListener(this);
                runOnNextFrame(action);
                return true;
            }
        });
        image.invalidate();

    }

    private void runOnNextFrame(Runnable action) {
        final long frameLength = 17L; // 1 frame at 60 fps
        image.postDelayed(action, frameLength);
    }
    private void runOnNextFrames(Runnable action) {
        final long frameLength = 17L; // 1 frame at 60 fps
        pager.postDelayed(action, frameLength);
    }

    @Override
    public void onBackPressed() {
        if (totalimg >1){
            if (!animator.isLeaving()) {
                animator.exit(true);
            }
        }else{
            if (!image.getPositionAnimator().isLeaving()) {
                image.getPositionAnimator().exit(true);
            }
        }

    }

}
