package com.vluver.beta


import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatButton
import android.text.TextUtils
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.Toast
import android.widget.ViewSwitcher

import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest

import org.json.JSONException
import org.json.JSONObject

import java.util.HashMap

class RegisterUser : AppCompatActivity() {
    internal lateinit var scrollView: ScrollView
    private var nombres: EditText? = null
    private var apellidos: EditText? = null
    private var correo: EditText? = null
    private var clave: EditText? = null
    private var reclave: EditText? = null
    private var hombre: RadioButton? = null
    private var mujer: RadioButton? = null
    private var acepto: CheckBox? = null
    private var registrar: AppCompatButton? = null
    private var deacuerdo: AppCompatButton? = null
    private var switcher: ViewSwitcher? = null
    private var switchermain: ViewSwitcher? = null
    internal lateinit var mQueue: RequestQueue
    private var mAuth: FirebaseAuth? = null
    private var rg: RadioGroup? = null


    internal var genre: String? = null
    private var isOlder = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)
        mQueue = Volley.newRequestQueue(this@RegisterUser)
        mAuth = FirebaseAuth.getInstance()
        rg = findViewById<View>(R.id.register_rg) as RadioGroup
        scrollView = findViewById<View>(R.id.scroll) as ScrollView
        nombres = findViewById<View>(R.id.names) as EditText
        apellidos = findViewById<View>(R.id.lastnames) as EditText
        hombre = findViewById<View>(R.id.radio_masculino) as RadioButton
        mujer = findViewById<View>(R.id.radio_femenino) as RadioButton
        acepto = findViewById<View>(R.id.register_accept) as CheckBox
        switcher = findViewById<View>(R.id.switcher) as ViewSwitcher
        switchermain = findViewById<View>(R.id.switchermain) as ViewSwitcher
        correo = findViewById<View>(R.id.email) as EditText
        clave = findViewById<View>(R.id.password) as EditText
        reclave = findViewById<View>(R.id.repeatpassword) as EditText
        registrar = findViewById<View>(R.id.register) as AppCompatButton
        deacuerdo = findViewById<View>(R.id.close) as AppCompatButton
        registrar!!.setOnClickListener {
            if (registrar!!.text == "Siguiente") {
                verificarvista1()

            } else {
                verificarvista2()
            }
        }
        deacuerdo!!.setOnClickListener {
            val intent = Intent(this@RegisterUser,
                    LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }



        acepto!!.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isChecked) {
                // checked
                isOlder = true
            } else {
                // not checked
                isOlder = false
            }
        }

    }

    private fun vibrate_error(time: Int) {
        if (Build.VERSION.SDK_INT >= 26) {
            (getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(VibrationEffect.createOneShot(time.toLong(), VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            (getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(time.toLong())
        }
    }

    private fun verificarvista1() {
        val name = nombres!!.text.toString()
        val lastname = apellidos!!.text.toString().trim { it <= ' ' }
        if (name.isEmpty()) {
            nombres!!.error = "Ingresa tu nombre"
            vibrate_error(100)
        } else if (lastname.isEmpty()) {
            apellidos!!.error = "Ingresa tu apellido"
            vibrate_error(100)
        } else {
            switcher!!.showNext()
            registrar!!.text = "Registrar"
        }
    }

    private fun verificarvista2() {

        var cancel = false

        val name = nombres!!.text.toString()
        val lastname = apellidos!!.text.toString().trim { it <= ' ' }
        if (hombre!!.isChecked) {
            genre = "0"
        }
        if (mujer!!.isChecked) {
            genre = "1"
        }
        val email = correo!!.text.toString().trim { it <= ' ' }
        val password = clave!!.text.toString().trim { it <= ' ' }
        val repassword = reclave!!.text.toString().trim { it <= ' ' }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            clave!!.error = "Field required"
            cancel = true

        }
        if (!isPasswordValid(password)) {
            clave!!.error = "Invalid, to short"
            cancel = true


        }
        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(repassword)) {
            reclave!!.error = "Field required"
            cancel = true


        }
        if (!isPasswordValid(repassword)) {
            reclave!!.error = "Invalid, to short"
            cancel = true


        }


        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            correo!!.error = "Field required"
            cancel = true


        } else if (!isEmailValid(email)) {
            correo!!.error = "Not valid email"
            cancel = true


        }
        if (!cancel) {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            if (clave!!.text.toString().trim { it <= ' ' } == reclave!!.text.toString().trim { it <= ' ' }) {
                //do things if these 2 are correct.

                if (isOlder) {
                    acepto!!.error = null

                    register_user(correo!!.text.toString().trim { it <= ' ' }, clave!!.text.toString().trim { it <= ' ' })

                } else {
                    acepto!!.error = "You have to be older than 12"
                }


                //mAuthTask = new UserLoginTask(email, password);
                //mAuthTask.execute((Void) null);
            } else {
                reclave!!.error = "Password mismatch"

            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
    }

    private fun isEmailValid(email: String): Boolean {
        //TODO: Replace this with your own logic
        return email.contains("@")
    }

    private fun isPasswordValid(password: String): Boolean {
        //TODO: Replace this with your own logic
        return password.length > 4
    }


    private fun register_user(mEmail: String, mPassword: String) {
        mAuth!!.createUserWithEmailAndPassword(mEmail, mPassword)
                .addOnCompleteListener(this@RegisterUser) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        //Log.d(TAG, "createUserWithEmail:success");
                        val user = mAuth!!.currentUser

                        if (user != null) {

                            val profileUpdates = UserProfileChangeRequest.Builder()
                                    .setDisplayName(nombres!!.text.toString() + " " + apellidos!!.text.toString())
                                    //.setPhotoUri(Uri.parse("https://example.com/jane-q-user/profile.jpg"))
                                    .build()
                            user.sendEmailVerification()
                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val checkedRadioButtonId = rg!!.checkedRadioButtonId
                                            if (checkedRadioButtonId != -1) {
                                                if (checkedRadioButtonId == R.id.radio_masculino) {
                                                    registerUserToDB(user.uid, user.email,
                                                            nombres!!.text.toString() + " " + apellidos!!.text.toString(), "", "1")
                                                } else {
                                                    registerUserToDB(user.uid, user.email,
                                                            nombres!!.text.toString() + " " + apellidos!!.text.toString(), "", "0")
                                                }
                                            }
                                        }
                                    }
                        }


                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(this@RegisterUser, "Registration failed",
                                Toast.LENGTH_SHORT).show()

                        // FirebaseAuth.getInstance().signOut();
                    }
                }
    }

    private fun registerUserToDB(userId: String, email: String?, fullnames: String, avatar: String, gender: String) {
        val url = "https://www.vluver.com/mobile/registerUserToDB.php"
        val strReq = object : StringRequest(Request.Method.POST,
                url, Response.Listener { response ->
            try {
                val jsonObject = JSONObject(response)
                val error = jsonObject.getBoolean("error")
                if (!error) {
                    updateUI()
                } else {
                    val errorMsg = jsonObject.getString("error_msg")
                    if (errorMsg == "Este usuario ya existe") {
                        updateUI()
                    } else {
                        Toast.makeText(this@RegisterUser, "Algo anda mal", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: JSONException) {
                Toast.makeText(this@RegisterUser, "Algo anda mal$e", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }, Response.ErrorListener { error ->
            Toast.makeText(applicationContext,
                    error.message, Toast.LENGTH_LONG).show()
        }) {

            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["UID"] = userId
                params["email"] = email!!
                params["fullnames"] = fullnames
                params["avatar"] = avatar
                params["gender"] = gender
                params["phone"] = ""
                return params
            }

        }
        strReq.retryPolicy = DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        mQueue.add(strReq)
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

    private fun updateUI() {
        val intent = Intent(this@RegisterUser, MainActivity::class.java)
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent)
        finish()

    }

}
