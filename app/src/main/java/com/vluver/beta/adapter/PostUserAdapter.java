package com.vluver.beta.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alexvasilkov.events.Events;
import com.alexvasilkov.gestures.animation.ViewPosition;
import com.vluver.beta.R;
import com.vluver.beta.activities.FullGestureImage;
import com.vluver.beta.activities.FullScreen;
import com.vluver.beta.base.CrossEvents;
import com.vluver.beta.model.Posts;
import com.vluver.beta.utils.GlideLoadImages;
import com.vluver.beta.utils.OnLoadMoreListener;
import java.util.List;


public class PostUserAdapter extends RecyclerView.Adapter {
    @NonNull
    private static Context contexto;
    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;
    private static List<Posts> postslist;
    private int visibleThreshold = 10;
    private int lastVisibleItem, totalItemCount;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;
    public PostUserAdapter(List<Posts> posts, RecyclerView recyclerView, Context context) {
        postslist = posts;
        contexto = context;
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {

            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView
                    .getLayoutManager();


            recyclerView
                    .addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrolled(RecyclerView recyclerView,
                                               int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);

                            totalItemCount = linearLayoutManager.getItemCount();
                            lastVisibleItem = linearLayoutManager
                                    .findLastVisibleItemPosition();
                            if (!loading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                                // End has been reached
                                // Do something
                                if (onLoadMoreListener != null) {
                                    onLoadMoreListener.onLoadMore();
                                }
                                loading = true;
                            }
                        }
                    });
        }
    }
    @Override
    public int getItemViewType(int position) {
        return postslist.get(position) != null ? VIEW_ITEM : VIEW_PROG;
    }
    public static ImageView getImageView(RecyclerView.ViewHolder holder) {
        return ((PostsViewHolder) holder).onlyimage;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.adapter_posts, parent, false);

            vh = new PostsViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.loading_layout, parent, false);

            vh = new PostsViewHolder.ProgressViewHolder(v);
        }
        return vh;
    }


    @SuppressLint("Range")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PostsViewHolder){
            //estado = items.get(i).getPost_id();
            // viewHolder.draweeView.getHierarchy().reset();
            Posts getpostsimet = (Posts) postslist.get(position);
            int numimg = getpostsimet.getNum_imgs();
            String avatar =  getpostsimet.getAvatar();
            GlideLoadImages.loadAvatar(contexto,avatar,((PostsViewHolder) holder).circle);
            ((PostsViewHolder) holder).onlyimage.setTag(R.id.tag_item, position);

            if (numimg == 1){
                ((PostsViewHolder) holder).it2.setVisibility(View.GONE);
                ((PostsViewHolder) holder).it3.setVisibility(View.GONE);
                ((PostsViewHolder) holder).it4.setVisibility(View.GONE);
                ((PostsViewHolder) holder).it5.setVisibility(View.GONE);
                ((PostsViewHolder) holder).onlyimage.setVisibility(View.VISIBLE);
                GlideLoadImages.onlyimage(contexto,getpostsimet.getPathimg().get(0),getpostsimet.getNameimg().get(0),((PostsViewHolder) holder).onlyimage);
            }else if (numimg == 2){
                ((PostsViewHolder) holder).it2.setVisibility(View.VISIBLE);
                ((PostsViewHolder) holder).it3.setVisibility(View.GONE);
                ((PostsViewHolder) holder).it4.setVisibility(View.GONE);
                ((PostsViewHolder) holder).it5.setVisibility(View.GONE);
                ((PostsViewHolder) holder).onlyimage.setVisibility(View.GONE);
                GlideLoadImages.loadImagesPosts(contexto,getpostsimet.getPathimg().get(0),getpostsimet.getNameimg().get(0),((PostsViewHolder) holder).it2img1);
                GlideLoadImages.loadImagesPosts(contexto,getpostsimet.getPathimg().get(1),getpostsimet.getNameimg().get(1),((PostsViewHolder) holder).it2img2);
            }else if (numimg == 3){
                ((PostsViewHolder) holder).it3.setVisibility(View.VISIBLE);
                ((PostsViewHolder) holder).it4.setVisibility(View.GONE);
                ((PostsViewHolder) holder).it5.setVisibility(View.GONE);
                ((PostsViewHolder) holder).it2.setVisibility(View.GONE);
                ((PostsViewHolder) holder).onlyimage.setVisibility(View.GONE);
                GlideLoadImages.loadImagesPosts(contexto,getpostsimet.getPathimg().get(0),getpostsimet.getNameimg().get(0),((PostsViewHolder) holder).it3img1);
                GlideLoadImages.loadImagesPosts(contexto,getpostsimet.getPathimg().get(1),getpostsimet.getNameimg().get(1),((PostsViewHolder) holder).it3img2);
                GlideLoadImages.loadImagesPosts(contexto,getpostsimet.getPathimg().get(2),getpostsimet.getNameimg().get(2),((PostsViewHolder) holder).it3img3);
            }else if (numimg == 4){
                ((PostsViewHolder) holder).it4.setVisibility(View.VISIBLE);
                ((PostsViewHolder) holder).it5.setVisibility(View.GONE);
                ((PostsViewHolder) holder).it2.setVisibility(View.GONE);
                ((PostsViewHolder) holder).it3.setVisibility(View.GONE);
                ((PostsViewHolder) holder).onlyimage.setVisibility(View.GONE);
                GlideLoadImages.loadImagesPosts(contexto,getpostsimet.getPathimg().get(0),getpostsimet.getNameimg().get(0),((PostsViewHolder) holder).it4img1);
                GlideLoadImages.loadImagesPosts(contexto,getpostsimet.getPathimg().get(1),getpostsimet.getNameimg().get(1),((PostsViewHolder) holder).it4img2);
                GlideLoadImages.loadImagesPosts(contexto,getpostsimet.getPathimg().get(2),getpostsimet.getNameimg().get(2),((PostsViewHolder) holder).it4img3);
                GlideLoadImages.loadImagesPosts(contexto,getpostsimet.getPathimg().get(3),getpostsimet.getNameimg().get(3),((PostsViewHolder) holder).it4img4);
            }else if (numimg > 4){
                ((PostsViewHolder) holder).it5.setVisibility(View.VISIBLE);
                ((PostsViewHolder) holder).it2.setVisibility(View.GONE);
                ((PostsViewHolder) holder).it3.setVisibility(View.GONE);
                ((PostsViewHolder) holder).it4.setVisibility(View.GONE);
                ((PostsViewHolder) holder).onlyimage.setVisibility(View.GONE);
                GlideLoadImages.loadImagesPosts(contexto,getpostsimet.getPathimg().get(0),getpostsimet.getNameimg().get(0),((PostsViewHolder) holder).it5img1);
                GlideLoadImages.loadImagesPosts(contexto,getpostsimet.getPathimg().get(1),getpostsimet.getNameimg().get(1),((PostsViewHolder) holder).it5img2);
                GlideLoadImages.loadImagesPosts(contexto,getpostsimet.getPathimg().get(2),getpostsimet.getNameimg().get(2),((PostsViewHolder) holder).it5img3);
                GlideLoadImages.loadImagesPosts(contexto,getpostsimet.getPathimg().get(3),getpostsimet.getNameimg().get(3),((PostsViewHolder) holder).it5img4);
                GlideLoadImages.loadImagesPosts(contexto,getpostsimet.getPathimg().get(4),getpostsimet.getNameimg().get(4),((PostsViewHolder) holder).it5img5);
                if (numimg > 5){
                    ((PostsViewHolder)holder).txtcount.setVisibility(View.VISIBLE);
                    ((PostsViewHolder)holder).txtcount.setText("+"+(numimg-5));
                    ((PostsViewHolder)holder).txtcount.setAlpha(75);
                }else{
                    ((PostsViewHolder)holder).txtcount.setVisibility(View.GONE);
                }
            }else{
                ((PostsViewHolder) holder).it2.setVisibility(View.GONE);
                ((PostsViewHolder) holder).it3.setVisibility(View.GONE);
                ((PostsViewHolder) holder).it4.setVisibility(View.GONE);
                ((PostsViewHolder) holder).it5.setVisibility(View.GONE);
                ((PostsViewHolder) holder).onlyimage.setVisibility(View.GONE);
            }

            ((PostsViewHolder) holder).nombres.setText(getpostsimet.getUser());


            /*String img = getpostsimet.getImage();
            Uri uripost = Uri.parse(img);

            if (!img.equals("")) {

                GenericDraweeHierarchyBuilder builder =
                        new GenericDraweeHierarchyBuilder(contexto.getResources());
                GenericDraweeHierarchy hierarchy = builder
                        //.setPlaceholderImage(height,ScalingUtils.ScaleType.FIT_CENTE)
                        .setPlaceholderImage(contexto.getResources().getDrawable(R.color.grey_transparent))
                        .build();

                //hierarchy.setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER);
                hierarchy.setActualImageFocusPoint(new PointF(0.5f,0f));
               ((PostsViewHolder) holder).draweeViewer.setImageURI(uripost);
               ((PostsViewHolder) holder).draweeViewer.setHierarchy(hierarchy);
               ((PostsViewHolder) holder).draweeViewer.setAspectRatio(1.15f);
            }else{
                ((PostsViewHolder) holder).draweeViewer.setVisibility(View.GONE);
            }*/
            ((PostsViewHolder) holder).dateTextView.setText(getpostsimet.getDate());

            String descripcion = getpostsimet.getDescription();
            if (descripcion != null) {
               /* descripcion.replace("  ", "\t");
                int countLines = descripcion.split("[\n][\r][\t][  ]").length;
                if (descripcion.length()> 150 || countLines > 4){
                    String nuevotextomaximo = descripcion.substring(0,153) + "...";
                    ((PostsViewHolder) holder).contentTextView.setText(nuevotextomaximo);
                    ((PostsViewHolder) holder).viewmore.setVisibility(View.VISIBLE);
                }else{
                    ((PostsViewHolder) holder).viewmore.setVisibility(View.GONE);
                    ((PostsViewHolder) holder).contentTextView.setText(getpostsimet.getDescription());

                }*/
                ((PostsViewHolder) holder).contentTextView.setText(getpostsimet.getDescription());
                ((PostsViewHolder) holder).contentTextView.setVisibility(View.VISIBLE);
            } else {
                ((PostsViewHolder) holder).viewmore.setVisibility(View.GONE);
                ((PostsViewHolder) holder).contentTextView.setVisibility(View.GONE);
            }

            ((PostsViewHolder) holder).num_likes.setText("" + getpostsimet.get_num_likes());

            if (getpostsimet.get_our_like()) {
                ((PostsViewHolder) holder).txtlike.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                ((PostsViewHolder) holder).txtlike.setTextColor(contexto.getResources().getColor(R.color.textlikeColorSelected));
                ((PostsViewHolder) holder).like.setImageResource(R.drawable.ic_heart_red);

            }
            else {
                ((PostsViewHolder) holder).txtlike.setTypeface(Typeface.DEFAULT);
                ((PostsViewHolder) holder).txtlike.setTextColor(contexto.getResources().getColor(android.R.color.tab_indicator_text));
                ((PostsViewHolder) holder).like.setImageResource(R.drawable.ic_heart_outline_grey);

            }
        }else {
            ((PostsViewHolder.ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }

    }
    public void setLoaded() {
        loading = false;

    }

    @Override
    public int getItemCount() {
        return postslist.size();
    }
    public void remove(int position) {
        postslist.remove(position);
        notifyItemRemoved(position);
    }

    public void clear() {
        postslist.clear();
        notifyDataSetChanged();
    }



    public void update(List<Posts> models) {
        postslist.clear();
        postslist.addAll(models);
        notifyDataSetChanged();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }


    public static class PostsViewHolder extends RecyclerView.ViewHolder implements PopupMenu.OnMenuItemClickListener, View.OnClickListener {
        String uid;
        TextView nombres;
        TextView contentTextView;
        TextView dateTextView;
        TextView num_likes;
        TextView num_comments;
        TextView txtlike;
        ImageButton opciones;
        ImageButton like;
        ImageButton comentar;
        ImageView circle;
        LinearLayout lnlike;
        Button viewmore;
        ImageView onlyimage;
        FrameLayout it2, it3, it4, it5;
        ImageView it2img1, it2img2, it3img1, it3img2, it3img3, it4img1, it4img2, it4img3, it4img4, it5img1, it5img2, it5img3, it5img4, it5img5;
        TextView txtcount;

        PostsViewHolder(View v) {
            super(v);
            nombres = (TextView) itemView.findViewById(R.id.nombres);
            like = (ImageButton) itemView.findViewById(R.id.btnLike);
            comentar = (ImageButton) itemView.findViewById(R.id.btncomment);
            contentTextView = (TextView) itemView.findViewById(R.id.textView8);
            dateTextView = (TextView) itemView.findViewById(R.id.textView10);
            num_comments = (TextView) itemView.findViewById(R.id.comentarios);
            num_likes = (TextView) itemView.findViewById(R.id.likes);
            opciones = (ImageButton) itemView.findViewById(R.id.options);
            lnlike = (LinearLayout) itemView.findViewById(R.id.lnlike);
            txtlike = (TextView) itemView.findViewById(R.id.txtlike);
            viewmore = (Button) itemView.findViewById(R.id.view_moretxt);
            onlyimage = (ImageView) itemView.findViewById(R.id.onlyimage);
            it2 = (FrameLayout) itemView.findViewById(R.id.item2);
            it3 = (FrameLayout) itemView.findViewById(R.id.item3);
            it4 = (FrameLayout) itemView.findViewById(R.id.item4);
            it5 = (FrameLayout) itemView.findViewById(R.id.item5);
            it2img1 = (ImageView) itemView.findViewById(R.id.item2id1);
            it2img2 = (ImageView) itemView.findViewById(R.id.item2id2);
            it3img1 = (ImageView) itemView.findViewById(R.id.item3id1);
            it3img2 = (ImageView) itemView.findViewById(R.id.item3id2);
            it3img3 = (ImageView) itemView.findViewById(R.id.item3id3);
            it4img1 = (ImageView) itemView.findViewById(R.id.item4id1);
            it4img2 = (ImageView) itemView.findViewById(R.id.item4id2);
            it4img3 = (ImageView) itemView.findViewById(R.id.item4id3);
            it4img4 = (ImageView) itemView.findViewById(R.id.item4id4);
            it5img1 = (ImageView) itemView.findViewById(R.id.item5id1);
            it5img2 = (ImageView) itemView.findViewById(R.id.item5id2);
            it5img3 = (ImageView) itemView.findViewById(R.id.item5id3);
            it5img4 = (ImageView) itemView.findViewById(R.id.item5id4);
            it5img5 = (ImageView) itemView.findViewById(R.id.item5id5);
            txtcount = (TextView) itemView.findViewById(R.id.tvCount);
            viewmore.setOnClickListener(this);
            opciones.setOnClickListener(this);
            //like.setOnClickListener(this);
            lnlike.setOnClickListener(this);
            circle = (ImageView) itemView.findViewById(R.id.user_profile);
            //draweeViewer = (WrapContentDraweeView) itemView.findViewById(R.id.my_image_view);
            comentar.setOnClickListener(this);
            onlyimage.setOnClickListener(this);
            it2img1.setOnClickListener(this);
            it2img2.setOnClickListener(this);
            it3img1.setOnClickListener(this);
            it3img2.setOnClickListener(this);
            it3img3.setOnClickListener(this);
            it4img1.setOnClickListener(this);
            it4img2.setOnClickListener(this);
            it4img3.setOnClickListener(this);
            it4img4.setOnClickListener(this);
            it5img1.setOnClickListener(this);
            it5img2.setOnClickListener(this);
            it5img3.setOnClickListener(this);
            it5img4.setOnClickListener(this);
            it5img5.setOnClickListener(this);
            onlyimage.getViewTreeObserver().addOnGlobalLayoutListener(this::onLayoutChanges);
            it2img1.getViewTreeObserver().addOnGlobalLayoutListener(this::onLayoutChanges);
            it2img2.getViewTreeObserver().addOnGlobalLayoutListener(this::onLayoutChanges);
            it3img1.getViewTreeObserver().addOnGlobalLayoutListener(this::onLayoutChanges);
            it3img2.getViewTreeObserver().addOnGlobalLayoutListener(this::onLayoutChanges);
            it3img3.getViewTreeObserver().addOnGlobalLayoutListener(this::onLayoutChanges);
            it4img1.getViewTreeObserver().addOnGlobalLayoutListener(this::onLayoutChanges);
            it4img2.getViewTreeObserver().addOnGlobalLayoutListener(this::onLayoutChanges);
            it4img3.getViewTreeObserver().addOnGlobalLayoutListener(this::onLayoutChanges);
            it4img4.getViewTreeObserver().addOnGlobalLayoutListener(this::onLayoutChanges);
            it5img1.getViewTreeObserver().addOnGlobalLayoutListener(this::onLayoutChanges);
            it5img2.getViewTreeObserver().addOnGlobalLayoutListener(this::onLayoutChanges);
            it5img3.getViewTreeObserver().addOnGlobalLayoutListener(this::onLayoutChanges);
            it5img4.getViewTreeObserver().addOnGlobalLayoutListener(this::onLayoutChanges);
            it5img5.getViewTreeObserver().addOnGlobalLayoutListener(this::onLayoutChanges);

            /*v.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(),
                            "Hola" ,
                            Toast.LENGTH_SHORT).show();

                }
            });*/
        }

        @Override
        public void onClick(View view) {
            ViewPosition position = ViewPosition.from(view);
            switch (view.getId()) {
                case R.id.options:
                    //openmenu(view);
                    break;
                case R.id.lnlike:
                    String s = postslist.get(getAdapterPosition()).get_post_id();
                    int num = postslist.get((getAdapterPosition())).get_num_likes();
                    if (postslist.get(getAdapterPosition()).get_our_like()) {
                        try {
                            int menos = num - 1;
                            postslist.get(getAdapterPosition()).set_num_likes(menos);
                            //_frag.delete_like(s);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        txtlike.setTypeface(Typeface.DEFAULT);
                        txtlike.setTextColor(contexto.getResources().getColor(android.R.color.tab_indicator_text));
                        num_likes.setText("" + postslist.get(getAdapterPosition()).get_num_likes());
                        Animation animation = AnimationUtils.loadAnimation(contexto, android.R.anim.fade_in);
                        like.setAnimation(animation);
                        //dislike lokk at me
                        like.setImageResource(R.drawable.ic_heart_outline_grey);
                        postslist.get(getAdapterPosition()).set_our_like(false);

                    } else {
                        try {
                            int mas = num + 1;
                            postslist.get(getAdapterPosition()).set_num_likes(mas);
                            //_frag.sendLike(s);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        txtlike.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                        txtlike.setTextColor(contexto.getResources().getColor(R.color.textlikeColorSelected));
                        num_likes.setText("" + postslist.get((getAdapterPosition())).get_num_likes());
                        Animation animation = AnimationUtils.loadAnimation(contexto, android.R.anim.fade_in);
                        like.setAnimation(animation);
                        like.setImageResource(R.drawable.ic_heart_red);
                        like.setColorFilter(Color.parseColor("#540bff"));
                        postslist.get(getAdapterPosition()).set_our_like(true);
                    }
                    break;
                case R.id.btncomment:
                    Bundle comentbundle = new Bundle();
                    //comentbundle.putInt("id_post",items.get((getAdapterPosition())).getPost_id());
                    comentbundle.putString("unique_id", uid);
                    //Intent intent = new Intent(context,Comentar.class);
                    //                    //intent.putExtras(comentbundle);
                    //                    //context.startActivity(intent);
                    break;
                case R.id.view_moretxt:
                    Toast.makeText(contexto, "" + postslist.get((getAdapterPosition())).getDescription(), Toast.LENGTH_SHORT).show();
                    break;
                case R.id.item2id1:
                    FullGestureImage.open(contexto, position, postslist.get(getPosition()).getPathimg(), postslist.get(getPosition()).getNameimg(), 0, "small_", 5);
                    break;
                case R.id.item2id2:
                    FullGestureImage.open(contexto, position, postslist.get(getPosition()).getPathimg(), postslist.get(getPosition()).getNameimg(), 1, "small_", 15);
                    break;
                case R.id.item3id1:
                    FullGestureImage.open(contexto, position, postslist.get(getPosition()).getPathimg(), postslist.get(getPosition()).getNameimg(), 0, "small_", 15);
                    break;
                case R.id.item3id2:
                    FullGestureImage.open(contexto, position, postslist.get(getPosition()).getPathimg(), postslist.get(getPosition()).getNameimg(), 1, "small_", 15);
                    break;
                case R.id.item3id3:
                    FullGestureImage.open(contexto, position, postslist.get(getPosition()).getPathimg(), postslist.get(getPosition()).getNameimg(), 2, "small_", 15);
                    break;
                case R.id.item4id1:
                    FullGestureImage.open(contexto, position, postslist.get(getPosition()).getPathimg(), postslist.get(getPosition()).getNameimg(), 0, "small_", 15);
                    break;
                case R.id.item4id2:
                    FullGestureImage.open(contexto, position, postslist.get(getPosition()).getPathimg(), postslist.get(getPosition()).getNameimg(), 1, "small_", 15);
                    break;
                case R.id.item4id3:
                    FullGestureImage.open(contexto, position, postslist.get(getPosition()).getPathimg(), postslist.get(getPosition()).getNameimg(), 2, "small_", 15);
                    break;
                case R.id.item4id4:
                    FullGestureImage.open(contexto, position, postslist.get(getPosition()).getPathimg(), postslist.get(getPosition()).getNameimg(), 3, "small_", 15);
                    break;
                case R.id.item5id1:
                    FullGestureImage.open(contexto, position, postslist.get(getPosition()).getPathimg(), postslist.get(getPosition()).getNameimg(), 0, "small_", 15);
                    break;
                case R.id.item5id2:
                    FullGestureImage.open(contexto, position, postslist.get(getPosition()).getPathimg(), postslist.get(getPosition()).getNameimg(), 1, "small_", 15);
                    break;
                case R.id.item5id3:
                    FullGestureImage.open(contexto, position, postslist.get(getPosition()).getPathimg(), postslist.get(getPosition()).getNameimg(), 2, "small_", 15);
                    break;
                case R.id.item5id4:
                    FullGestureImage.open(contexto, position, postslist.get(getPosition()).getPathimg(), postslist.get(getPosition()).getNameimg(), 3, "small_", 15);
                    break;
                case R.id.item5id5:
                    FullGestureImage.open(contexto, position, postslist.get(getPosition()).getPathimg(), postslist.get(getPosition()).getNameimg(), 4, "small_", 15);
                    break;
                case R.id.onlyimage:
                    FullGestureImage.open(contexto, position, postslist.get(getPosition()).getPathimg(), postslist.get(getPosition()).getNameimg(), 0, "", 1);
                    break;
            }
        }


        private void onLayoutChanges() {
            ViewPosition position = ViewPosition.from(onlyimage);
            Events.create(CrossEvents.POSITION_CHANGED).param(position).post();
        }

        @Events.Subscribe(CrossEvents.SHOW_IMAGE)
        public void showImage(boolean show) {
            // Fullscreen activity requested to show or hide original image
            onlyimage.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            return false;
        }


        interface OnPaintingClickListener {
            void onPaintingClick(int position);
        }

        public static class ProgressViewHolder extends RecyclerView.ViewHolder {
            public ProgressBar progressBar;

            public ProgressViewHolder(View v) {
                super(v);
                progressBar = (ProgressBar) v.findViewById(R.id.progressBar1);
            }
        }
    }

}
