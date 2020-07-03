package com.example.recyclerview_retrofit2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.GsonBuilder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.internal.schedulers.IoScheduler
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var adapter: RecyclerViewAdapter? = null
    private var movies: ArrayList<Movie> = ArrayList<Movie>()

    lateinit var rv: RecyclerView

    lateinit var bGetAll: Button
    lateinit var bPost: Button
    lateinit var bPostMultipart: Button

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rv = findViewById(R.id.list_recycler_view) as RecyclerView

        bGetAll = findViewById(R.id.bGetAll)
        bPost = findViewById(R.id.bPost)
        bPostMultipart = findViewById(R.id.bPostMultipart)

        bGetAll.setOnClickListener(this);
        bPost.setOnClickListener(this);
        bPostMultipart.setOnClickListener(this);
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
        }
    }
}
