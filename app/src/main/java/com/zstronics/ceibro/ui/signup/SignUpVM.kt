package com.zstronics.ceibro.ui.signup

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.onesignal.OneSignal
import com.zstronics.ceibro.CeibroApplication
import com.zstronics.ceibro.base.KEY_Firebase_Token
import com.zstronics.ceibro.base.validator.IValidator
import com.zstronics.ceibro.base.validator.Validator
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.auth.IAuthRepository
import com.zstronics.ceibro.data.repos.auth.signup.SignUpRequest
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SignUpVM @Inject constructor(
    override val viewState: SignUpState,
    override var validator: Validator?,
    private val repository: IAuthRepository,
    val sessionManager: SessionManager
) : HiltBaseViewModel<ISignUp.State>(), ISignUp.ViewModel, IValidator {


    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)

        with(viewState) {
            phoneNumber.value = bundle?.getString("phoneNumber")
            phoneCode.value = bundle?.getString("phoneCode")
        }
//        doSignUp(viewState.firstName.value.toString(), viewState.surname.value.toString(), viewState.email.value.toString(),
//            viewState.password.value.toString(), viewState.confirmPassword.value.toString())
    }

    override fun doSignUp(
        context: Context,
        firstName: String,
        surname: String,
        email: String,
        companyName: String,
        jobTitle: String,
        password: String,
        onSignedUp: () -> Unit
    ) {
        val deviceInfo = StringBuilder()
        val manufacturer = Build.MANUFACTURER
        deviceInfo.append("$manufacturer ")
        val model = Build.MODEL
        deviceInfo.append("$model")

        val androidId =  Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        println("DeviceInfo: ${deviceInfo.toString()} -> android ID: $androidId")
        var firebaseToken = ""
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener { token: String ->
            if (!TextUtils.isEmpty(token)) {
                Log.d("FirebaseToken-SignUp", "retrieve token successful : $token")
                firebaseToken = token
                sessionManager.saveStringValue(KEY_Firebase_Token, token)
            } else {
                Log.w("FirebaseToken-SignUp", "token should not be null...")
            }
        }.addOnFailureListener { e: Exception? -> }.addOnCanceledListener {}
            .addOnCompleteListener { task: Task<String> ->
                if (task.isSuccessful) {
                    Log.v(
                        "FirebaseToken-SignUp",
                        "This is the token : " + task.result
                    )
                    firebaseToken = task.result
                    sessionManager.saveStringValue(KEY_Firebase_Token, task.result)
                } else {
                    Log.w("FirebaseToken-SignUp", "token should not be null...addOnCompleteListener")
                }
            }

        val request = SignUpRequest(
            firstName = firstName,
            surName = surname,
            email = email,
            companyName = companyName,
            jobTitle = jobTitle,
            password = password
        )
        launch {
            loading(true)
            when (val response =
                repository.signup(viewState.phoneNumber.value.toString(), request)) {

                is ApiResponse.Success -> {
                    val secureUUID = UUID.randomUUID()
                    CeibroApplication.CookiesManager.deviceType = deviceInfo.toString()
                    CeibroApplication.CookiesManager.secureUUID = secureUUID.toString()
                    CeibroApplication.CookiesManager.androidId = androidId
                    CeibroApplication.CookiesManager.firebaseToken = firebaseToken
                    sessionManager.startUserSession(
                        response.data.user,
                        response.data.tokens,
                        "",
                        true,
                        secureUUID.toString(),
                        deviceInfo.toString(),
                        androidId,
                        firebaseToken
                    )
                    OneSignal.setExternalUserId(response.data.user.id)
                    OneSignal.disablePush(false)        //Running setSubscription() operation inside this method (a hack)
                    OneSignal.pauseInAppMessages(false)
                    if (sessionManager.getUpdatedAtTimeStamp().isEmpty()) {
                        sessionManager.saveUpdatedAtTimeStamp(response.data.user.createdAt)
                    }
                    loading(false, "Profile setup complete")
                    onSignedUp.invoke()
                }

                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                }
            }
        }
    }
}