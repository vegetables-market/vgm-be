package com.example.myapp.dto.user.address

import com.fasterxml.jackson.annotation.JsonAlias

data class UpsertUserAddressRequest(
    @JsonAlias("name")
    val name: String,
    @JsonAlias("name_kana", "nameKana")
    val nameKana: String? = null,
    @JsonAlias("postal_code", "postalCode")
    val postalCode: String,
    @JsonAlias("prefecture")
    val prefecture: String,
    @JsonAlias("city")
    val city: String,
    @JsonAlias("address_line1", "addressLine1")
    val addressLine1: String,
    @JsonAlias("address_line2", "addressLine2")
    val addressLine2: String? = null,
    @JsonAlias("phone_number", "phoneNumber")
    val phoneNumber: String? = null,
    @JsonAlias("country_code", "countryCode")
    val countryCode: String? = "JP",
    @JsonAlias("is_default", "isDefault")
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
