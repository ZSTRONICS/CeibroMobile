package com.zstronics.ceibro.utils

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

const val LOCAL_PREF = "local_pref";
const val KEY_LOCAL = "local";

object LocaleChanger {
    @JvmStatic
    fun wrapContext(context: Context): Context {
        val savedLocale = createLocaleFromSavedLanguage(context)
        // as part of creating a new context that contains the new locale we also need to override the default locale.
        Locale.setDefault(savedLocale)
        // create new configuration with the saved locale
        val newConfig = Configuration()
        newConfig.setLocale(savedLocale)
        return context.createConfigurationContext(newConfig)
    }

    @JvmStatic
    private fun createLocaleFromSavedLanguage(mContext: Context): Locale {
        val local = mContext.getSharedPreferences(LOCAL_PREF, Context.MODE_PRIVATE)
            .getString(KEY_LOCAL, "en")!!
        return Locale(local)
    }

    @JvmStatic
    fun changeLanguage(mContext: Context, langCode: String) {
        mContext.getSharedPreferences(LOCAL_PREF, Context.MODE_PRIVATE)
            .edit().apply {
                putString(KEY_LOCAL, langCode)
                apply()
            }
    }
}