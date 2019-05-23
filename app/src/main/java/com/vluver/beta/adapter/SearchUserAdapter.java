package com.vluver.beta.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.vluver.beta.FoundUserActivity;
import com.vluver.beta.R;
import com.vluver.beta.model.SearchUser;
import com.vluver.beta.utils.GlideLoadImages;

import java.util.List;

/**
 * Created by Vlover on 21/04/2018.
 */

public class SearchUserAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private LayoutInflater inflater;
    List<SearchUser> data;

    public SearchUserAdapter(Context context, List<SearchUser> data){
        this.context=context;
        inflater= LayoutInflater.from(context);
        this.data=data;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=inflater.inflate(R.layout.adaptador_busqueda_personas, parent,false);
        MyHolder holder=new MyHolder(view);
        return holder;
    }

    // Bind data
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        // Get current position of item in RecyclerView to bind data and assign values from list
        MyHolder myHolder= (MyHolder) holder;
        SearchUser current=data.get(position);
        myHolder.textUserName.setText(current.userName);

        myHolder.textUserEmail.setText(current.userEmail);

        GlideLoadImages.loadAvatar(context,current.userAvatar,myHolder.imageUserAvatar);

    }

    @Override
    public int getItemCount() {
        return data.size();
    }
    class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView textUserName;

        TextView textUserEmail;

        ImageView imageUserAvatar;


        // create constructor to get widget reference
        public MyHolder(View itemView) {
            super(itemView);
            textUserName = (TextView) itemView.findViewById(R.id.search_user_name);

            textUserEmail = (TextView) itemView.findViewById(R.id.search_user_email);

            imageUserAvatar = (ImageView) itemView.findViewById(R.id.circleImageView_search_user);

            itemView.setOnClickListener(this);
        }


        // Click event for all items
        @Override
        public void onClick(View v) {

            Intent intent = new Intent(context, FoundUserActivity.class);
            intent.putExtra("userUID", data.get((getAdapterPosition())).userUID);
            intent.putExtra("userName", data.get((getAdapterPosition())).userName);
            intent.putExtra("userAvatar", data.get((getAdapterPosition())).userAvatar);
            intent.putExtra("userPrivacy",data.get((getAdapterPosition())).userPrivacy);
            context.startActivity(intent);

        }


    }
}
