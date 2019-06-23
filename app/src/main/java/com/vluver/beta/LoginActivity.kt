package com.vluver.beta

import android.app.ProgressDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.facebook.AccessToken
import com.facebook.AccessTokenTracker
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.Profile
import com.facebook.ProfileTracker
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

import org.json.JSONException
import org.json.JSONObject

import java.util.Arrays
import java.util.HashMap

class LoginActivity : AppCompatActivity() {
    private var progreso: ProgressDialog? = null

    private var mEmailView: AutoCompleteTextView? = null
    private var mPasswordView: EditText? = null
    private var mAuth: FirebaseAuth? = null
    private var callbackManager: CallbackManager? = null
    private var accessTokenTracker: AccessTokenTracker? = null
    private var loginButton: LoginButton? = null
    private var profileTracker: ProfileTracker? = null
    internal lateinit var mQueue: RequestQueue

    private val callback = object : FacebookCallback<LoginResult> {
        override fun onSuccess(loginResult: LoginResult) {
            handleFacebookAccessToken(loginResult.accessToken)

        }

        override fun onCancel() {
            Toast.makeText(this@LoginActivity, "Cancelado", Toast.LENGTH_SHORT).show()
        }

        override fun onError(error: FacebookException) {
            Toast.makeText(this@LoginActivity, "" + error, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mQueue = Volley.newRequestQueue(this@LoginActivity)
        progreso = ProgressDialog(this@LoginActivity)
        mAuth = FirebaseAuth.getInstance()
        FacebookSdk.sdkInitialize(this)
        FacebookSdk.setApplicationId(resources.getString(R.string.facebook_app_id))
        callbackManager = CallbackManager.Factory.create()

        accessTokenTracker = object : AccessTokenTracker() {
            override fun onCurrentAccessTokenChanged(oldAccessToken: AccessToken, currentAccessToken: AccessToken) {}
        }
        profileTracker = object : ProfileTracker() {
            override fun onCurrentProfileChanged(oldProfile: Profile, newProfile: Profile) {

            }
        }
        accessTokenTracker!!.startTracking()
        profileTracker!!.startTracking()
        loginButton = findViewById<View>(R.id.login_button) as LoginButton

        loginButton!!.setReadPermissions(Arrays.asList("public_profile", "email"))
        loginButton!!.registerCallback(callbackManager, callback)

        val register = findViewById<View>(R.id.email_register_button) as Button
        register.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterUser::class.java)
            startActivity(intent)
        }



        mEmailView = findViewById<View>(R.id.email) as AutoCompleteTextView
        mPasswordView = findViewById<View>(R.id.password) as EditText
        val mEmailSignInButton = findViewById<View>(R.id.email_sign_in_button) as Button
        mEmailSignInButton.setOnClickListener { attemptLogin() }
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        progreso!!.setMessage("Iniciando sesion...\t")
        progreso!!.setCancelable(false)
        progreso!!.show()
        val credential = FacebookAuthProvider.getCredential(token.token)
        mAuth!!.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        val user = mAuth!!.currentUser
                        if (user != null) {
                            val avatar = "https://graph.facebook.com/" + user.providerData[1].uid + "/picture?height=150"
                            if (user.email == null) {
                                registerUserToDB(user.uid, user.uid, user.displayName, avatar, "")
                            } else {
                                registerUserToDB(user.uid, user.email, user.displayName, avatar, "")
                            }

                            Log.d("Datos re:", "UID:" + user.uid + "\tEmail:" + user.email + "\tNombres:" + user.displayName + "\tAvatar:" + avatar + "\tCelular:" + user.phoneNumber)
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(this@LoginActivity, "Cuenta suspendida",
                                Toast.LENGTH_SHORT).show()
                        progreso!!.dismiss()


                        // FirebaseAuth.getInstance().signOut();
                    }

                    // ...
                }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager!!.onActivityResult(requestCode, resultCode, data)
    }

    private fun updateUI() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
        overridePendingTransition(0, 0)

    }

    private fun registerUserToDB(userId: String, email: String?, fullnames: String?, avatar: String, gender: String) {
        val url = "https://www.vluver.com/mobile/registerUserToDB.php"
        val strReq = object : StringRequest(Request.Method.POST,
                url, Response.Listener { response ->
            try {
                val jsonObject = JSONObject(response)
                val error = jsonObject.getBoolean("error")
                if (!error) {
                    progreso!!.dismiss()
                    updateUI()
                } else {
                    val errorMsg = jsonObject.getString("error_msg")
                    if (errorMsg == "Este usuario ya existe") {
                        updateUI()
                        progreso!!.dismiss()
                    } else {
                        progreso!!.dismiss()
                        Toast.makeText(this@LoginActivity, "Algo anda mal", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: JSONException) {
                progreso!!.dismiss()
                Toast.makeText(this@LoginActivity, "Algo anda mal$e", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }, Response.ErrorListener { error ->
            progreso!!.dismiss()
            Toast.makeText(applicationContext, "234: " + error.message, Toast.LENGTH_LONG).show()
        }) {

            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["UID"] = userId
                params["email"] = email!!
                params["fullnames"] = fullnames!!
                params["avatar"] = avatar
                params["gender"] = gender
                params["phone"] = ""
                return params
            }

        }
        strReq.retryPolicy = DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        mQueue.add(strReq)
    }


    private fun attemptLogin() {


        // Reset errors.
        mEmailView!!.error = null
        mPasswordView!!.error = null

        // Store values at the time of the login attempt.
        val email = mEmailView!!.text.toString()
        val password = mPasswordView!!.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView!!.error = "invalid password"
            focusView = mPasswordView
            cancel = true
        }
        // Check for a valid password.
        if (TextUtils.isEmpty(password)) {
            mPasswordView!!.error = "Field required"
            focusView = mPasswordView
            cancel = true
        }
        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView!!.error = "Field required"
            focusView = mEmailView
            cancel = true
        } else if (!isEmailValid(email)) {
            mEmailView!!.error = "Invalid email"
            focusView = mEmailView
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView!!.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            progreso!!.setMessage("Login now....")
            progreso!!.show()
            //mAuthTask = new UserLoginTask(email, password);
            //mAuthTask.execute((Void) null);
            login_user(email, password)
        }
    }

    private fun isEmailValid(email: String): Boolean {
        //TODO: Replace this with your own logic
        return email.contains("@")
    }

    private fun isPasswordValid(password: String): Boolean {
        //TODO: Replace this with your own logic
        return password.length > 4
    }

    private fun login_user(mEmail: String, mPassword: String) {
        mAuth!!.signInWithEmailAndPassword(mEmail, mPassword)
                .addOnCompleteListener(this@LoginActivity) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        //Log.d(TAG, "signInWithEmail:success");
                        // FirebaseUser user = mAuth.getCurrentUser();
                        progreso!!.dismiss()
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()


                    } else {
                        // If sign in fails, display a message to the user.
                        //Log.w(TAG, "signInWithEmail:failure", task.getException());
                        //  showProgress(false);

                        mPasswordView!!.error = "Error password or email"
                        mPasswordView!!.requestFocus()
                        progreso!!.dismiss()
                        //  updateUI(null);
                    }
                }
    }
}
