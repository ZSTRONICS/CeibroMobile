package com.zstronics.ceibro.base.validator.binding

import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.zstronics.ceibro.R


object ImageBinding {

    @JvmStatic
    @BindingAdapter(
        value = ["app:loadImageFromUrl"],
        requireAll = false
    )
    fun loadProductImage(
        imageView: AppCompatImageView,
        imageUrl: String?,
    ) {
        Glide.with(imageView.context)
            .load(imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .placeholder(R.drawable.profile_img)
            .into(imageView)
    }

    @JvmStatic
    @BindingAdapter(
        value = ["app:setTextNow"],
        requireAll = false
    )
    fun setEditText(
        editText: EditText,
        text: String
    ) {
        editText.setText("$text")
    }

    @JvmStatic
    @BindingAdapter(
        value = ["app:firstName", "app:surName"],
        requireAll = false
    )
    fun useInitial(
        textView: TextView,
        firstName: String?,
        surName: String?
    ) {
        textView.text = "${firstName?.get(0)?.uppercaseChar()}${surName?.get(0)?.uppercaseChar()}"
    }

    @JvmStatic
    @BindingAdapter(
        value = ["app:groupTitle"],
        requireAll = false
    )
    fun groupInitials(
        textView: TextView,
        groupTitle: String?
    ) {
        textView.text = "${groupTitle?.get(0)?.uppercaseChar()}"
    }
}