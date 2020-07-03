package com.example.recyclerview_retrofit2

import com.example.recyclerview_retrofit2.Movie
import io.reactivex.Observable
import retrofit2.http.GET

public interface ApiService {

    @GET("misc/api/movie.php")
    fun getAllPosts(): Observable<ArrayList<Movie>>

}