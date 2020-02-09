package com.vluver.beta.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.vluver.beta.R;
import com.vluver.beta.adapter.MultiAddImages;
import com.vluver.beta.model.AddMoreImages;
import com.vluver.beta.servicebackground.UploadImagesPost;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gun0912.tedimagepicker.builder.TedImagePicker;
import gun0912.tedimagepicker.builder.listener.OnErrorListener;
import gun0912.tedimagepicker.builder.listener.OnMultiSelectedListener;
import gun0912.tedimagepicker.builder.type.MediaType;

import static com.vluver.beta.Direccion.urlgeneral;


public class PostActivity extends AppCompatActivity {
    ProgressDialog progressDialog;
    ImageView new_post_pictures;
    ImageView regresar,enviar,seleccionar_imagen;
    private ImageView user_image;
    EditText descripcion_post;
    boolean imageIsSet = false;
    private String userId;
    RequestQueue mQueue;

    //firebase
    private FirebaseAuth mAuth;
    public FirebaseUser currentUser;


    int PICK_IMAGE_MULTIPLE = 1;
    public RecyclerView mRVFish;
    public MultiAddImages mAdapter;
    List<AddMoreImages> dataa;
    GridLayoutManager mGridLayoutManager;

    ArrayList<Uri> encodedImageList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        mAuth = FirebaseAuth.getInstance();
        mQueue = Volley.newRequestQueue(PostActivity.this);
        progressDialog = new ProgressDialog(this);
        new_post_pictures = (ImageView) findViewById(R.id.new_post_picture);
        regresar=(ImageView)findViewById(R.id.back);
        enviar=(ImageView) findViewById(R.id.send);
        descripcion_post =(EditText) findViewById(R.id.descripcion);
        user_image = (ImageView) findViewById(R.id.img_avatar);
        seleccionar_imagen = (ImageView) findViewById(R.id.new_post_submit);
        regresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mGridLayoutManager = new GridLayoutManager(PostActivity.this, 3);
        dataa=new ArrayList<>();
        encodedImageList = new ArrayList<>();
        mRVFish = (RecyclerView) findViewById(R.id.multimages);
        mAdapter = new MultiAddImages(PostActivity.this, dataa);
        mRVFish.setAdapter(mAdapter);
        mRVFish.setLayoutManager(mGridLayoutManager);
        seleccionar_imagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TedImagePicker.with(PostActivity.this)
                        .max(99,"Ups! max 99 img")
                        .mediaType(MediaType.IMAGE).errorListener(new OnErrorListener() {
                    @Override
                    public void onError(@NotNull String s) {
                        imageIsSet = false;
                        Toast.makeText(PostActivity.this, "Error: "+s, Toast.LENGTH_SHORT).show();
                    }
                }).startMultiImage((OnMultiSelectedListener) uriList -> {
                            imageIsSet = true;
                            hideKeyboard(PostActivity.this);
                            for(Uri photos : uriList){
                                AddMoreImages addMoreImages = new AddMoreImages();
                                addMoreImages.mArrayUri = photos;
                                dataa.add(addMoreImages);
                                encodedImageList.add(photos);
                            }
                            mAdapter.notifyDataSetChanged();
                            mRVFish.setAdapter(mAdapter);

                        });
               /* TedImagePicker.with(PostActivity.this).max(99,"Max 99")
                        .start(new OnMultiSelectedListener() {
                            @Override
                            public void onSelected(@NotNull List<? extends Uri> uriList) {
                                imageIsSet = true;
                                for(Uri photos : uriList){
                                    AddMoreImages addMoreImages = new AddMoreImages();
                                    addMoreImages.mArrayUri = photos;
                                    dataa.add(addMoreImages);
                                    encodedImageList.add(String.valueOf(uriList));
                                }
                                mAdapter.notifyDataSetChanged();
                                mRVFish.setAdapter(mAdapter);
                                // multi
                            }
                        });*/
            }
        });

        enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageIsSet){



                    Intent mServiceIntent = new Intent(PostActivity.this, UploadImagesPost.class);
                    mServiceIntent.putExtra("userId", userId);
                    mServiceIntent.putParcelableArrayListExtra("mPhotos", encodedImageList);
                    mServiceIntent.putExtra("descripcion",descripcion_post.getText().toString().trim());
                    startService(mServiceIntent);
                    finish();
                }else{
                    if (descripcion_post.getText().toString().isEmpty()){
                        descripcion_post.setError("Agrega algo a tus publicacion...");
                    }else{
                        insertPostToVluver(userId,"null",descripcion_post.getText().toString());
                    }
                }


            }
        });
        getAvatar();

    }


    private void getAvatar(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            for (UserInfo profile : user.getProviderData()) {
                if (profile.getProviderId().equals("facebook.com")){
                    String image = "https://graph.facebook.com/" + user.getProviderData().get(1).getUid() + "/picture?type=small";
                   // GlideLoadImages.loadAvatar(PostActivity.this,image,user_image);
                }else if (profile.getProviderId().equals("google.com")){
                    Toast.makeText(PostActivity.this, "avatar Google", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    private void insertPostToVluver(String creator, String codeuniqueimages, String description){
        String url = urlgeneral+"insert/insertPost.php";
        StringRequest strReq = new StringRequest(Request.Method.POST,
                url, new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("error");
                    if (!error){
                        // progressDialog.dismiss();
                        Toast.makeText(PostActivity.this, "Publicacion subida correctamente", Toast.LENGTH_LONG).show();
                        //show_notification("Vluver", "Publicacion lista", 2 );
                        finish();
                    }else{
                        //progressDialog.dismiss();
                        String errorMsg = jsonObject.getString("error_msg");
                        Toast.makeText(PostActivity.this, ""+errorMsg, Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    // progressDialog.dismiss();
                    Toast.makeText(PostActivity.this, "Algo anda mal"+e, Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("creator", creator);
                params.put("namecode",codeuniqueimages);
                params.put("description",description);
                return params;
            }


        };
        strReq.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mQueue.add(strReq);
    }


    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        assert imm != null;
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    @Override
    protected void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
        userId = currentUser.getUid();
    }

    @Override
    protected void onDestroy() {
        mQueue.stop();
        mQueue = null;
        super.onDestroy();
    }
}