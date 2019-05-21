package com.vluver.beta.adapter;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.ViewGroup;

import com.alexvasilkov.gestures.Settings;
import com.alexvasilkov.gestures.commons.RecyclePagerAdapter;
import com.alexvasilkov.gestures.views.GestureImageView;
import com.vluver.beta.utils.GlideLoadImages;

import java.util.ArrayList;


public class PagerAdapterSwipe extends RecyclePagerAdapter<PagerAdapterSwipe.ViewHolder> {

    private final ViewPager viewPager;
    private final ArrayList<String> pathimg;
    private final ArrayList<String> nameimg;
    private final int p;
    private String thumb;

    public PagerAdapterSwipe(ViewPager pager, ArrayList<String> pathimg, ArrayList<String> nameimg, int p, String thumb) {
        this.viewPager = pager;
        this.pathimg=pathimg;
        this.nameimg=nameimg;
        this.p =p;
        this.thumb=thumb;

    }

    @Override
    public int getCount() {
        return pathimg.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup container) {
        ViewHolder holder = new ViewHolder(container);
        holder.image.getController().enableScrollInViewPager(viewPager);
        holder.image.getController().getSettings()
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
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //settingsController.apply(holder.image);
        GlideLoadImages.fullImage(holder.image.getContext(),pathimg,nameimg, position,holder.image,thumb);


    }

    public static GestureImageView getImageView(RecyclePagerAdapter.ViewHolder holder) {
        return ((ViewHolder) holder).image;
    }


    static class ViewHolder extends RecyclePagerAdapter.ViewHolder {
        final GestureImageView image;

        ViewHolder(ViewGroup container) {
            super(new GestureImageView(container.getContext()));
            image = (GestureImageView) itemView;
        }
    }

}
