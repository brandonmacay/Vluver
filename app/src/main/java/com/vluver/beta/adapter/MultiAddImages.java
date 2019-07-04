package com.vluver.beta.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.vluver.beta.R;
import com.vluver.beta.model.AddMoreImages;

import java.util.List;

public class MultiAddImages extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private LayoutInflater inflater;
    List<AddMoreImages> data;

    public MultiAddImages(Context context, List<AddMoreImages> data){
        this.context=context;
        inflater= LayoutInflater.from(context);
        this.data=data;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=inflater.inflate(R.layout.gv_item, parent,false);
        MultiAddImages.MyHolder holder=new MultiAddImages.MyHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        // Get current position of item in RecyclerView to bind data and assign values from list
        MultiAddImages.MyHolder myHolder= (MultiAddImages.MyHolder) holder;
        AddMoreImages current=data.get(position);
        RequestOptions optionb = new RequestOptions()
                //.centerCrop()
                .fitCenter()
                .placeholder(android.R.color.darker_gray)
                .error(R.drawable.noneimg)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE);
        Glide.with(context)
                .load(current.mArrayUri)
                .apply(optionb)
                .into(myHolder.imagess);
        // myHolder.imageUserAvatar.setpic(current.userAvatar);


        //myHolder.textPrice.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
        //poss = position;

    }
    @Override
    public int getItemCount() {
        return data.size();
    }
    class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView imagess;


        // create constructor to get widget reference
        public MyHolder(View itemView) {
            super(itemView);

            imagess = (ImageView) itemView.findViewById(R.id.ivGallery);

            itemView.setOnClickListener(this);
        }


        // Click event for all items
        @Override
        public void onClick(View v) {



        }


    }
}
