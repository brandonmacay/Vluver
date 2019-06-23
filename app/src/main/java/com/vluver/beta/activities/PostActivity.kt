package com.vluver.beta.activities

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.tangxiaolv.telegramgallery.GalleryActivity
import com.tangxiaolv.telegramgallery.GalleryConfig
import com.vluver.beta.R
import com.vluver.beta.adapter.MultiAddImages
import com.vluver.beta.model.AddMoreImages
import com.vluver.beta.servicebackground.UploadImagesPost
import com.vluver.beta.utils.GlideLoadImages
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.*


class PostActivity : AppCompatActivity() {
    internal lateinit var progressDialog: ProgressDialog
    internal lateinit var new_post_pictures: ImageView
    internal lateinit var regresar: ImageView
    internal lateinit var enviar: ImageView
    internal lateinit var seleccionar_imagen: ImageView
    private var user_image: ImageView? = null
    internal lateinit var descripcion_post: EditText
    internal var imageIsSet = false
    internal var bitmappost: Bitmap? = null
    private var userId: String? = null
    internal var mQueue: RequestQueue? = null


    //firebase
    private var mAuth: FirebaseAuth? = null
    var currentUser: FirebaseUser? = null
    private val channelId = "vluver_id"


    internal var PICK_IMAGE_MULTIPLE = 1
    internal var imagesEncodedList: List<String>? = null
    lateinit var mRVFish: RecyclerView
    lateinit var mAdapter: MultiAddImages
    internal lateinit var dataa: MutableList<AddMoreImages>
    internal lateinit var mGridLayoutManager: GridLayoutManager

    private var mPhotos: ArrayList<String>? = null
    internal lateinit var encodedImageList: ArrayList<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)
        mAuth = FirebaseAuth.getInstance()
        mQueue = Volley.newRequestQueue(this@PostActivity)
        progressDialog = ProgressDialog(this)
        new_post_pictures = findViewById<View>(R.id.new_post_picture) as ImageView
        regresar = findViewById<View>(R.id.back) as ImageView
        enviar = findViewById<View>(R.id.send) as ImageView
        descripcion_post = findViewById<View>(R.id.descripcion) as EditText
        user_image = findViewById<View>(R.id.img_avatar) as ImageView
        seleccionar_imagen = findViewById<View>(R.id.new_post_submit) as ImageView
        regresar.setOnClickListener { finish() }
        mGridLayoutManager = GridLayoutManager(this@PostActivity, 3)
        dataa = ArrayList()
        encodedImageList = ArrayList()
        mRVFish = findViewById<View>(R.id.multimages) as RecyclerView
        mAdapter = MultiAddImages(this@PostActivity, dataa)
        mRVFish.adapter = mAdapter
        mRVFish.layoutManager = mGridLayoutManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 2)
            }
        }
        seleccionar_imagen.setOnClickListener {
            val config = GalleryConfig.Build()
                    .limitPickPhoto(99)
                    .singlePhoto(false)
                    .hintOfPick("max 99")
                    .filterMimeTypes(arrayOf("image/*"))
                    .build()
            GalleryActivity.openActivity(this@PostActivity, 2, config)
        }

        enviar.setOnClickListener {
            if (imageIsSet) {
                val mServiceIntent = Intent(this@PostActivity, UploadImagesPost::class.java)
                mServiceIntent.putExtra("userId", userId)
                mServiceIntent.putStringArrayListExtra("mPhotos", encodedImageList)
                mServiceIntent.putExtra("descripcion", descripcion_post.text.toString().trim { it <= ' ' })
                startService(mServiceIntent)
                finish()
            } else {
                if (descripcion_post.text.toString().isEmpty()) {
                    descripcion_post.error = "Agrega algo a tus publicacion..."
                } else {
                    insertPostToVluver(userId, "null", descripcion_post.text.toString())
                }
            }
        }
        getAvatar()

    }

    private fun getAvatar() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            for (profile in user.providerData) {
                if (profile.providerId == "facebook.com") {
                    val image = "https://graph.facebook.com/" + user.providerData[1].uid + "/picture?type=small"
                    GlideLoadImages.loadAvatar(this@PostActivity, image, user_image)
                } else if (profile.providerId == "google.com") {
                    Toast.makeText(this@PostActivity, "avatar Google", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    private fun sendPost(description: String) {
        progressDialog.setMessage("\tPublicando...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        //insertPostToVluver(userId,"",description);
    }

    private fun insertPostToVluver(creator: String?, codeuniqueimages: String, description: String) {
        val url = "https://vluver.com/mobile/insert/insertPost.php"
        val strReq = object : StringRequest(Request.Method.POST,
                url, Response.Listener { response ->
            try {
                val jsonObject = JSONObject(response)
                val error = jsonObject.getBoolean("error")
                if (!error) {
                    // progressDialog.dismiss();
                    Toast.makeText(this@PostActivity, "Publicacion subida correctamente", Toast.LENGTH_LONG).show()
                    //show_notification("Vluver", "Publicacion lista", 2 );
                    finish()
                } else {
                    //progressDialog.dismiss();
                    val errorMsg = jsonObject.getString("error_msg")
                    Toast.makeText(this@PostActivity, "" + errorMsg, Toast.LENGTH_SHORT).show()
                }

            } catch (e: JSONException) {
                // progressDialog.dismiss();
                Toast.makeText(this@PostActivity, "Algo anda mal$e", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }, Response.ErrorListener { error ->
            //progressDialog.dismiss();
            Toast.makeText(applicationContext, error.message, Toast.LENGTH_LONG).show()
        }) {

            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["creator"] = creator!!
                params["namecode"] = codeuniqueimages
                params["description"] = description
                return params
            }


        }
        strReq.retryPolicy = DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        mQueue!!.add(strReq)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        try {
            if (data != null) {
                mPhotos = data.getSerializableExtra(GalleryActivity.PHOTOS) as ArrayList<String>
                imageIsSet = true
                for (photo in mPhotos!!) {
                    val uri = Uri.fromFile(File(photo))
                    val addMoreImages = AddMoreImages()
                    addMoreImages.mArrayUri = uri
                    dataa.add(addMoreImages)
                    encodedImageList.add(photo)
                }
                mAdapter.notifyDataSetChanged()
                mRVFish.adapter = mAdapter
            } else {
                imageIsSet = false
                Toast.makeText(this, "Ninguna foto seleccionada!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            imageIsSet = false
            Toast.makeText(this, "Error de seleccion: $e", Toast.LENGTH_SHORT).show()
        }


    }


    private fun hasGalleryPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun askForGalleryPermission() {
        ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_CODE_READ_PERMISSION)
    }


    override fun onStart() {
        super.onStart()
        currentUser = mAuth!!.currentUser
        userId = currentUser!!.uid
    }

    override fun onDestroy() {
        bitmappost = null
        mQueue!!.stop()
        mQueue = null
        super.onDestroy()
    }

    companion object {
        private val REQUEST_CODE_READ_PERMISSION = 22


        fun hideKeyboard(activity: Activity) {
            val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            //Find the currently focused view, so we can grab the correct window token from it.
            var view = activity.currentFocus
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = View(activity)
            }
            assert(imm != null)
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}
