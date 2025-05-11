package com.example.drinklist.model

import com.google.gson.annotations.SerializedName

data class DrinkDetailResponse(
    @SerializedName("drinks") val drinks: List<DrinkDetail>?
)