package com.composeproject.brewerysearch_app.data.remote

import com.composeproject.brewerysearch_app.data.networkmodels.NetworkBreweryDto
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

// Data source responsible for fetching data from the network (via Retrofit + OkHttp)
class NetworkDataSource(
    private val api: ApiService
) {
    companion object {
        // Factory method to create an instance of NetworkDataSource with default setup
        fun create(baseUrl: String = "https://api.openbrewerydb.org/"): NetworkDataSource {

            // Logging interceptor to print HTTP request/response details (useful for debugging)
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            // OkHttp client with logging enabled
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()

            // Moshi JSON parser setup with reflection support for Kotlin data classes
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            // Retrofit instance configured with base URL, Moshi converter, and custom client
            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl) // base URL for all API requests
                .addConverterFactory(MoshiConverterFactory.create(moshi)) // JSON <-> Kotlin conversion
                .client(client) // attach OkHttp client
                .build()

            // Create Retrofit implementation of the ApiService interface
            val api = retrofit.create(ApiService::class.java)

            // Return a NetworkDataSource with the configured ApiService
            return NetworkDataSource(api)
        }
    }

    // Public suspend function to fetch a list of breweries from the API
    suspend fun getBreweryList(page: Int, perPage: Int): List<NetworkBreweryDto> = api.getBreweryList(page, perPage)

    // Autocomplete breweries based on query
    suspend fun autocomplete(query: String): List<NetworkBreweryDto> = api.autocomplete(query)

    // Fetch a single brewery by id
    suspend fun getBreweryById(id: String): NetworkBreweryDto = api.getBreweryById(id)
}