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
import android.widget.Toast
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

    lateinit var itTitle: EditText
    lateinit var itYear: EditText

    private val TAG = MainActivity::class.qualifiedName

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rv = findViewById(R.id.list_recycler_view) as RecyclerView

        bGetAll = findViewById(R.id.bGetAll)
        bPost = findViewById(R.id.bPost)

        itTitle = findViewById(R.id.itTitle)
        itYear = findViewById(R.id.itYear)

        bGetAll.setOnClickListener(this);
        bPost.setOnClickListener(this);
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
        }
    }
}
