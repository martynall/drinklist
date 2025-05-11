package com.example.drinklist.model

import com.google.gson.annotations.SerializedName

data class DrinkListResponse(
    @SerializedName("drinks") val drinks: List<DrinkSummary>?
)