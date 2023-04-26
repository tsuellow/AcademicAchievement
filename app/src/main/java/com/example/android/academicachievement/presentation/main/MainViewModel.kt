package com.example.android.academicachievement.presentation.main

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.academicachievement.common.PreferenceManager
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferences: PreferenceManager
): ViewModel() {

    var adminPin = mutableStateOf("")
        private set

    var fullName = mutableStateOf("")
        private set

    var credentialsInvalidated = mutableStateOf(false)
        private set

    init {
        getAdminPinAndName()
    }

    private fun getAdminPinAndName(){
        viewModelScope.launch {
            adminPin.value=preferences.getAdminPin()
            fullName.value=preferences.getLoginData().fullName
        }
    }

    fun verifyAuthorization(pin:String):Boolean{
        return pin.contentEquals(adminPin.value)
    }

    fun forgetLoginData(){
        viewModelScope.launch {
            preferences.saveLoginData(PreferenceManager.LoginData(login = "", fullName = "", pin = ""))
            credentialsInvalidated.value=true
        }
    }
}