package com.vluver.beta.activities;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.tangxiaolv.telegramgallery.GalleryActivity;
import com.tangxiaolv.telegramgallery.GalleryConfig;
import com.vluver.beta.R;
import com.vluver.beta.adapter.MultiAddImages;
import com.vluver.beta.model.AddMoreImages;
import com.vluver.beta.servicebackground.UploadImagesPost;
import com.vluver.beta.utils.GlideLoadImages;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PostActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_READ_PERMISSION = 22;
    ProgressDialog progressDialog;
    ImageView new_post_pictures;
    ImageView regresar,enviar,seleccionar_imagen;
    private ImageView user_image;
    EditText descripcion_post;
    boolean imageIsSet = false;
    Bitmap bitmappost;
    private String userId;
    RequestQueue mQueue;


    //firebase
    private FirebaseAuth mAuth;
    public FirebaseUser currentUser;
    private String channelId = "vluver_id";


    int PICK_IMAGE_MULTIPLE = 1;
    List<String> imagesEncodedList;
    public RecyclerView mRVFish;
    public MultiAddImages mAdapter;
    List<AddMoreImages> dataa;
    GridLayoutManager mGridLayoutManager;

    private ArrayList<String> mPhotos;
    ArrayList<String> encodedImageList;



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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
            }
        }
        seleccionar_imagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GalleryConfig config = new GalleryConfig.Build()
                        .limitPickPhoto(99)
                        .singlePhoto(false)
                        .hintOfPick("max 99")
                        .filterMimeTypes(new String[]{"image/*"})
                        .build();
                GalleryActivity.openActivity(PostActivity.this, 2, config);
            }
        });

        enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageIsSet){
                    Intent mServiceIntent = new Intent(PostActivity.this, UploadImagesPost.class);
                    mServiceIntent.putExtra("userId", userId);
                    mServiceIntent.putStringArrayListExtra("mPhotos", encodedImageList);
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
                    GlideLoadImages.loadAvatar(PostActivity.this,image,user_image);
                }else if (profile.getProviderId().equals("google.com")){
                    Toast.makeText(PostActivity.this, "avatar Google", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }
    private void sendPost(String description ){
        progressDialog.setMessage("\tPublicando...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        //insertPostToVluver(userId,"",description);
    }

    private void insertPostToVluver(String creator, String codeuniqueimages, String description){
        String url = "https://vluver.com/mobile/insert/insertPost.php";
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        try{
            if (data != null){
                mPhotos = (ArrayList<String>) data.getSerializableExtra(GalleryActivity.PHOTOS);
                imageIsSet = true;
                for (String photo : mPhotos) {
                    Uri uri = Uri.fromFile(new File(photo));
                    AddMoreImages addMoreImages = new AddMoreImages();
                    addMoreImages.mArrayUri= uri;
                    dataa.add(addMoreImages);
                    encodedImageList.add(photo);
                }
                mAdapter.notifyDataSetChanged();
                mRVFish.setAdapter(mAdapter);
            }else{
                imageIsSet = false;
                Toast.makeText(this, "Ninguna foto seleccionada!", Toast.LENGTH_SHORT).show();
            }
        }catch (Exception e){
            imageIsSet = false;
            Toast.makeText(this, "Error de seleccion: "+e, Toast.LENGTH_SHORT).show();
        }


    }


    private boolean hasGalleryPermission() {
        return ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void askForGalleryPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                REQUEST_CODE_READ_PERMISSION);
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
        bitmappost = null;
        mQueue.stop();
        mQueue = null;
        super.onDestroy();
    }
}
