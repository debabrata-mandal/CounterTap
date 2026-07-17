package com.countertap.customer.repository

import android.content.Context
import com.countertap.customer.ui.menu.MenuDisplayMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MenuDisplayPreferenceRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _mode = MutableStateFlow(loadMode())
    val mode: StateFlow<MenuDisplayMode> = _mode.asStateFlow()

    fun setMode(mode: MenuDisplayMode) {
        prefs.edit().putString(KEY_MODE, mode.name).apply()
        _mode.value = mode
    }

    private fun loadMode(): MenuDisplayMode =
        runCatching {
            MenuDisplayMode.valueOf(prefs.getString(KEY_MODE, MenuDisplayMode.Book.name)!!)
        }.getOrDefault(MenuDisplayMode.Book)

    companion object {
        private const val PREFS_NAME = "menu_display"
        private const val KEY_MODE = "mode"
    }
}
