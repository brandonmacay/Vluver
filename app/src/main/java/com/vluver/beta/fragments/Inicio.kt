package com.vluver.beta.fragments


import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import com.android.volley.AuthFailureError
import com.android.volley.NetworkError
import com.android.volley.ParseError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.ServerError
import com.android.volley.TimeoutError
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.vluver.beta.R
import com.vluver.beta.model.Posts
import com.vluver.beta.serviceVolley.VolleySingleton

import org.json.JSONException
import org.json.JSONObject

import java.util.ArrayList

import android.support.constraint.Constraints.TAG
import com.vluver.beta.adapter.PostUserAdapter


/**
 * A simple [Fragment] subclass.
 */
class Inicio : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    internal lateinit var view: View
    private var mAuth: FirebaseAuth? = null
    lateinit var mSwipe: SwipeRefreshLayout
    internal lateinit var mQueue: RequestQueue
    private var mAdapter: PostUserAdapter? = null
    private var mLayoutManager: LinearLayoutManager? = null
    private  var GetDataAdapter1: ArrayList<Posts>? = null
    protected lateinit var handler: Handler
    internal var fromId: Int = 0
    internal var case3: Int = 0


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_inicio, container, false)
        mQueue = Volley.newRequestQueue(context!!)
        handler = Handler()
        mAuth = FirebaseAuth.getInstance()
        //img_one = new int[]{1};

        mSwipe = view.findViewById(R.id.reload)
        recyclerView = view.findViewById<View>(R.id.rv_inicio) as RecyclerView
        GetDataAdapter1 = ArrayList()
        recyclerView.setHasFixedSize(true)
        mLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = mLayoutManager
        mAdapter = PostUserAdapter(GetDataAdapter1, recyclerView, context)
        recyclerView.adapter = mAdapter
        recyclerView.setItemViewCacheSize(50)
        recyclerView.isDrawingCacheEnabled = true
        recyclerView.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
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
                android.R.color.holo_blue_dark)
        mSwipe.isRefreshing = true
        mAdapter!!.setOnLoadMoreListener {
            val posts = Posts()
            GetDataAdapter1!!.add(posts)
            if (GetDataAdapter1!!.size > 10) {
                mAdapter!!.notifyItemInserted(GetDataAdapter1!!.size)
            }

            val url = "http://vluver.com/mobile/get/getAllPostsByUser.php?user_id=" + mAuth!!.uid + "&from=" + fromId + "&casetype=2"
            VolleySingleton.getInstance(context).addToRequestQueue(
                    JsonObjectRequest(
                            Request.Method.GET, url, null as JSONObject?,
                            Response.Listener { response ->
                                // Procesar la respuesta Json
                                try {

                                    GetDataAdapter1!!.removeAt(GetDataAdapter1!!.size - 1)
                                    mAdapter!!.notifyItemRemoved(GetDataAdapter1!!.size)
                                    val error = response.getBoolean("error")

                                    if (!error) {

                                        fromId = response.getInt("nmen")

                                        val postt = response.getJSONObject("post")
                                        val pray = postt.getJSONArray("pid")
                                        for (i in 0 until pray.length()) {
                                            val objPid = postt.getJSONArray("pid")
                                            val pid = objPid.getString(i)
                                            val objname = postt.getJSONArray("name")
                                            val name = objname.getString(i)

                                            val objuid = postt.getJSONArray("unique_id")
                                            val uid = objuid.getString(i)

                                            val objuserimage = postt.getJSONArray("avatar")
                                            val userimage = objuserimage.getString(i)

                                            val objcontent = postt.getJSONArray("content")
                                            val content = objcontent.getString(i)

                                            val totalimg = postt.getJSONArray("totalimg")
                                            val ttimg = totalimg.getInt(i)
                                            val pathimgJSON = postt.getJSONArray("pathimg")
                                            val pathimg = ArrayList<String>()
                                            val nameimgJSON = postt.getJSONArray("nameimg")
                                            val nameimg = ArrayList<String>()
                                            pathimg.clear()
                                            nameimg.clear()
                                            if (ttimg > 0) {
                                                for (con in 0 until ttimg) {
                                                    val path = pathimgJSON.getJSONArray(i)
                                                    val pathString = path.getString(con)
                                                    pathimg.add(pathString)

                                                    val nm = nameimgJSON.getJSONArray(i)
                                                    val nmString = nm.getString(con)
                                                    nameimg.add(nmString)
                                                }

                                            }
                                            val objdate = postt.getJSONArray("fecha")
                                            val date = objdate.get(i) as String

                                            val objlike = postt.getJSONArray("estadolike")
                                            val mylike = objlike.getBoolean(i)

                                            val objNlikes = postt.getJSONArray("likes")
                                            val n_likes = objNlikes.getInt(i)

                                            val objNcomments = postt.getJSONArray("numcomments")
                                            val n_comments = objNcomments.getInt(i)
                                            val posts = Posts()
                                            posts._post_id = uid
                                            posts.user = name
                                            posts.avatar = userimage
                                            posts.pathimg = pathimg
                                            posts.nameimg = nameimg
                                            posts.description = content
                                            posts.date = date
                                            posts._our_like = mylike
                                            posts.num_imgs = ttimg
                                            posts._num_likes = n_likes
                                            GetDataAdapter1!!.add(posts)
                                        }

                                        mAdapter!!.notifyDataSetChanged()
                                        mAdapter!!.setLoaded()

                                    } else {
                                        val errorMsg = response.getString("error_msg")
                                        //Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                                        getNewsPosts()
                                    }

                                } catch (e: JSONException) {
                                    Log.d(TAG, e.message)
                                    GetDataAdapter1!!.removeAt(GetDataAdapter1!!.size - 1)
                                    mAdapter!!.notifyItemRemoved(GetDataAdapter1!!.size)
                                    Toast.makeText(context, "" + e, Toast.LENGTH_SHORT).show()
                                }
                            },
                            Response.ErrorListener { error ->
                                checkerror(error)
                                mSwipe.isRefreshing = false
                                GetDataAdapter1!!.removeAt(GetDataAdapter1!!.size - 1)
                                mAdapter!!.notifyItemRemoved(GetDataAdapter1!!.size)
                            }
                    )
            )
        }


        /* recyclerViewadapter = new PostUserAdapter(GetDataAdapter1, getContext(), Inicio.this);
        recyclerView.setAdapter(recyclerViewadapter);
        recyclerViewlayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(recyclerViewlayoutManager);*/

        // Setting up background animation during image transition
        mSwipe.setOnRefreshListener(this)
        //Toast.makeText(getContext(),mAuth.getUid(), Toast.LENGTH_SHORT).show();
        getPostsInicial()

        return view
    }


    private fun checkerror(error: VolleyError) {
        if (error is NetworkError) {
            Toast.makeText(context, "Sin  coneccion a internet", Toast.LENGTH_SHORT).show()
        } else if (error is ServerError) {
            Toast.makeText(context, "Servidores fallado", Toast.LENGTH_SHORT).show()
        } else if (error is AuthFailureError) {
            Toast.makeText(context, "Error desconocido", Toast.LENGTH_SHORT).show()
        } else if (error is ParseError) {
            Toast.makeText(context, "Error al parsear datos...", Toast.LENGTH_SHORT).show()
        } else if (error is TimeoutError) {
            Toast.makeText(context, "Tiempo agotado!", Toast.LENGTH_SHORT).show()
        }
    }

    fun regresarnested() {
        //recyclerView.scrollToPosition(0);
        //floatingActionButton.show();
        //smoothScroller.setTargetPosition(0);
        //recyclerViewlayoutManager.startSmoothScroll(smoothScroller);
        recyclerView.smoothScrollToPosition(-5)
    }

    fun cargartodo() {
        if (recyclerView.computeVerticalScrollOffset() > 1000) {
            regresarnested()
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

    fun casomayor(): Boolean {

        if (recyclerView.computeVerticalScrollOffset() > 1000) {
            regresarnested()
            if (!mSwipe.isRefreshing) {
                if (mSwipe.isRefreshing) {
                    mSwipe.isRefreshing = false
                } else {
                    mSwipe.isRefreshing = true
                }
                getPostsInicial()
            }
            return true
        } else {
            return false
        }
    }

    private fun getPostsInicial() {
        val url = "http://vluver.com/mobile/get/getAllPostsByUser.php?user_id=" + mAuth!!.uid + "&from=" + 0 + "&casetype=1"
        VolleySingleton.getInstance(context).addToRequestQueue(
                JsonObjectRequest(
                        Request.Method.GET, url, null as JSONObject?,
                        Response.Listener { response ->
                            // Procesar la respuesta Json
                            try {

                                val error = response.getBoolean("error")
                                if (GetDataAdapter1 != null) {
                                    GetDataAdapter1!!.clear()
                                    mAdapter!!.clear()
                                }

                                if (!error) {
                                    val postt = response.getJSONObject("post")

                                    val pray = postt.getJSONArray("pid")
                                    fromId = response.getInt("nmen")
                                    case3 = response.getInt("nmay")
                                    for (i in 0 until pray.length()) {
                                        val objPid = postt.getJSONArray("pid")
                                        val pid = objPid.getString(i)

                                        val objname = postt.getJSONArray("name")
                                        val name = objname.getString(i)

                                        val objuid = postt.getJSONArray("unique_id")
                                        val uid = objuid.getString(i)

                                        val objuserimage = postt.getJSONArray("avatar")
                                        val userimage = objuserimage.getString(i)

                                        val objcontent = postt.getJSONArray("content")
                                        val content = objcontent.getString(i)

                                        val totalimg = postt.getJSONArray("totalimg")
                                        val ttimg = totalimg.getInt(i)
                                        val pathimgJSON = postt.getJSONArray("pathimg")
                                        val pathimg = ArrayList<String>()
                                        val nameimgJSON = postt.getJSONArray("nameimg")
                                        val nameimg = ArrayList<String>()
                                        pathimg.clear()
                                        nameimg.clear()
                                        if (ttimg > 0) {
                                            for (con in 0 until ttimg) {
                                                val path = pathimgJSON.getJSONArray(i)
                                                val pathString = path.getString(con)
                                                pathimg.add(pathString)

                                                val nm = nameimgJSON.getJSONArray(i)
                                                val nmString = nm.getString(con)
                                                nameimg.add(nmString)
                                            }

                                        }
                                        val objdate = postt.getJSONArray("fecha")
                                        val date = objdate.get(i) as String
                                        val objlike = postt.getJSONArray("estadolike")
                                        val mylike = objlike.getBoolean(i)
                                        val objNlikes = postt.getJSONArray("likes")
                                        val n_likes = objNlikes.getInt(i)
                                        val objNcomments = postt.getJSONArray("numcomments")
                                        val n_comments = objNcomments.getInt(i)
                                        val posts = Posts()
                                        posts._post_id = uid
                                        posts.user = name
                                        posts.avatar = userimage
                                        posts.pathimg = pathimg
                                        posts.nameimg = nameimg
                                        posts.description = content
                                        posts.date = date
                                        posts._our_like = mylike
                                        posts.num_imgs = ttimg
                                        posts._num_likes = n_likes
                                        GetDataAdapter1!!.add(posts)
                                        //mAdapter.notifyItemInserted(GetDataAdapter1.size());
                                    }
                                    mAdapter!!.notifyDataSetChanged()
                                    mAdapter!!.setLoaded()
                                    recyclerView.visibility = View.VISIBLE
                                    recyclerView.visibility = View.VISIBLE
                                    mSwipe.isRefreshing = false

                                } else {
                                    val errorMsg = response.getString("error_msg")
                                    recyclerView.visibility = View.VISIBLE
                                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                    mSwipe.isRefreshing = false
                                }

                            } catch (e: JSONException) {
                                Log.d(TAG, e.message)
                                Toast.makeText(context, "" + e, Toast.LENGTH_SHORT).show()
                                mSwipe.isRefreshing = false
                            }
                        },
                        Response.ErrorListener { error ->
                            checkerror(error)
                            mSwipe.isRefreshing = false
                        }
                )
        )

    }

    private fun getNewsPosts() {
        val posts = Posts()
        GetDataAdapter1!!.add(posts)
        if (GetDataAdapter1!!.size > 7) {
            mAdapter!!.notifyItemInserted(GetDataAdapter1!!.size)
        }
        val url = "http://vluver.com/mobile/get/getAllPostsByUser.php?user_id=" + mAuth!!.uid + "&from=" + case3 + "&casetype=3"
        VolleySingleton.getInstance(context).addToRequestQueue(
                JsonObjectRequest(
                        Request.Method.GET, url, null as JSONObject?,
                        Response.Listener { response ->
                            // Procesar la respuesta Json
                            try {

                                val error = response.getBoolean("error")
                                GetDataAdapter1!!.removeAt(GetDataAdapter1!!.size - 1)
                                mAdapter!!.notifyItemRemoved(GetDataAdapter1!!.size)
                                if (!error) {
                                    val postt = response.getJSONObject("post")

                                    val pray = postt.getJSONArray("pid")
                                    //fromId = response.getInt("nmen");
                                    case3 = response.getInt("nmay")

                                    for (i in 0 until pray.length()) {
                                        val objPid = postt.getJSONArray("pid")
                                        val pid = objPid.getString(i)

                                        val objname = postt.getJSONArray("name")
                                        val name = objname.getString(i)

                                        val objuid = postt.getJSONArray("unique_id")
                                        val uid = objuid.getString(i)

                                        val objuserimage = postt.getJSONArray("avatar")
                                        val userimage = objuserimage.getString(i)

                                        val objcontent = postt.getJSONArray("content")
                                        val content = objcontent.getString(i)

                                        val totalimg = postt.getJSONArray("totalimg")
                                        val ttimg = totalimg.getInt(i)
                                        val pathimgJSON = postt.getJSONArray("pathimg")
                                        val pathimg = ArrayList<String>()
                                        val nameimgJSON = postt.getJSONArray("nameimg")
                                        val nameimg = ArrayList<String>()
                                        pathimg.clear()
                                        nameimg.clear()
                                        if (ttimg > 0) {
                                            for (con in 0 until ttimg) {
                                                val path = pathimgJSON.getJSONArray(i)
                                                val pathString = path.getString(con)
                                                pathimg.add(pathString)

                                                val nm = nameimgJSON.getJSONArray(i)
                                                val nmString = nm.getString(con)
                                                nameimg.add(nmString)
                                            }

                                        }
                                        val objdate = postt.getJSONArray("fecha")
                                        val date = objdate.get(i) as String

                                        val objlike = postt.getJSONArray("estadolike")
                                        val mylike = objlike.getBoolean(i)

                                        val objNlikes = postt.getJSONArray("likes")
                                        val n_likes = objNlikes.getInt(i)

                                        val objNcomments = postt.getJSONArray("numcomments")
                                        val n_comments = objNcomments.getInt(i)
                                        val posts = Posts()
                                        posts._post_id = uid
                                        posts.user = name
                                        posts.avatar = userimage
                                        posts.pathimg = pathimg
                                        posts.nameimg = nameimg
                                        posts.description = content
                                        posts.date = date
                                        posts._our_like = mylike
                                        posts.num_imgs = ttimg
                                        posts._num_likes = n_likes
                                        GetDataAdapter1!!.add(posts)
                                        //mAdapter.notifyItemInserted(GetDataAdapter1.size());
                                    }
                                    mAdapter!!.notifyDataSetChanged()
                                    mAdapter!!.setLoaded()
                                    recyclerView.visibility = View.VISIBLE
                                    mSwipe.isRefreshing = false

                                } else {

                                    val errorMsg = response.getString("error_msg")
                                    recyclerView.visibility = View.VISIBLE
                                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                    mSwipe.isRefreshing = false
                                }

                            } catch (e: JSONException) {
                                Log.d(TAG, e.message)
                                Toast.makeText(context, "" + e, Toast.LENGTH_SHORT).show()
                                mSwipe.isRefreshing = false
                                GetDataAdapter1!!.removeAt(GetDataAdapter1!!.size - 1)
                                mAdapter!!.notifyItemRemoved(GetDataAdapter1!!.size)
                            }
                        },
                        Response.ErrorListener { error ->
                            checkerror(error)
                            mSwipe.isRefreshing = false
                        }
                )
        )

    }

    fun sendLike(post_id: String) {
        val user_id = mAuth!!.uid

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

    override fun onRefresh() {
        getPostsInicial()
    }

    companion object {
        lateinit var recyclerView: RecyclerView
    }


}// Required empty public constructor
