package com.example.recyclerview_retrofit2

import io.reactivex.Observable
import retrofit2.http.GET

public interface APIService {

    @GET("api/v1/movie.php")
    fun getAllPosts(): Observable<ArrayList<Movie>>

}