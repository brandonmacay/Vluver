package com.vluver.beta.activities;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
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
        }



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
