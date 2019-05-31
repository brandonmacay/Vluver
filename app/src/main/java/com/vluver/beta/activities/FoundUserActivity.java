package com.vluver.beta.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.vluver.beta.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FoundUserActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView avatar;
    TextView names;
    AppCompatButton btnFollower,btnChat;
    RequestQueue mQueue;
    Intent data;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_found_user);
        data = this.getIntent();
        avatar =(ImageView) findViewById(R.id.avatar);
        names = (TextView) findViewById(R.id.name_user);
        btnFollower = (AppCompatButton) findViewById(R.id.follow);
        btnChat = (AppCompatButton) findViewById(R.id.chat);
        btnFollower.setOnClickListener(this);
        mQueue = Volley.newRequestQueue(FoundUserActivity.this);

        setUserData();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.follow:{
                if (!btnFollower.getText().toString().equals("Siguiendo")){
                    btnFollower.setEnabled(false);
                    sendRequestFollow(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(),data.getStringExtra("userUID"),0, Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName());
                }
                break;
            }
        }
    }

    private void setUserData(){

        String urlavatar = null;
        String namesUser = null;
        if (data != null) {
            urlavatar = data.getStringExtra("userAvatar");
            namesUser = data.getStringExtra("userName");
        }
        RequestOptions optionb = new RequestOptions()
                //.centerCrop()
                .fitCenter()
                .placeholder(android.R.color.darker_gray)
                .error(R.drawable.noneimg)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE);
        Glide.with(FoundUserActivity.this)
                .load(urlavatar)
                .apply(optionb)
                .thumbnail(0.5f)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(avatar);
        names.setText(namesUser);
    }

    private void sendRequestFollow(String user_sender, String user_receiver, int accepted,String nameuser){
        String url = "https://vluver.com/mobile/sendaction/followuser.php";
        StringRequest strReq = new StringRequest(Request.Method.POST,
                url, new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("error");
                    if (!error){
                        btnFollower.setText("Siguiendo");
                        btnFollower.setEnabled(true);
                    }else{
                        btnFollower.setText("Siguiendo");
                        btnFollower.setEnabled(true);
                        String errorMsg = jsonObject.getString("error_msg");
                        Toast.makeText(FoundUserActivity.this, ""+errorMsg, Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    Toast.makeText(FoundUserActivity.this, "Algo anda mal"+e, Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("user_sender", user_sender);
                params.put("user_receiver",user_receiver);
                params.put("accepted", String.valueOf(accepted));
                params.put("username",nameuser);
                return params;
            }

        };
        strReq.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mQueue.add(strReq);
    }




}
