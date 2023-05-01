package com.zstronics.ceibro.resourses

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import androidx.annotation.*

interface IResourceProvider {
    var context: Context
    fun getString(resourceIdentifier: Int, vararg arguments: Any = arrayOf()): String

    fun getString(@StringRes resourceIdentifier: Int): String

    fun getStringArray(resourceIdentifier: Int): Array<String>

    fun getInteger(@IntegerRes resourceIdentifier: Int): Int

    fun getIntegerArray(@ArrayRes resourceIdentifier: Int): Array<Int>

    fun getBoolean(@BoolRes resourceIdentifier: Int): Boolean

    fun getColor(resourceIdentifier: Int): Int

    fun obtainTypedArray(@ArrayRes resourceIdentifier: Int): TypedArray

    fun getDrawable(@DrawableRes resourceIdentifier: Int): Drawable?

    fun toast(message: String)

    fun getColorFromArray(
        colorArray: IntArray,
        position: Int
    ): Int
}