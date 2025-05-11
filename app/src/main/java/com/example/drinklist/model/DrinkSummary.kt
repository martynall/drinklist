package com.example.drinklist.model

import com.google.gson.annotations.SerializedName

data class DrinkSummary(
    @SerializedName("idDrink") val idDrink: String?,
    @SerializedName("strDrink") val strDrink: String?,
    @SerializedName("strDrinkThumb") val strDrinkThumb: String?
)