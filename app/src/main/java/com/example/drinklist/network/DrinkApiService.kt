package com.example.drinklist.network

import com.example.drinklist.model.DrinkDetailResponse
import com.example.drinklist.model.DrinkListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface DrinkApiService {
    // Metoda do pobierania listy drinków
    @GET("filter.php")
    suspend fun getDrinks(
        @Query("c") category: String = "Cocktail"
    ): Response<DrinkListResponse>

    // Metoda do pobierania szczegółów drinka po ID
    @GET("lookup.php")
    suspend fun getDrinkDetail(
        @Query("i") drinkId: String
    ): Response<DrinkDetailResponse>
}