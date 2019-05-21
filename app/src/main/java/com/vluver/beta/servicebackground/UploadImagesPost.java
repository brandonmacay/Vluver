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
                    Uri file = Uri.fromFile(new File(finalMPhotos.get(i)));
                    Bitmap bitmap = null;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), file);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        changeOrientation(bitmap,file.getPath()).compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                        params.put("fileToUpload[" + i + "]", new DataPart(imagename + i + ".jpg",  byteArrayOutputStream.toByteArray(), "image/*"));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                return params;
            }
        };
        multipartRequest.setRetryPolicy(new DefaultRetryPolicy(10000, 1, 1.0f));
        MySingleton.getInstance(UploadImagesPost.this).addToRequestQueue(multipartRequest);



    }
    public static Bitmap changeOrientation(Bitmap bitmap, String imagePath) throws IOException {
        ExifInterface ei = new ExifInterface(imagePath);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotate(bitmap, 90);

            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotate(bitmap, 180);

            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotate(bitmap, 270);

            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                return flip(bitmap, true, false);

            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                return flip(bitmap, false, true);

            default:
                return bitmap;
        }
    }
    public static Bitmap rotate(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }
    public static Bitmap flip(Bitmap bitmap, boolean horizontal, boolean vertical) {
        Matrix matrix = new Matrix();
        matrix.preScale(horizontal ? -1 : 1, vertical ? -1 : 1);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

}
