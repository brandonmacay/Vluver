package com.vluver.beta.VisionIA;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
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
import com.vluver.beta.servicebackground.UploadImagesPost;
import com.vluver.beta.utils.PackageManagerUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VerifyImageWithIA {
    private static final String CLOUD_VISION_API_KEY = "AIzaSyCWL_8dR5XDWaIwDP2QIjwWIFkAIq9Vahs";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
    private static final int MAX_DIMENSION = 1200;

    public void SearchSafeWithUri(Context context,Uri uri) {
        if (uri != null) {
            try {
                // scale the image to save on bandwidth
                Bitmap bitmap =
                        scaleBitmapDown(
                                MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri),
                                MAX_DIMENSION);

                callCloudVision(bitmap,context);

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
    private void callCloudVision(final Bitmap bitmap,Context context) {
        // Switch text to loading
        //revisando imagen
        // Do the real work in an async task, because we need to use the network anyway
        try {
            LableDetectionTask labelDetectionTask = new LableDetectionTask((UploadImagesPost) context, prepareAnnotationRequest(bitmap,context));
            labelDetectionTask.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static class LableDetectionTask extends AsyncTask<Object, Void, String> {
        private Vision.Images.Annotate mRequest;

        LableDetectionTask(UploadImagesPost activity, Vision.Images.Annotate annotate) {
            mRequest = annotate;
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
        StringBuilder message = new StringBuilder("Contenido encontrado:\n\n");
        List<AnnotateImageResponse> responses = response.getResponses();
        for (AnnotateImageResponse res : responses) {

            // For full list of available annotations, see http://g.co/cloud/vision/docs
            SafeSearchAnnotation annotation1 = res.getSafeSearchAnnotation();
            message.append(String.format(Locale.US,  "Adulto: %s\nMédico: %s\nFalsificado: %s\nViolencia: %s\nPicante: %s\n",
                    annotation1.getAdult().replace("VERY_LIKELY","Muy Probable").replace("VERY_UNLIKELY","Muy improbable").replace("UNLIKELY","Improbable").replace("LIKELY","Probable").replace("POSSIBLE","Posible"),
                    annotation1.getMedical().replace("VERY_LIKELY","Muy Probable").replace("VERY_UNLIKELY","Muy improbable").replace("UNLIKELY","Improbable").replace("LIKELY","Probable").replace("POSSIBLE","Posible"),
                    annotation1.getSpoof().replace("VERY_LIKELY","Muy Probable").replace("VERY_UNLIKELY","Muy improbable").replace("UNLIKELY","Improbable").replace("LIKELY","Probable").replace("POSSIBLE","Posible"),
                    annotation1.getMedical().replace("VERY_LIKELY","Muy Probable").replace("VERY_UNLIKELY","Muy improbable").replace("UNLIKELY","Improbable").replace("LIKELY","Probable").replace("POSSIBLE","Posible"),
                    annotation1.getRacy().replace("VERY_LIKELY","Muy Probable").replace("VERY_UNLIKELY","Muy improbable").replace("UNLIKELY","Improbable").replace("LIKELY","Probable").replace("POSSIBLE","Posible")));
            message.append("\n");

        }

        return message.toString();
    }
}
