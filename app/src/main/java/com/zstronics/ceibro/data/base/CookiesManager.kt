package com.zstronics.ceibro.data.base

import com.zstronics.ceibro.data.repos.auth.login.Tokens

object CookiesManager {
    var jwtToken: String? = null
    var isLoggedIn: Boolean = false
    var tokens: Tokens? = null
}