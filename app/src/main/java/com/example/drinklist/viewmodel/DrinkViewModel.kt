package com.example.drinklist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drinklist.model.DrinkDetail
import com.example.drinklist.model.DrinkDetailResponse
import com.example.drinklist.model.DrinkSummary
import com.example.drinklist.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

class DrinkViewModel : ViewModel() {
    private val _drinkList = MutableStateFlow<List<DrinkSummary>>(emptyList())
    //val drinkList: StateFlow<List<DrinkSummary>> = _drinkList

    private val _selectedDrink = MutableStateFlow<DrinkDetail?>(null)
    val selectedDrink: StateFlow<DrinkDetail?> = _selectedDrink

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private var hasFetchedInitialData = false


    val _alcoholicDrinks = MutableStateFlow<List<DrinkSummary>>(emptyList())
    val _nonAlcoholicDrinks = MutableStateFlow<List<DrinkSummary>>(emptyList())

    val alcoholicDrinks: StateFlow<List<DrinkSummary>> get() = _alcoholicDrinks
    val nonAlcoholicDrinks: StateFlow<List<DrinkSummary>> get() = _nonAlcoholicDrinks

//    val drinkList: StateFlow<List<DrinkSummary>> get() = when(currentFilter) {
//        "Alcoholic" -> _alcoholicDrinks
//        "Non_Alcoholic" -> _nonAlcoholicDrinks
//        else -> _alcoholicDrinks // Default to alcoholic
//    }
//
//
//    fun fetchDrinksIfNeeded(filter: String? = null) {
//        //if (currentFilter != filter) {
//            currentFilter = filter
//            fetchDrinks(filter)
//        //}
//    }

    fun fetchDrinks(filter: String? = null) {
        viewModelScope.launch {
            _loading.value = true
            try {
                when (filter) {
                    "Alcoholic" -> {
                        if (_alcoholicDrinks.value.isNotEmpty()) return@launch
                        val response = RetrofitClient.apiService.getDrinksByAlcoholic("Alcoholic")
                        if (response.isSuccessful) _alcoholicDrinks.value = response.body()?.drinks ?: emptyList()
                    }
                    "Non_Alcoholic" -> {
                        if (_nonAlcoholicDrinks.value.isNotEmpty()) return@launch
                        val response = RetrofitClient.apiService.getDrinksByAlcoholic("Non_Alcoholic")
                        if (response.isSuccessful) _nonAlcoholicDrinks.value = response.body()?.drinks ?: emptyList()
                    }
                }
            } catch (e: Exception) {
                // obsługa błędów
            } finally {
                _loading.value = false
            }
        }
    }

    fun fetchDrinkDetail(drinkId: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = RetrofitClient.apiService.getDrinkDetail(drinkId)
                if (response.isSuccessful) {
                    _selectedDrink.value = response.body()?.drinks?.firstOrNull()
                }
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            } finally {
                _loading.value = false
            }
        }
    }
}