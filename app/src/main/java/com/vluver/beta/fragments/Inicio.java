package com.vluver.beta.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.vluver.beta.R;
import com.vluver.beta.adapter.PostUserAdapter;
import com.vluver.beta.model.Posts;
import com.vluver.beta.serviceVolley.VolleySingleton;
import com.vluver.beta.utils.OnLoadMoreListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class Inicio extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    View view;
    private FirebaseAuth mAuth;
    public SwipeRefreshLayout mSwipe;
    RequestQueue mQueue;
    public static RecyclerView recyclerView;
    private PostUserAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    List<Posts> GetDataAdapter1;
    protected Handler handler;
    int fromId;
    int case3;


    public Inicio() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_inicio, container, false);
        mQueue = Volley.newRequestQueue(getContext());
        handler = new Handler();
        mAuth = FirebaseAuth.getInstance();
        //img_one = new int[]{1};

        mSwipe = view.findViewById(R.id.reload);
        recyclerView = (RecyclerView) view.findViewById(R.id.rv_inicio);
        GetDataAdapter1 = new ArrayList<Posts>();
        recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new PostUserAdapter(GetDataAdapter1,recyclerView,getContext());
        recyclerView.setAdapter(mAdapter);
        recyclerView.setItemViewCacheSize(50);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        /*recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                try {
                    if (dy > 0 && floatingActionButton.getVisibility() == View.VISIBLE) {
                        floatingActionButton.hide();
                    } else if (dy < 0 && floatingActionButton.getVisibility() != View.VISIBLE) {
                        floatingActionButton.show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });*/



        mSwipe.setColorScheme(R.color.colorAccent,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        mSwipe.setRefreshing(true);
        mAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                GetDataAdapter1.add(null);
                if (GetDataAdapter1.size() > 10){
                    mAdapter.notifyItemInserted(GetDataAdapter1.size());
                }

                String url = "http://vluver.com/mobile/get/getAllPostsByUser.php?user_id="+mAuth.getUid()+"&from="+fromId+"&casetype=2";
                VolleySingleton.
                        getInstance(getContext()).
                        addToRequestQueue(
                                new JsonObjectRequest(
                                        Request.Method.GET, url, (JSONObject) null,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                // Procesar la respuesta Json
                                                try {

                                                    GetDataAdapter1.remove(GetDataAdapter1.size() - 1);
                                                    mAdapter.notifyItemRemoved(GetDataAdapter1.size());
                                                    boolean error = response.getBoolean("error");

                                                    if (!error) {

                                                        fromId = response.getInt("nmen");

                                                        JSONObject postt = response.getJSONObject("post");
                                                        JSONArray pray = postt.getJSONArray("pid");
                                                        for (int i = 0; i < pray.length(); i++) {
                                                            JSONArray objPid = postt.getJSONArray("pid");
                                                            final String pid = objPid.getString(i);
                                                            JSONArray objname = postt.getJSONArray("name");
                                                            String name = objname.getString(i);

                                                            JSONArray objuid = postt.getJSONArray("unique_id");
                                                            final String uid = objuid.getString(i);

                                                            JSONArray objuserimage = postt.getJSONArray("avatar");
                                                            String userimage = objuserimage.getString(i);

                                                            JSONArray objcontent = postt.getJSONArray("content");
                                                            final String content = objcontent.getString(i);

                                                            JSONArray totalimg = postt.getJSONArray("totalimg");
                                                            int ttimg = totalimg.getInt(i);
                                                            JSONArray pathimgJSON = postt.getJSONArray("pathimg");
                                                            ArrayList<String> pathimg = new ArrayList<>();
                                                            JSONArray nameimgJSON = postt.getJSONArray("nameimg");
                                                            ArrayList<String> nameimg = new ArrayList<>();
                                                            pathimg.clear();
                                                            nameimg.clear();
                                                            if (ttimg > 0){
                                                                for (int con = 0; con < ttimg;con++){
                                                                    JSONArray path = pathimgJSON.getJSONArray(i);
                                                                    final String pathString = path.getString(con);
                                                                    pathimg.add(pathString);

                                                                    JSONArray nm = nameimgJSON.getJSONArray(i);
                                                                    final String nmString = nm.getString(con);
                                                                    nameimg.add(nmString);
                                                                }

                                                            }
                                                            JSONArray objdate = postt.getJSONArray("fecha");
                                                            String date = (String) objdate.get(i);

                                                            JSONArray objlike = postt.getJSONArray("estadolike");
                                                            final boolean mylike = objlike.getBoolean(i);

                                                            JSONArray objNlikes = postt.getJSONArray("likes");
                                                            final int n_likes = objNlikes.getInt(i);

                                                            JSONArray objNcomments = postt.getJSONArray("numcomments");
                                                            final int n_comments = objNcomments.getInt(i);
                                                            Posts posts = new Posts();
                                                            posts.set_post_id(uid);
                                                            posts.setUser(name);
                                                            posts.setAvatar(userimage);
                                                            posts.setPathimg(pathimg);
                                                            posts.setNameimg(nameimg);
                                                            posts.setDescription(content);
                                                            posts.setDate(date);
                                                            posts.set_our_like(mylike);
                                                            posts.setNum_imgs(ttimg);
                                                            posts.set_num_likes(n_likes);
                                                            GetDataAdapter1.add(posts);
                                                        }

                                                        mAdapter.notifyDataSetChanged();
                                                        mAdapter.setLoaded();

                                                    } else {
                                                        String errorMsg = response.getString("error_msg");
                                                        //Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                                                        getNewsPosts();
                                                    }

                                                } catch (JSONException e) {
                                                    GetDataAdapter1.remove(GetDataAdapter1.size() - 1);
                                                    mAdapter.notifyItemRemoved(GetDataAdapter1.size());
                                                    Toast.makeText(getContext(), ""+e, Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                checkerror(error);
                                                mSwipe.setRefreshing(false);
                                                GetDataAdapter1.remove(GetDataAdapter1.size() - 1);
                                                mAdapter.notifyItemRemoved(GetDataAdapter1.size());

                                            }
                                        }
                                )
                        );
            }
        });




       /* recyclerViewadapter = new PostUserAdapter(GetDataAdapter1, getContext(), Inicio.this);
        recyclerView.setAdapter(recyclerViewadapter);
        recyclerViewlayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(recyclerViewlayoutManager);*/

        // Setting up background animation during image transition
        mSwipe.setOnRefreshListener(this);
        //Toast.makeText(getContext(),mAuth.getUid(), Toast.LENGTH_SHORT).show();
        getPostsInicial();

        return view;
    }



    private void checkerror(VolleyError error){
        if( error instanceof NetworkError) {
            Toast.makeText(getContext(), "Sin  coneccion a internet", Toast.LENGTH_SHORT).show();
        } else if( error instanceof ServerError) {
            Toast.makeText(getContext(), "Servidores fallado", Toast.LENGTH_SHORT).show();
        } else if( error instanceof AuthFailureError) {
            Toast.makeText(getContext(), "Error desconocido", Toast.LENGTH_SHORT).show();
        } else if( error instanceof ParseError) {
            Toast.makeText(getContext(), "Error al parsear datos...", Toast.LENGTH_SHORT).show();
        } else if( error instanceof TimeoutError) {
            Toast.makeText(getContext(), "Tiempo agotado!", Toast.LENGTH_SHORT).show();
        }
    }

    public void regresarnested(){
        //recyclerView.scrollToPosition(0);
        //floatingActionButton.show();
        //smoothScroller.setTargetPosition(0);
        //recyclerViewlayoutManager.startSmoothScroll(smoothScroller);
        recyclerView.smoothScrollToPosition(-5);
    }

    public void cargartodo(){
        if (recyclerView.computeVerticalScrollOffset() > 1000){
            regresarnested();
            /*if (!mSwipe.isRefreshing()){
                if (mSwipe.isRefreshing()){
                    mSwipe.setRefreshing(false);
                }else{
                    mSwipe.setRefreshing(true);
                }
                  getPostsInicial();
            }*/
        }/*else{
            if (!mSwipe.isRefreshing()){
                if (mSwipe.isRefreshing()){
                    mSwipe.setRefreshing(false);
                }else{
                    mSwipe.setRefreshing(true);
                }
                getPostsInicial();
            }
        }*/
    }
    public boolean casomayor(){

        if (recyclerView.computeVerticalScrollOffset() > 1000){
            regresarnested();
            if (!mSwipe.isRefreshing()){
                if (mSwipe.isRefreshing()){
                    mSwipe.setRefreshing(false);
                }else{
                    mSwipe.setRefreshing(true);
                }
                getPostsInicial();
            }
            return true;
        }else{
            return false;
        }
    }

    private void getPostsInicial(){
        String url = "http://vluver.com/mobile/get/getAllPostsByUser.php?user_id="+mAuth.getUid()+"&from="+0+"&casetype=1";
        VolleySingleton.
                getInstance(getContext()).
                addToRequestQueue(
                        new JsonObjectRequest(
                                Request.Method.GET, url, (JSONObject) null,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        // Procesar la respuesta Json
                                        try {

                                            boolean error = response.getBoolean("error");
                                            if (GetDataAdapter1 != null){
                                                GetDataAdapter1.clear();
                                                mAdapter.clear();
                                            }

                                            if (!error) {
                                                JSONObject postt = response.getJSONObject("post");

                                                JSONArray pray = postt.getJSONArray("pid");
                                                fromId = response.getInt("nmen");
                                                case3= response.getInt("nmay");
                                                for (int i = 0; i < pray.length(); i++) {
                                                    JSONArray objPid = postt.getJSONArray("pid");
                                                    final String pid = objPid.getString(i);

                                                    JSONArray objname = postt.getJSONArray("name");
                                                    String name = objname.getString(i);

                                                    JSONArray objuid = postt.getJSONArray("unique_id");
                                                    final String uid = objuid.getString(i);

                                                    JSONArray objuserimage = postt.getJSONArray("avatar");
                                                    String userimage = objuserimage.getString(i);

                                                    JSONArray objcontent = postt.getJSONArray("content");
                                                    final String content = objcontent.getString(i);

                                                    JSONArray totalimg = postt.getJSONArray("totalimg");
                                                    int ttimg = totalimg.getInt(i);
                                                    JSONArray pathimgJSON = postt.getJSONArray("pathimg");
                                                    ArrayList<String> pathimg = new ArrayList<>();
                                                    JSONArray nameimgJSON = postt.getJSONArray("nameimg");
                                                    ArrayList<String> nameimg = new ArrayList<>();
                                                    pathimg.clear();
                                                    nameimg.clear();
                                                    if (ttimg > 0){
                                                        for (int con = 0; con < ttimg;con++){
                                                            JSONArray path = pathimgJSON.getJSONArray(i);
                                                            final String pathString = path.getString(con);
                                                            pathimg.add(pathString);

                                                            JSONArray nm = nameimgJSON.getJSONArray(i);
                                                            final String nmString = nm.getString(con);
                                                            nameimg.add(nmString);
                                                        }

                                                    }
                                                    JSONArray objdate = postt.getJSONArray("fecha");
                                                    String date = (String) objdate.get(i);
                                                    JSONArray objlike = postt.getJSONArray("estadolike");
                                                    final boolean mylike = objlike.getBoolean(i);
                                                    JSONArray objNlikes = postt.getJSONArray("likes");
                                                    final int n_likes = objNlikes.getInt(i);
                                                    JSONArray objNcomments = postt.getJSONArray("numcomments");
                                                    final int n_comments = objNcomments.getInt(i);
                                                    Posts posts = new Posts();
                                                    posts.set_post_id(uid);
                                                    posts.setUser(name);
                                                    posts.setAvatar(userimage);
                                                    posts.setPathimg(pathimg);
                                                    posts.setNameimg(nameimg);
                                                    posts.setDescription(content);
                                                    posts.setDate(date);
                                                    posts.set_our_like(mylike);
                                                    posts.setNum_imgs(ttimg);
                                                    posts.set_num_likes(n_likes);
                                                    GetDataAdapter1.add(posts);
                                                    //mAdapter.notifyItemInserted(GetDataAdapter1.size());
                                                }
                                                mAdapter.notifyDataSetChanged();
                                                mAdapter.setLoaded();
                                                recyclerView.setVisibility(View.VISIBLE);
                                                recyclerView.setVisibility(View.VISIBLE);
                                                mSwipe.setRefreshing(false);

                                            } else {
                                                String errorMsg = response.getString("error_msg");
                                                recyclerView.setVisibility(View.VISIBLE);
                                                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                                                mSwipe.setRefreshing(false);
                                            }

                                        } catch (JSONException e) {
                                            Toast.makeText(getContext(), ""+e, Toast.LENGTH_SHORT).show();
                                            mSwipe.setRefreshing(false);
                                        }
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        checkerror(error);
                                        mSwipe.setRefreshing(false);


                                    }
                                }
                        )
                );

    }

    private void getNewsPosts(){
        GetDataAdapter1.add(null);
        if (GetDataAdapter1.size() > 7){
            mAdapter.notifyItemInserted(GetDataAdapter1.size());
        }
        String url = "http://vluver.com/mobile/get/getAllPostsByUser.php?user_id="+mAuth.getUid()+"&from="+case3+"&casetype=3";
        VolleySingleton.
                getInstance(getContext()).
                addToRequestQueue(
                        new JsonObjectRequest(
                                Request.Method.GET, url, (JSONObject) null,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        // Procesar la respuesta Json
                                        try {

                                            boolean error = response.getBoolean("error");
                                            GetDataAdapter1.remove(GetDataAdapter1.size() - 1);
                                            mAdapter.notifyItemRemoved(GetDataAdapter1.size());
                                            if (!error) {
                                                JSONObject postt = response.getJSONObject("post");

                                                JSONArray pray = postt.getJSONArray("pid");
                                                //fromId = response.getInt("nmen");
                                                case3 = response.getInt("nmay");

                                                for (int i = 0; i < pray.length(); i++) {
                                                    JSONArray objPid = postt.getJSONArray("pid");
                                                    final String pid = objPid.getString(i);

                                                    JSONArray objname = postt.getJSONArray("name");
                                                    String name = objname.getString(i);

                                                    JSONArray objuid = postt.getJSONArray("unique_id");
                                                    final String uid = objuid.getString(i);

                                                    JSONArray objuserimage = postt.getJSONArray("avatar");
                                                    String userimage = objuserimage.getString(i);

                                                    JSONArray objcontent = postt.getJSONArray("content");
                                                    final String content = objcontent.getString(i);

                                                    JSONArray totalimg = postt.getJSONArray("totalimg");
                                                    int ttimg = totalimg.getInt(i);
                                                    JSONArray pathimgJSON = postt.getJSONArray("pathimg");
                                                    ArrayList<String> pathimg = new ArrayList<>();
                                                    JSONArray nameimgJSON = postt.getJSONArray("nameimg");
                                                    ArrayList<String> nameimg = new ArrayList<>();
                                                    pathimg.clear();
                                                    nameimg.clear();
                                                    if (ttimg > 0){
                                                        for (int con = 0; con < ttimg;con++){
                                                            JSONArray path = pathimgJSON.getJSONArray(i);
                                                            final String pathString = path.getString(con);
                                                            pathimg.add(pathString);

                                                            JSONArray nm = nameimgJSON.getJSONArray(i);
                                                            final String nmString = nm.getString(con);
                                                            nameimg.add(nmString);
                                                        }

                                                    }
                                                    JSONArray objdate = postt.getJSONArray("fecha");
                                                    String date = (String) objdate.get(i);

                                                    JSONArray objlike = postt.getJSONArray("estadolike");
                                                    final boolean mylike = objlike.getBoolean(i);

                                                    JSONArray objNlikes = postt.getJSONArray("likes");
                                                    final int n_likes = objNlikes.getInt(i);

                                                    JSONArray objNcomments = postt.getJSONArray("numcomments");
                                                    final int n_comments = objNcomments.getInt(i);
                                                    Posts posts = new Posts();
                                                    posts.set_post_id(uid);
                                                    posts.setUser(name);
                                                    posts.setAvatar(userimage);
                                                    posts.setPathimg(pathimg);
                                                    posts.setNameimg(nameimg);
                                                    posts.setDescription(content);
                                                    posts.setDate(date);
                                                    posts.set_our_like(mylike);
                                                    posts.setNum_imgs(ttimg);
                                                    posts.set_num_likes(n_likes);
                                                    GetDataAdapter1.add(posts);
                                                    //mAdapter.notifyItemInserted(GetDataAdapter1.size());
                                                }
                                                mAdapter.notifyDataSetChanged();
                                                mAdapter.setLoaded();
                                                recyclerView.setVisibility(View.VISIBLE);
                                                mSwipe.setRefreshing(false);

                                            } else {

                                                String errorMsg = response.getString("error_msg");
                                                recyclerView.setVisibility(View.VISIBLE);
                                                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                                                mSwipe.setRefreshing(false);
                                            }

                                        } catch (JSONException e) {
                                            Toast.makeText(getContext(), ""+e, Toast.LENGTH_SHORT).show();
                                            mSwipe.setRefreshing(false);
                                            GetDataAdapter1.remove(GetDataAdapter1.size() - 1);
                                            mAdapter.notifyItemRemoved(GetDataAdapter1.size());
                                        }
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        checkerror(error);
                                        mSwipe.setRefreshing(false);

                                    }
                                }
                        )
                );

    }

    public void sendLike(String post_id){
        String user_id = mAuth.getUid();

        /*VolleySingleton.
                getInstance(getContext()).
                addToRequestQueue(
                        new JsonObjectRequest(
                                Request.Method.GET, url, (JSONObject) null,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        // Procesar la respuesta Json
                                        procesarRespuesta(response);
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Toast.makeText(getContext(), "Internet no disponible", Toast.LENGTH_SHORT).show();
                                        mSwipe.setRefreshing(false);
                                    }
                                }
                        )
                );*/
    }

    @Override
    public void onRefresh() {
        getPostsInicial();
    }


}