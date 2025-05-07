package com.unluckygbs.recipebingo.data.dataclass

data class UserProfile(
    val username: String = "",
    val email: String = "",
    val profileImageBase64: String? = null
)