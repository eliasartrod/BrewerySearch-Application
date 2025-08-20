package com.composeproject.brewerysearch_app.data.remote

import com.composeproject.brewerysearch_app.data.networkmodels.NetworkBreweryDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// Open Brewery DB API interface
interface ApiService {
    @GET("v1/breweries")
    suspend fun getBreweryList(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): List<NetworkBreweryDto>

    // Autocomplete endpoint returning up to 15 matches
    @GET("v1/breweries/autocomplete")
    suspend fun autocomplete(
        @Query("query") query: String
    ): List<NetworkBreweryDto>

    // Get a single brewery by id
    @GET("v1/breweries/{id}")
    suspend fun getBreweryById(
        @Path("id") id: String
    ): NetworkBreweryDto
}