package com.zstronics.ceibro.base.validator.rule

import android.widget.TextView
import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.zstronics.ceibro.R

import java.lang.reflect.InvocationTargetException

/**
 * Created by Muhammad Ibraheem
 */
@Keep
abstract class TypeRule(
    view: TextView?,
    value: FieldType?,
    errorMessage: String?,
    errorEnabled: Boolean
) : Rule<TextView?, TypeRule.FieldType?>(
    view,
    value,
    errorMessage,
    errorEnabled
) {
    enum class FieldType {
        Cpf(
            CpfTypeRule::class.java,
            R.string.error_message_cpf_validation
        ),
        FirstName(
            FirstNameRule::class.java, R.string.error_message_first_name_validation
        ),
        LastName(
            LastNameRule::class.java, R.string.error_message_sur_name_validation
        ),
        Email(
            EmailTypeRule::class.java, R.string.error_message_email_validation
        ),
        Phone(
            PhoneTypeRule::class.java, R.string.error_message_phone_validation
        ),
        Url(
            UrlTypeRule::class.java, R.string.error_message_url_validation
        ),
        PostalAddress(
            PostalAddressRule::class.java,
            R.string.error_message_credit_card_validation
        ),
        None;

        var mClass: Class<out TypeRule>? = null

        @JvmField
        @StringRes
        var errorMessageId = 0

        constructor(
            mClass: Class<out TypeRule>?,
            @StringRes errorMessageId: Int
        ) {
            this.mClass = mClass
            this.errorMessageId = errorMessageId
        }

        constructor()

        @Throws(
            NoSuchMethodException::class,
            IllegalAccessException::class,
            InvocationTargetException::class,
            InstantiationException::class
        )
        fun instantiate(view: TextView?, errorMessage: String?): TypeRule {
            if (this != None) {
                return mClass!!.getConstructor(
                    TextView::class.java,
                    String::class.java
                ).newInstance(view, errorMessage)
            }
            throw IllegalStateException("It's not possible to bind a none value type")
        }

        @Throws(
            NoSuchMethodException::class,
            IllegalAccessException::class,
            InvocationTargetException::class,
            InstantiationException::class
        )
        fun instantiate(
            view: TextView?,
            errorMessage: String?,
            errorEnabled: Boolean
        ): TypeRule {
            if (this != None) {
                return mClass?.getConstructor(
                    TextView::class.java,
                    String::class.java,
                    Boolean::class.javaPrimitiveType
                )?.newInstance(view, errorMessage, errorEnabled)!!
            }
            throw IllegalStateException("It's not possible to bind a none value type")
        }
    }
}