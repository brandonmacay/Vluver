package com.vluver.beta.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.vluver.beta.R;

import java.util.ArrayList;

/**
 * Created by brand on 6/17/2018.
 */

public class GlideLoadImages {

    public static void loadAvatar(Context c, String url, ImageView img)
    {
        RequestOptions options = new RequestOptions()
                .format(DecodeFormat.PREFER_RGB_565)
                .fitCenter()
                .circleCrop()
                .placeholder(R.drawable.noneimg)
                .error(R.drawable.noneimg)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);
        Glide.with(c)
                .load(url)
                .apply(options)
                .thumbnail(0.5f)
                .into(img);
    }

    public static void loadImagesPosts(Context context, String path, String nameimg, ImageView imgpost) {

        RequestOptions optionb = new RequestOptions()
                .format(DecodeFormat.PREFER_ARGB_8888)
                .centerCrop()
                //.fitCenter()
                .placeholder(android.R.color.darker_gray)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .error(R.drawable.brokenimage);

        final RequestBuilder<Drawable> thumbRequest = Glide.with(context)
                .load(path+"small_"+nameimg)
                .apply(optionb);

        Glide.with(context)
                .load(path+nameimg)
                .apply(optionb)
                .thumbnail(thumbRequest)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imgpost);
    }
    public static void onlyimage(Context context, String path, String nameimg, ImageView imageView){
        RequestOptions optionb = new RequestOptions()
                .format(DecodeFormat.PREFER_ARGB_8888)
                .fitCenter()
                .override(Target.SIZE_ORIGINAL)
                .placeholder(android.R.color.darker_gray)
                .error(R.drawable.brokenimage)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .priority(Priority.IMMEDIATE);
        final RequestBuilder<Drawable> thumbRequest = Glide.with(imageView)
                .load(path+"small_"+nameimg)
                .apply(optionb);

        Glide.with(context)
                .load(path+nameimg)
                .apply(optionb)
                .thumbnail(thumbRequest)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView);
    }

    public static void fullImage(Context context, ArrayList<String> path, ArrayList<String> nameimg, int p, ImageView imageView, String thumb){
        RequestOptions optionb = new RequestOptions()
                .format(DecodeFormat.PREFER_ARGB_8888)
                .fitCenter()
                .override(Target.SIZE_ORIGINAL)
                .placeholder(android.R.color.darker_gray)
                .error(R.drawable.brokenimage)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .priority(Priority.IMMEDIATE);

        final RequestBuilder<Drawable> thumbRequest = Glide.with(imageView)
                .load(path.get(p)+thumb+nameimg.get(p))
                .apply(optionb);

        Glide.with(context)
                .load(path.get(p)+nameimg.get(p))
                .apply(optionb)
                .thumbnail(thumbRequest)
                .into(imageView);


    }

}

