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

    private var currentFilter: String? = null
    private var hasFetchedInitialData = false


    private val _alcoholicDrinks = MutableStateFlow<List<DrinkSummary>>(emptyList())
    private val _nonAlcoholicDrinks = MutableStateFlow<List<DrinkSummary>>(emptyList())

    val drinkList: StateFlow<List<DrinkSummary>> get() = when(currentFilter) {
        "Alcoholic" -> _alcoholicDrinks
        "Non_Alcoholic" -> _nonAlcoholicDrinks
        else -> _alcoholicDrinks // Default to alcoholic
    }


    fun fetchDrinksIfNeeded(filter: String? = null) {
        if (currentFilter != filter) {
            currentFilter = filter
            fetchDrinks(filter)
        }
    }

    private fun fetchDrinks(filter: String? = null) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = when (filter) {
                    "Alcoholic" -> {
                        RetrofitClient.apiService.getDrinksByAlcoholic("Alcoholic").also {
                            if (it.isSuccessful) _alcoholicDrinks.value = it.body()?.drinks ?: emptyList()
                        }
                    }
                    "Non_Alcoholic" -> {
                        RetrofitClient.apiService.getDrinksByAlcoholic("Non_Alcoholic").also {
                            if (it.isSuccessful) _nonAlcoholicDrinks.value = it.body()?.drinks ?: emptyList()
                        }
                    }
                    else -> null
                }
            } catch (e: Exception) {
                // Handle error
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