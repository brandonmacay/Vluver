package com.vluver.beta;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterUser extends AppCompatActivity {
    ScrollView scrollView;
    private EditText nombres,apellidos,correo,clave, reclave;
    private RadioButton hombre,mujer;
    private CheckBox acepto;
    private AppCompatButton registrar,deacuerdo;
    private ViewSwitcher switcher,switchermain;
    RequestQueue mQueue;
    private FirebaseAuth mAuth;
    private RadioGroup rg;


    String genre = null;
    private boolean isOlder = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        mQueue = Volley.newRequestQueue(RegisterUser.this);
        mAuth = FirebaseAuth.getInstance();
        rg = (RadioGroup) findViewById(R.id.register_rg);
        scrollView = (ScrollView) findViewById(R.id.scroll);
        nombres = (EditText) findViewById(R.id.names);
        apellidos = (EditText) findViewById(R.id.lastnames);
        hombre = (RadioButton) findViewById(R.id.radio_masculino);
        mujer = (RadioButton) findViewById(R.id.radio_femenino);
        acepto = (CheckBox) findViewById(R.id.register_accept);
        switcher = (ViewSwitcher) findViewById(R.id.switcher);
        switchermain = (ViewSwitcher) findViewById(R.id.switchermain);
        correo = (EditText) findViewById(R.id.email);
        clave = (EditText) findViewById(R.id.password);
        reclave =  (EditText) findViewById(R.id.repeatpassword);
        registrar = (AppCompatButton) findViewById(R.id.register);
        deacuerdo = (AppCompatButton) findViewById(R.id.close);
        registrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (registrar.getText().equals("Siguiente")){
                    verificarvista1();

                }else {
                    verificarvista2();
                }
            }
        });
        deacuerdo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterUser.this,
                        LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT| Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });



        acepto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    // checked
                    isOlder = true;
                }
                else
                {
                    // not checked
                    isOlder = false;
                }
            }

        });

    }
    private void vibrate_error(final int time) {
        if (Build.VERSION.SDK_INT >= 26) {
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(time, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(time);
        }
    }
    private void verificarvista1(){
        String name = nombres.getText().toString();
        String lastname = apellidos.getText().toString().trim();
        if (name.isEmpty()) {
            nombres.setError("Ingresa tu nombre");
            vibrate_error(100);
        } else if (lastname.isEmpty()){
            apellidos.setError("Ingresa tu apellido");
            vibrate_error(100);
        }
        else {
            switcher.showNext();
            registrar.setText("Registrar");
        }
    }
    private void verificarvista2(){

        boolean cancel = false;

        String name = nombres.getText().toString();
        String lastname = apellidos.getText().toString().trim();
        if (hombre.isChecked()){
            genre = "0";
        }
        if (mujer.isChecked()){
            genre = "1";
        }
        String email = correo.getText().toString().trim();
        String password = clave.getText().toString().trim();
        String repassword = reclave.getText().toString().trim();

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password) ) {
            clave.setError("Field required");
            cancel = true;

        }
        if (!isPasswordValid(password) ) {
            clave.setError("Invalid, to short");
            cancel = true;


        }
        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(repassword) ) {
            reclave.setError("Field required");
            cancel = true;


        }
        if (!isPasswordValid(repassword) ) {
            reclave.setError("Invalid, to short");
            cancel = true;


        }



        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            correo.setError("Field required");
            cancel = true;


        } else if (!isEmailValid(email)) {
            correo.setError("Not valid email");
            cancel = true;


        }
        if (!cancel) {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            if (clave.getText().toString().trim().equals(reclave.getText().toString().trim())) {
//do things if these 2 are correct.

                if (isOlder) {
                    acepto.setError(null);

                    register_user(correo.getText().toString().trim(), clave.getText().toString().trim());

                }
                else {
                    acepto.setError("You have to be older than 12");
                }



                //mAuthTask = new UserLoginTask(email, password);
                //mAuthTask.execute((Void) null);
            } else {
                reclave.setError("Password mismatch");

            }
        }
    }





    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }



    private void register_user (String mEmail, String mPassword) {
        mAuth.createUserWithEmailAndPassword(mEmail, mPassword)
                .addOnCompleteListener(RegisterUser.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            if (user != null) {

                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(nombres.getText().toString() + " " + apellidos.getText().toString())
                                        //.setPhotoUri(Uri.parse("https://example.com/jane-q-user/profile.jpg"))
                                        .build();
                                user.sendEmailVerification();
                                user.updateProfile(profileUpdates)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    int checkedRadioButtonId = rg.getCheckedRadioButtonId();
                                                    if (checkedRadioButtonId != -1) {
                                                        if (checkedRadioButtonId == R.id.radio_masculino) {
                                                            registerUserToDB(user.getUid(),user.getEmail(),
                                                                    nombres.getText().toString() + " " + apellidos.getText().toString(),"","1");
                                                        }
                                                        else {
                                                            registerUserToDB(user.getUid(),user.getEmail(),
                                                                    nombres.getText().toString() + " " + apellidos.getText().toString(),"","0");
                                                        }
                                                    }
                                                }
                                            }
                                        });
                            }


                        }
                        else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(RegisterUser.this, "Registration failed",
                                    Toast.LENGTH_SHORT).show();

                            // FirebaseAuth.getInstance().signOut();
                        }


                    }
                });
    }
    private void registerUserToDB(String userId, String email, String fullnames, String avatar, String gender){
        String url = "https://mrsearch.000webhostapp.com/vluver/mobile/registerUserToDB.php";
        StringRequest strReq = new StringRequest(Request.Method.POST,
                url, new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("error");
                    if (!error){
                        updateUI();
                    }else{
                        String errorMsg = jsonObject.getString("error_msg");
                        if (errorMsg.equals("Este usuario ya existe")){
                            updateUI();
                        }else {
                            Toast.makeText(RegisterUser.this, "Algo anda mal", Toast.LENGTH_SHORT).show();
                        }
                    }

                } catch (JSONException e) {
                    Toast.makeText(RegisterUser.this, "Algo anda mal"+e, Toast.LENGTH_SHORT).show();
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
                params.put("UID", userId);
                params.put("email",email);
                params.put("fullnames",fullnames);
                params.put("avatar", avatar);
                params.put("gender",gender);
                params.put("phone","");
                return params;
            }

        };
        strReq.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mQueue.add(strReq);
    }

    /*private void registerUserToDB(String userId,String email, String fullnames,String avatar, String gender){
        Map<String, Object> post = new HashMap<>();
        post.put("UID", userId);
        post.put("email",email);
        post.put("fullnames",fullnames);
        post.put("fullnameslower", fullnames.toLowerCase());
        post.put("avatar", avatar);
        post.put("gender",gender);
        post.put("phone","");
        db.collection("user").document(userId)
                .set(post)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        updateUI();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterUser.this, "Error LoginActivity line:201", Toast.LENGTH_SHORT).show();
                progreso.dismiss();
            }
        });
    }*/

    private void updateUI(){
        Intent intent = new Intent(RegisterUser.this, MainActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();

    }

}