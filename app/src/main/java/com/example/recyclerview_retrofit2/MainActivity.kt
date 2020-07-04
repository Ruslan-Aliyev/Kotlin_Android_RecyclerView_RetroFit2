package com.example.recyclerview_retrofit2

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.GsonBuilder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.internal.schedulers.IoScheduler
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var adapter: RecyclerViewAdapter? = null
    private var movies: ArrayList<Movie> = ArrayList<Movie>()

    lateinit var rv: RecyclerView

    lateinit var bGetAll: Button
    lateinit var bPost: Button
    lateinit var bPostMultipart: Button

    lateinit var itTitle: EditText
    lateinit var itYear: EditText
    lateinit var bChooseImage: Button

    private val REQUEST_CAMERA = 5000
    private val SELECT_FILE = 5001

    private val TAG = MainActivity::class.qualifiedName

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rv = findViewById(R.id.list_recycler_view) as RecyclerView

        bGetAll = findViewById(R.id.bGetAll)
        bPost = findViewById(R.id.bPost)
        bPostMultipart = findViewById(R.id.bPostMultipart)

        itTitle = findViewById(R.id.itTitle)
        itYear = findViewById(R.id.itYear)
        bChooseImage = findViewById(R.id.bChooseImage)

        bGetAll.setOnClickListener(this);
        bPost.setOnClickListener(this);
        bPostMultipart.setOnClickListener(this);
        bChooseImage.setOnClickListener(this);
    }

    override fun onClick(v: View?) {
        when (v?.getId()) {
            R.id.bGetAll -> {
                val retrofit = Retrofit
                    .Builder()
                    .addConverterFactory(
                        GsonConverterFactory
                            .create(
                                GsonBuilder().create()
                            )
                    )
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .baseUrl("https://ruslan-website.com/")
                    .build()

                val moviesApi = retrofit.create(ApiService::class.java)

                var response = moviesApi.getAllPosts()

                response.observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(IoScheduler())
                    .subscribe {
                        movies = it
                        adapter = RecyclerViewAdapter(movies)
                        rv.setAdapter(adapter)
                        rv.setLayoutManager(LinearLayoutManager(getApplicationContext()))
                    }
            }
            R.id.bPost -> {
                if( itTitle.getText().toString().length == 0 ){
                    itTitle.setError( "Title is required!" );
                    return;
                }
                if( itYear.getText().toString().length == 0 ){
                    itYear.setError( "Year is required!" );
                    return;
                }

                val title: RequestBody = RequestBody.create(MediaType.parse("text/plain"), itTitle.getText().toString())
                val year: RequestBody = RequestBody.create(MediaType.parse("text/plain"), itYear.getText().toString())

                val retrofit = Retrofit
                    .Builder()
                    .addConverterFactory(
                        GsonConverterFactory
                            .create(
                                GsonBuilder().create()
                            )
                    )
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .baseUrl("https://ruslan-website.com/")
                    .build()

                val moviesApi = retrofit.create(ApiService::class.java)

                moviesApi.post(title, year)?.enqueue(
                    object : Callback<ResponseBody?> {
                        override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {

                        }
                        override fun onResponse( call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                            Log.i(TAG, response.body().toString())
                        }
                    }
                )
            }
            R.id.bPostMultipart -> {}
            R.id.bChooseImage -> {
                val items = arrayOf<CharSequence>("Take Photo", "Choose from Library", "Cancel")
                val builder: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
                builder.setTitle("Add Photo!")
                builder.setItems(items,
                    DialogInterface.OnClickListener { dialog, item ->
                        if (items[item] == "Take Photo") {
                            cameraIntent()
                        } else if (items[item] == "Choose from Library") {
                            galleryIntent()
                        } else if (items[item] == "Cancel") {
                            dialog.dismiss()
                        }
                    })
                builder.show()
            }
        }
    }

    private fun cameraIntent() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_CAMERA)
    }

    private fun galleryIntent() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode === Activity.RESULT_OK) {
            if (requestCode === SELECT_FILE) {
                onSelectFromGalleryResult(data)
            } else if (requestCode === REQUEST_CAMERA) {
                onCaptureImageResult(data)
            }
        }
    }

    private fun onSelectFromGalleryResult(data: Intent?) {
        var bm: Bitmap? = null
        if (data != null) {
            try {
                val selectedImage: Uri? = data.getData()
                bm = MediaStore.Images.Media.getBitmap(
                    applicationContext.contentResolver,
                    selectedImage
                )
                saveImage(bm)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun onCaptureImageResult(data: Intent?) {
        val thumbnail = data!!.extras!!["data"] as Bitmap?

        saveImage(thumbnail)
    }

    private fun saveImage(thumbnail: Bitmap?) {

    }
}
