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
    val drinkList: StateFlow<List<DrinkSummary>> = _drinkList

    private val _selectedDrink = MutableStateFlow<DrinkDetail?>(null)
    val selectedDrink: StateFlow<DrinkDetail?> = _selectedDrink

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    // Funkcja do pobierania listy drinków
    fun fetchDrinks() {
        viewModelScope.launch {
            _loading.value = true
            val response = RetrofitClient.apiService.getDrinks()
            _loading.value = false
            if (response.isSuccessful) {
                _drinkList.value = response.body()?.drinks ?: emptyList()
            }
        }
    }

    // Funkcja do pobierania szczegółów drinka
    fun fetchDrinkDetail(drinkId: String) {
        viewModelScope.launch {
            _loading.value = true
            val response: Response<DrinkDetailResponse> =
                RetrofitClient.apiService.getDrinkDetail(drinkId)
            _loading.value = false
            if (response.isSuccessful) {
                _selectedDrink.value = response.body()?.drinks?.firstOrNull()
            }
        }
    }
}