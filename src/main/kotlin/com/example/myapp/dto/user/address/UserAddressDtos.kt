package com.example.myapp.dto.user.address

data class UpsertUserAddressRequest(
    val name: String,
    val nameKana: String? = null,
    val postalCode: String,
    val prefecture: String,
    val city: String,
    val addressLine1: String,
    val addressLine2: String? = null,
    val phoneNumber: String? = null,
    val countryCode: String? = "JP",
    val isDefault: Boolean = false,
)

data class UserAddressResponse(
    val addressId: Int,
    val name: String?,
    val nameKana: String?,
    val postalCode: String,
    val prefecture: String,
    val city: String,
    val addressLine1: String,
    val addressLine2: String?,
    val phoneNumber: String?,
    val countryCode: String,
    val isDefault: Boolean,
)
