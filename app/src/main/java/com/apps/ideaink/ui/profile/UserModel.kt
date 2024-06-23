package com.example.PPAB10

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserModel(
    var img: String? = null,
    var name: String? = null,
    var email: String? = null,
    var quote: String? = null,
    var theme: Boolean? = null
): Parcelable
