package com.zstronics.ceibro.base.validator.binding

import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
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
        val context = imageView.context

        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()

        val requestOptions = RequestOptions()
            .placeholder(circularProgressDrawable)
            .error(R.drawable.profile_img)
            .skipMemoryCache(true)
            .centerCrop()

        Glide.with(context)
            .load(imageUrl)
            .placeholder(R.drawable.profile_img)
            .into(imageView)
    }

    @JvmStatic
    @BindingAdapter(
        value = ["app:loadSmallImageFromUrl"],
        requireAll = false
    )
    fun loadSmallProductImage(
        imageView: AppCompatImageView,
        imageUrl: String?,
    ) {
        val context = imageView.context

        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 4f
        circularProgressDrawable.centerRadius = 14f
        circularProgressDrawable.start()

        val requestOptions = RequestOptions()
            .placeholder(circularProgressDrawable)
            .error(R.drawable.profile_img)
            .skipMemoryCache(true)
            .centerCrop()

        Glide.with(context)
            .load(imageUrl)
            .apply(requestOptions)
            .transition(DrawableTransitionOptions.withCrossFade())
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

    @JvmStatic
    @BindingAdapter(
        value = ["app:checked"],
        requireAll = false
    )
    fun setButtonChecked(
        button: AppCompatButton,
        isSelected: MutableLiveData<Boolean>
    ) {
        button.isSelected = isSelected.value ?: false
    }
}