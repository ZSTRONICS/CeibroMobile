package com.zstronics.ceibro.base.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Point
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun CollapsingToolbarLayout.enableScroll(@AppBarLayout.LayoutParams.ScrollFlags flags: Int = (AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED)) {
    val params = this.layoutParams as AppBarLayout.LayoutParams
    params.scrollFlags = flags
    this.layoutParams = params
}

fun CollapsingToolbarLayout.disableScroll() {
    val params = this.layoutParams as AppBarLayout.LayoutParams
    params.scrollFlags = 0
    this.layoutParams = params
}

fun ScrollView.scrollToBottomWithoutFocusChange() { // Kotlin extension to scrollView
    val lastChild = getChildAt(childCount - 1)
    val bottom = lastChild.bottom + paddingBottom
    val delta = bottom - (scrollY + height)
    post {
        smoothScrollBy(0, delta)
    }
}

fun ChipGroup.generateChipViews(@LayoutRes itemView: Int, list: List<String>) {
    val inflater: LayoutInflater =
        this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    for (index in list.indices) {
        val categoryName = list[index]
        val chip = inflater.inflate(itemView, this, false) as Chip
        val paddingDp = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 10f,
            this.resources.displayMetrics
        ).toInt()
        chip.setPadding(paddingDp, paddingDp, paddingDp, paddingDp)
        chip.text = categoryName
        this.addView(chip)
    }
}

fun ImageView?.hasBitmap(): Boolean {
    return this?.let {
        this.drawable != null && (this.drawable is BitmapDrawable)
    } ?: false
}

fun View.detachFromParent() {
    (this.parent as ViewGroup?)?.removeView(this)
}

fun View.visible() {
    if (visibility != View.VISIBLE) {
        visibility = View.VISIBLE
    }
}

inline fun View.visiableIf(block: () -> Boolean) {
    if (visibility != View.VISIBLE && block()) {
        visibility = View.VISIBLE
    }
}

fun View.invisiable() {
    if (visibility != View.INVISIBLE) {
        visibility = View.INVISIBLE
    }
}

inline fun View.invisiableIf(block: () -> Boolean) {
    if (visibility != View.INVISIBLE && block()) {
        visibility = View.INVISIBLE
    }
}

fun View.gone() {
    if (visibility != View.GONE) {
        visibility = View.GONE
    }
}

inline fun View.goneIf(block: () -> Boolean) {
    if (visibility != View.GONE && block()) {
        visibility = View.GONE
    }
}

/**
 * Toggle a view's visibility
 */
fun View.toggleVisibility(): View {
    if (visibility == View.VISIBLE) {
        visibility = View.GONE
    } else {
        visibility = View.VISIBLE
    }
    return this
}

fun View.isVisible() = visibility == View.VISIBLE

fun View.isGone() = visibility == View.GONE

fun View.isInvisible() = visibility == View.INVISIBLE
fun <T : View> T.click(block: (T) -> Unit) = setOnClickListener { block(it as T) }

fun <T : View> T.longClick(block: (T) -> Boolean) = setOnLongClickListener { block(it as T) }
fun View.resize(width: Int, height: Int) {
    val lp = layoutParams
    lp?.let {
        lp.width = width
        lp.height = height
        layoutParams = lp
    }
}

/**
 * Extension method to addOnLayoutChangeListener.
 *
 */
fun View.doOnLayout(onLayout: (View) -> Boolean) {
    addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
        override fun onLayoutChange(
            view: View, left: Int, top: Int, right: Int, bottom: Int,
            oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int
        ) {
            if (onLayout(view)) {
                view.removeOnLayoutChangeListener(this)
            }
        }
    })

}

/**
 * Extension method to get ClickableSpan.
 * e.g.
 * val loginLink = getClickableSpan(context.getColorCompat(R.color.colorAccent), { })
 */
fun getClickableSpan(color: Int, action: (view: View) -> Unit): ClickableSpan {
    return object : ClickableSpan() {
        override fun onClick(view: View) {
            action
        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.color = color
        }
    }
}

fun View.getDrawable(@DrawableRes drawResId: Int) = ContextCompat.getDrawable(context, drawResId)

fun Resources.hasNavBar(): Boolean {
    val id = getIdentifier("config_showNavigationBar", "bool", "android")
    return id > 0 && getBoolean(id)
}

fun Resources.getNavBarHeight(): Int {

    val resourceId: Int = getIdentifier("navigation_bar_height", "dimen", "android")
    if (resourceId > 0) {
        return getDimensionPixelSize(resourceId)
    }
    return 0
}

val View.locationOnScreen: Point
    get() = IntArray(2).let {
        getLocationOnScreen(it)
        Point(it[0], it[1])

    }

fun TextView.clickableSpan(
    vararg links: Pair<String, View.OnClickListener>,
    @ColorInt color: Int = 0,
    underline: Boolean = false,
    isBold: Boolean = false
) {
    val spannableString = SpannableString(this.text)
    for (link in links) {
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                Selection.setSelection((view as TextView).text as Spannable, 0)
                view.invalidate()
                link.second.onClick(view)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = underline
                if (isBold) ds.typeface = Typeface.DEFAULT_BOLD
            }
        }

        val startIndexOfLink = this.text.toString().indexOf(link.first)
        spannableString.setSpan(
            clickableSpan, startIndexOfLink, startIndexOfLink + link.first.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        if (color != 0) {
            spannableString.setSpan(
                ForegroundColorSpan(color),
                startIndexOfLink,
                startIndexOfLink + link.first.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }
    this.movementMethod =
        LinkMovementMethod.getInstance() // without LinkMovementMethod, link can not click
    this.setText(spannableString, TextView.BufferType.SPANNABLE)
}

fun Activity.adjustPan() {
    this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
}

fun Activity.adjustNothing() {
    this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
}

fun String.getSpanableDynamicString(
    context: Context,
    dynamicString: String,
    spannableColor: Int = -1
): SpannableStringBuilder? {
    return try {
        val formatedString = this.format(dynamicString)
        val fcs = ForegroundColorSpan(ContextCompat.getColor(context, spannableColor))
        val separated = formatedString.split(dynamicString)
        val str = SpannableStringBuilder(this)

        str.setSpan(
            fcs,
            separated[0].length,
            separated[0].length + dynamicString.length + separated[1].length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        str
    } catch (e: Exception) {
        return null
    }
}

fun RecyclerView?.prevent() {
    this?.adapter?.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT
}

fun Context.openDialer(number: String) {
    val intent = Intent(Intent.ACTION_DIAL)
    intent.data = Uri.parse("tel:${number}")
    startActivity(intent)
}

fun View.fadeInOut(viewModelScope: CoroutineScope) {
    viewModelScope.launch {
        delay(200)
        val alphaAnimation = AlphaAnimation(0.0f, 1.0f)
        alphaAnimation.duration = 1000
        alphaAnimation.repeatCount = 1
        alphaAnimation.repeatMode = Animation.REVERSE
        visibility = View.VISIBLE
        startAnimation(alphaAnimation)
        delay(1500)
        visibility = View.INVISIBLE
    }
}
