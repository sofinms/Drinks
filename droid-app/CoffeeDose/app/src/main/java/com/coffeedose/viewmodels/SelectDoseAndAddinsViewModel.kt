package com.coffeedose.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.coffeedose.database.CoffeeDatabase
import com.coffeedose.domain.Addin
import com.coffeedose.domain.CoffeeSize
import com.coffeedose.domain.OrderDetail
import com.coffeedose.extensions.mutableLiveData
import com.coffeedose.network.HttpExceptionEx
import com.coffeedose.repository.AddinsRepository
import com.coffeedose.repository.OrderDetailsRepository
import com.coffeedose.repository.SizesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SelectDoseAndAddinsViewModel(application: Application,var drinkId : Int) : AndroidViewModel(application) {

    private val viewModelJob = Job()

    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val sizesRepository = SizesRepository(CoffeeDatabase.getInstance(application).sizeDatabaseDao)
    private val addinsRepository = AddinsRepository(CoffeeDatabase.getInstance(application).addinsDatabaseDao)
    private val orderDetailsRepository= OrderDetailsRepository(CoffeeDatabase.getInstance(application).orderDetailsDatabaseDao)

    private val addinsTotal = mutableLiveData(0)

    val count = mutableLiveData(1)

    var isRefreshing = mutableLiveData(false)

    var errorMessage : MutableLiveData<String?> = mutableLiveData(null)

    fun getSummary() : LiveData<String>{
        var result = MediatorLiveData<String>()

        val update = {
            val portionPrice = (addinsTotal.value?:0) + if (selectedSize.value == null) 0 else selectedSize.value!!.price
            val countP = count.value!!

            result.value = "Итого: $portionPrice * $countP = ${portionPrice*countP} Р."
        }

        result.addSource(selectedSize) { update.invoke()}
        result.addSource(addinsTotal) { update.invoke()}
        result.addSource(count) { update.invoke()}

        return result

    }

    private var selectedItemIndex = mutableLiveData(-1)

    val sizes = sizesRepository.getSizes(drinkId)
    val addins = addinsRepository.addins

    val selectedSize : LiveData<CoffeeSize?>
        get() = _selectedSize

    private var _selectedSize  = Transformations.map(selectedItemIndex){
        if (sizes.value?.isEmpty() != false)
            return@map null
        else return@map sizes.value!![selectedItemIndex.value!!]
    }


    init {
        viewModelScope.launch {
            sizesRepository.refreshSizes(drinkId)
            addinsRepository.refreshAddins()
        }
    }

    fun refreshData(showRefresh:Boolean = false){
        viewModelScope.launch {
            try {

                if (showRefresh) isRefreshing.value = true

                sizesRepository.refreshSizes(drinkId)
                addinsRepository.refreshAddins()

                errorMessage.value = null
            }
            catch (responseEx: HttpExceptionEx){
                errorMessage.value = responseEx.error.title
            }
            catch (ex: java.lang.Exception){
                errorMessage.value = ex.message
            }
            finally {
                if (showRefresh) isRefreshing.value = false
            }
        }
    }

    fun onSelectedSizeIndexChanged(newIndex : Int){
        if (newIndex != selectedItemIndex.value)
            selectedItemIndex.value = newIndex
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun updateTotalOnAddinCheck(addin : Addin, isChecked : Boolean){
        if (isChecked) addinsTotal.value = addinsTotal.value?.plus(addin.price)
        else addinsTotal.value = addinsTotal.value?.minus(addin.price)

        addin.isSelected = isChecked
    }

    fun updateCount(newValue : Int){
        if (count.value!! != newValue) count.value = newValue
    }

    private fun getAddinsToString() : String? {
        if (addins.value?.size == 0) return null
        else return addins.value!!.filter { it.isSelected }.map { it.price }.joinToString()
    }

    fun saveOrderDetails(){
        viewModelScope.launch {
            try {
                orderDetailsRepository.insertNew( OrderDetail(
                    id = 0,
                    drinkId = drinkId,
                    sizeId = selectedSize.value!!.id,
                    addIns = addins.value?.filter { it.isSelected } ?: listOf(),
                    count = count.value!!,
                    orderId = null
                ))
            }
            catch (ex:Exception){
                Log.d("SelectDoseAndAddinsViewModel.saveOrderDetails",ex.message)
            }
        }
    }


    class Factory(val app: Application, private var drinkId: Int) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SelectDoseAndAddinsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SelectDoseAndAddinsViewModel(app,drinkId) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}