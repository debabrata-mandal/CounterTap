package com.countertap.customer.viewmodel

import androidx.lifecycle.ViewModel
import com.countertap.customer.repository.MenuDisplayPreferenceRepository
import com.countertap.customer.ui.menu.MenuDisplayMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MenuDisplayViewModel @Inject constructor(
    private val repository: MenuDisplayPreferenceRepository
) : ViewModel() {
    val mode = repository.mode

    fun setMode(mode: MenuDisplayMode) = repository.setMode(mode)
}
