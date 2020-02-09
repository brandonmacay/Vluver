package com.vluver.beta.servicebackground;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.SafeSearchAnnotation;
import com.vluver.beta.R;
import com.vluver.beta.VisionIA.VerifyImageWithIA;
import com.vluver.beta.serviceVolley.VolleyMultipartRequest;
import com.vluver.beta.serviceVolley.VolleySingleton;
import com.vluver.beta.utils.ConvertBitmapToByte;
import com.vluver.beta.utils.PackageManagerUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static com.vluver.beta.Direccion.urlgeneral;

public class UploadImagesPost extends IntentService {
    RequestQueue mQueue;
    String userId;
    private static final String CLOUD_VISION_API_KEY = "AIzaSyCWL_8dR5XDWaIwDP2QIjwWIFkAIq9Vahs";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
    private static final int MAX_DIMENSION = 1200;
    private static boolean isAdultPost = false;
    private static VolleyMultipartRequest multipartRequest;
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
        ArrayList<Uri> mPhotos = new ArrayList<>();
        if (intent != null) {
            mPhotos = intent.getParcelableArrayListExtra("mPhotos");
        }
        assert intent != null;
        String descripcion = intent.getStringExtra("descripcion");
        String randomuuid = UUID.randomUUID().toString();
        long imagename = System.currentTimeMillis();
        ArrayList<Uri> finalMPhotos = mPhotos;
        for (int i = 0; i < finalMPhotos.size(); i++) {
            SearchSafeWithUri(UploadImagesPost.this,finalMPhotos.get(i),finalMPhotos.size(),i+1);
        }
         multipartRequest = new VolleyMultipartRequest(Request.Method.POST, urlgeneral+"insert/api_post.php?apicall=uploadpic", new Response.Listener<NetworkResponse>() {
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
                    params.put("fileToUpload[" + i + "]",
                            new DataPart(imagename + i + ".jpg",
                                    ConvertBitmapToByte
                                            .getfilebytefromuri(UploadImagesPost.this,finalMPhotos.get(i)),
                                    "image/*"));

                }
                return params;
            }
        };


    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        UploadImagesPost.isAdultPost = false;
        stopSelf();
    }

    public void SearchSafeWithUri(Context context, Uri uri, int urisize, int count) {
        if (uri != null) {
            try {
                // scale the image to save on bandwidth
                Bitmap bitmap =
                        scaleBitmapDown(
                                MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri),
                                MAX_DIMENSION);

                callCloudVision(bitmap,urisize,count);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }
    private void callCloudVision(final Bitmap bitmap,int uritotal,int count) {
        // Switch text to loading
        //revisando imagen
        // Do the real work in an async task, because we need to use the network anyway
        try {
            LableDetectionTask labelDetectionTask = new LableDetectionTask(uritotal,count, prepareAnnotationRequest(bitmap,this),this);
            labelDetectionTask.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private class LableDetectionTask extends AsyncTask<Object, Void, String> {
        private Vision.Images.Annotate mRequest;
        int totalimg;
        @SuppressLint("StaticFieldLeak")
        private Context contexto;
        int countImg;
        LableDetectionTask(int totalimg1,int contador, Vision.Images.Annotate annotate,Context context) {
            mRequest = annotate;
            totalimg = totalimg1;
            contexto = context;
            countImg = contador;
        }

        @Override
        protected String doInBackground(Object... params) {

            try {
                BatchAnnotateImagesResponse response = mRequest.execute();
                return convertResponseToString(response);

            } catch (GoogleJsonResponseException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "Cloud Vision API request failed. Check logs for details.";
        }

        protected void onPostExecute(String result) {
            //"Aqui muestra los result"
            if (!isAdultPost && result.contains("deny")){
                UploadImagesPost.isAdultPost = true;
                Toast.makeText(contexto, "Tu post fue denegado por contenido adulto!", Toast.LENGTH_SHORT).show();
            }

            Toast.makeText(contexto, "IA trabajando..."+"\n"+countImg+" de "+totalimg+"\ncontenidoAdulto= "+isAdultPost, Toast.LENGTH_SHORT).show();

            if (countImg == totalimg && !isAdultPost){
                VolleySingleton.getInstance(contexto).addToRequestQueue(multipartRequest);

            }

        }


    }
    private Vision.Images.Annotate prepareAnnotationRequest(Bitmap bitmap,Context context) throws IOException {
        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        VisionRequestInitializer requestInitializer =
                new VisionRequestInitializer(CLOUD_VISION_API_KEY) {
                    /**
                     * We override this so we can inject important identifying fields into the HTTP
                     * headers. This enables use of a restricted cloud platform API key.
                     */
                    @Override
                    protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                            throws IOException {
                        super.initializeVisionRequest(visionRequest);

                        String packageName = context.getPackageName();
                        visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);

                        String sig = PackageManagerUtils.getSignature(context.getPackageManager(), packageName);

                        visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
                    }
                };

        Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
        builder.setVisionRequestInitializer(requestInitializer);

        Vision vision = builder.build();

        BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                new BatchAnnotateImagesRequest();
        batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
            AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

            // Add the image
            Image base64EncodedImage = new Image();
            // Convert the bitmap to a JPEG
            // Just in case it's a format that Android understands but Cloud Vision
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            // Base64 encode the JPEG
            base64EncodedImage.encodeContent(imageBytes);
            annotateImageRequest.setImage(base64EncodedImage);

            // add the features we want
            annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                Feature labelDetection = new Feature();
                labelDetection.setType("SAFE_SEARCH_DETECTION");
                // labelDetection.setMaxResults(MAX_LABEL_RESULTS);
                add(labelDetection);
            }});

            // Add the list of one thing to the request
            add(annotateImageRequest);
        }});

        Vision.Images.Annotate annotateRequest =
                vision.images().annotate(batchAnnotateImagesRequest);
        // Due to a bug: requests to Vision API containing large images fail when GZipped.
        annotateRequest.setDisableGZipContent(true);
        return annotateRequest;
    }
    private static String convertResponseToString(BatchAnnotateImagesResponse response) {
        StringBuilder message = new StringBuilder("");
        List<AnnotateImageResponse> responses = response.getResponses();
        for (AnnotateImageResponse res : responses) {

            // For full list of available annotations, see http://g.co/cloud/vision/docs
            SafeSearchAnnotation annotation1 = res.getSafeSearchAnnotation();
            if (annotation1.getAdult().equals("VERY_LIKELY")&& annotation1.getRacy().equals("VERY_LIKELY")||
                    annotation1.getAdult().equals("LIKELY")){
                message.append("deny");
            }else {
                message.append("allow");
            }
            /*message.append(String.format(Locale.US,  "Adulto: %s\nMédico: %s\nFalsificado: %s\nViolencia: %s\nPicante: %s\n",
                    annotation1.getAdult().replace("VERY_LIKELY","Muy Probable").replace("VERY_UNLIKELY","Muy improbable").replace("UNLIKELY","Improbable").replace("LIKELY","Probable").replace("POSSIBLE","Posible"),
                    annotation1.getMedical().replace("VERY_LIKELY","Muy Probable").replace("VERY_UNLIKELY","Muy improbable").replace("UNLIKELY","Improbable").replace("LIKELY","Probable").replace("POSSIBLE","Posible"),
                    annotation1.getSpoof().replace("VERY_LIKELY","Muy Probable").replace("VERY_UNLIKELY","Muy improbable").replace("UNLIKELY","Improbable").replace("LIKELY","Probable").replace("POSSIBLE","Posible"),
                    annotation1.getMedical().replace("VERY_LIKELY","Muy Probable").replace("VERY_UNLIKELY","Muy improbable").replace("UNLIKELY","Improbable").replace("LIKELY","Probable").replace("POSSIBLE","Posible"),
                    annotation1.getRacy().replace("VERY_LIKELY","Muy Probable").replace("VERY_UNLIKELY","Muy improbable").replace("UNLIKELY","Improbable").replace("LIKELY","Probable").replace("POSSIBLE","Posible")));
            message.append("\n");*/

        }

        return message.toString();
    }

}
