package com.zstronics.ceibro.resourses

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import androidx.annotation.*
import androidx.core.content.ContextCompat
import com.zstronics.ceibro.base.extensions.getColors
import com.zstronics.ceibro.base.extensions.toast
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidResourceProvider @Inject constructor(@ApplicationContext val appContext: Context) :
    IResourceProvider {
    override var context: Context = appContext
    override fun getString(@StringRes resourceIdentifier: Int, vararg arguments: Any): String {
        return if (arguments.isNotEmpty())
            appContext.resources.getString(resourceIdentifier, *arguments)
        else
            appContext.resources.getString(resourceIdentifier)
    }

    override fun getString(@StringRes resourceIdentifier: Int): String =
        appContext.getString(resourceIdentifier)
    override fun getStringArray(@ArrayRes resourceIdentifier: Int): Array<String> =
        appContext.resources.getStringArray(resourceIdentifier)

    override fun getInteger(@IntegerRes resourceIdentifier: Int): Int =
        appContext.resources.getInteger(resourceIdentifier)

    override fun getIntegerArray(@ArrayRes resourceIdentifier: Int): Array<Int> =
        appContext.resources.getIntArray(resourceIdentifier).toTypedArray()

    override fun getBoolean(@BoolRes resourceIdentifier: Int): Boolean =
        appContext.resources.getBoolean(resourceIdentifier)

    override fun getColor(@ColorRes resourceIdentifier: Int): Int =
        appContext.getColors(resourceIdentifier)

    override fun obtainTypedArray(@ArrayRes resourceIdentifier: Int): TypedArray =
        appContext.resources.obtainTypedArray(resourceIdentifier)

    override fun getDrawable(resourceIdentifier: Int): Drawable? =
        ContextCompat.getDrawable(appContext, resourceIdentifier)

    override fun toast(message: String) {
        appContext.toast(message)
    }

    override fun getColorFromArray(
        colorArray: IntArray,
        position: Int
    ): Int {
        return colorArray[position % colorArray.size]
    }
}