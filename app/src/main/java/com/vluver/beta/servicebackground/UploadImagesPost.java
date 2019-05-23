package com.vluver.beta.servicebackground;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.vluver.beta.serviceVolley.MySingleton;
import com.vluver.beta.serviceVolley.VolleyMultipartRequest;
import com.vluver.beta.utils.ConvertBitmapToByte;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UploadImagesPost extends IntentService {
    RequestQueue mQueue;
    String userId;

    public UploadImagesPost() {
        super("UploadImagesPost");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        mQueue = Volley.newRequestQueue(UploadImagesPost.this);

        userId = null;
        if (intent != null) {
            userId = intent.getStringExtra("userId");
        }
        ArrayList<String> mPhotos = new ArrayList<>();
        if (intent != null) {
            mPhotos = intent.getStringArrayListExtra("mPhotos");
        }
        assert intent != null;
        String descripcion = intent.getStringExtra("descripcion");
        String randomuuid = UUID.randomUUID().toString();
        long imagename = System.currentTimeMillis();
        ArrayList<String> finalMPhotos = mPhotos;
        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, "http://vluver.com/mobile/insert/api_post.php?apicall=uploadpic", new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                try {
                    JSONObject obj = new JSONObject(new String(response.data));
                    Toast.makeText(UploadImagesPost.this, obj.getString("message") , Toast.LENGTH_SHORT).show();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(UploadImagesPost.this, "VolleyError: "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("userid", userId);
                params.put("foldername",randomuuid);
                params.put("itemfolder", String.valueOf(imagename));
                params.put("description",descripcion);
                // params.put("tags", tags);
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() throws AuthFailureError {
                Map<String, DataPart> params = new HashMap<>();
                // file name could found file base or direct access from real path
                //for now just get bitmap data from ImageView
                for (int i = 0; i < finalMPhotos.size(); i++) {
                    params.put("fileToUpload[" + i + "]", new DataPart(imagename + i + ".jpg", ConvertBitmapToByte.getfilebytefromuri(UploadImagesPost.this,finalMPhotos.get(i)), "image/*"));

                }
                return params;
            }
        };
        MySingleton.getInstance(UploadImagesPost.this).addToRequestQueue(multipartRequest);

    }


}
