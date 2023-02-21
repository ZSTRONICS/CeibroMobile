package com.zstronics.ceibro.data.sessions

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.zstronics.ceibro.base.KEY_USERNAME
import com.zstronics.ceibro.data.repos.auth.login.Tokens
import com.zstronics.ceibro.data.repos.auth.login.User
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton


/**
 * To improve the performanceSharedPreferenceManager  @constructor is private.
 * you must need to  access Singleton instance of SharedPreferenceManager
 * i.e SharedPreferenceManager.getInstance(context)
 * @see SingletonHolder
 * */
@Singleton
class SharedPreferenceManager @Inject constructor(@ApplicationContext val context: Context) {

    private val PREFS_NAME = "CEIBRO"
    private var sharedPref: SharedPreferences

    init {
        sharedPref = initializeEncryptedSharedPreferencesManager()
    }

    private fun initializeEncryptedSharedPreferencesManager(): SharedPreferences {
        val spec: KeyGenParameterSpec = KeyGenParameterSpec.Builder(
            MasterKey.DEFAULT_MASTER_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(MasterKey.DEFAULT_AES_GCM_MASTER_KEY_SIZE)
            .build()

        val masterKey = MasterKey.Builder(context)
            .setKeyGenParameterSpec(spec)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey, // masterKey created above
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
//        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
//
//        return EncryptedSharedPreferences.create(
//            PREFS_NAME,
//            masterKeyAlias,
//            context,
//            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
//            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
//        )
    }


    fun saveString(KEY_NAME: String, text: String) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putString(KEY_NAME, text)
        editor.commit()
        editor.apply()
    }

    fun saveCompleteUserObj(KEY_NAME: String, userObj: User) {
        //For storing complete data object in shared preferences
        val gson = Gson()
        val json = gson.toJson(userObj)
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putString(KEY_NAME, json)
        editor.commit()
        editor.apply()
    }

    fun saveCompleteTokenObj(KEY_NAME: String, tokenObj: Tokens) {
        //For storing complete data object in shared preferences
        val gson = Gson()
        val json = gson.toJson(tokenObj)
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putString(KEY_NAME, json)
        editor.commit()
        editor.apply()
    }
    fun saveInt(KEY_NAME: String, value: Int) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putInt(KEY_NAME, value)
        editor.commit()
        editor.apply()
    }
    fun saveBoolean(KEY_NAME: String, status: Boolean) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putBoolean(KEY_NAME, status)
        editor.commit()
        editor.apply()
    }




    fun getValueString(KEY_NAME: String): String? {
        return sharedPref.getString(KEY_NAME, null)
    }

    fun getCompleteUserObj(KEY_NAME: String): User? {
        val gson = Gson()
        val json = sharedPref.getString(KEY_NAME, "")
        return gson.fromJson(json, User::class.java)
    }
    fun getCompleteTokenObj(KEY_NAME: String): Tokens? {
        val gson = Gson()
        val json = sharedPref.getString(KEY_NAME, "")
        return gson.fromJson(json, Tokens::class.java)
    }

    fun getValueInt(KEY_NAME: String): Int {
        return sharedPref.getInt(KEY_NAME, 0)
    }

    fun getValueBoolean(KEY_NAME: String, defaultValue: Boolean): Boolean {
        return sharedPref.getBoolean(KEY_NAME, defaultValue)
    }

    fun removeQuotesAndUnescape(uncleanJson: String?): String? {
        val noQuotes = uncleanJson?.replace("^\"|\"$".toRegex(), "")
        return noQuotes
    }

    fun clearSharedPreference() {
        try {
            val editor: SharedPreferences.Editor = sharedPref.edit()
            val allEntr: Map<String, *> = sharedPref.all
            for (entry in allEntr.keys) {
                editor.remove(entry)
            }
            editor.commit()
            editor.apply()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun removeValue(KEY_NAME: String) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.remove(KEY_NAME)
        editor.commit()
        editor.apply()
    }


    fun getDecryptedUserName(): String? {
        return getValueString(KEY_USERNAME)
    }


}
