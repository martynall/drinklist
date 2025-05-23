package com.example.drinklist.network

import com.example.drinklist.model.DrinkDetailResponse
import com.example.drinklist.model.DrinkListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface DrinkApiService {
    // Get popular cocktails (main page)
    @GET("filter.php")
    suspend fun getPopularDrinks(
        @Query("c") category: String = "Cocktail"
    ): Response<DrinkListResponse>

    // Get drinks by alcoholic filter
    @GET("filter.php")
    suspend fun getDrinksByAlcoholic(
        @Query("a") alcoholic: String // "Alcoholic" or "Non_Alcoholic"
    ): Response<DrinkListResponse>

    // Get drink details by ID
    @GET("lookup.php")
    suspend fun getDrinkDetail(
        @Query("i") drinkId: String
    ): Response<DrinkDetailResponse>
}