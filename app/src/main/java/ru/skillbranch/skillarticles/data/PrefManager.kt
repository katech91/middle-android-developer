package ru.skillbranch.skillarticles.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.zip
import ru.skillbranch.skillarticles.App
import ru.skillbranch.skillarticles.data.delegates.PrefDelegate

val Context.dataStore : DataStore<Preferences> by preferencesDataStore(name = "settings")

class PrefManager(context: Context = App.applicationContext()) {

    var testInt by PrefDelegate(Int.MAX_VALUE)

    var testLong by PrefDelegate(Long.MAX_VALUE)

    var testDouble by PrefDelegate(Double.MAX_VALUE)

    var testFloat by PrefDelegate(Float.MAX_VALUE)

    var testString by PrefDelegate("test")

    var testBoolean by PrefDelegate(false)

    val dataStore = context.dataStore
    private val errHandler = CoroutineExceptionHandler { _, th ->
        Log.e("PrefManager", "err ${th.message}")
        //TODO handle error this
    }
    internal val scope = CoroutineScope(SupervisorJob() + errHandler)

    var isBigText by PrefDelegate(false)
    var isDarkMode by PrefDelegate(false)

    val settings: LiveData<AppSettings>
        get() {
            val isBig = dataStore.data.map { it[booleanPreferencesKey(this::isBigText.name)] ?: false }
            val isDark = dataStore.data.map { it[booleanPreferencesKey(this::isDarkMode.name)] ?: false }
            return isDark.zip(isBig){ dark, big -> AppSettings(dark, big) }
                .onEach { Log.e("PrefManager", "settings $it") }
                .distinctUntilChanged()
                .asLiveData()
        }
}