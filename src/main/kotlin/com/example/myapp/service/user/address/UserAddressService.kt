package com.example.myapp.service.user.address

import com.example.myapp.dto.user.address.UpsertUserAddressRequest
import com.example.myapp.dto.user.address.UserAddressResponse
import com.example.myapp.entity.user.address.UserAddress
import com.example.myapp.exception.AppException
import com.example.myapp.exception.ErrorCode
import com.example.myapp.repository.user.address.UserAddressRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class UserAddressService(
    private val userAddressRepository: UserAddressRepository,
) {
    fun getAddresses(userId: Int, addressTypeRaw: String): List<UserAddressResponse> {
        val addressType = normalizeAddressType(addressTypeRaw)
        return userAddressRepository
            .findByUserIdAndAddressTypeAndDeletedAtIsNullOrderByIsDefaultDescUpdatedAtDesc(userId, addressType)
            .map { it.toResponse() }
    }

    @Transactional
    fun createAddress(userId: Int, addressTypeRaw: String, request: UpsertUserAddressRequest): UserAddressResponse {
        val addressType = normalizeAddressType(addressTypeRaw)
        validate(request)
        if (addressType == ADDRESS_TYPE_SENDER) {
            val existingSenderAddresses = userAddressRepository
                .findByUserIdAndAddressTypeAndDeletedAtIsNullOrderByIsDefaultDescUpdatedAtDesc(
                    userId,
                    addressType,
                )
            if (existingSenderAddresses.isNotEmpty()) {
                throw AppException(
                    ErrorCode.INVALID_INPUT,
                    "Only one active sender address is allowed",
                )
            }
        }
        val address = userAddressRepository.save(
            UserAddress(
                userId = userId,
                name = request.name.trim(),
                nameKana = request.nameKana?.trim()?.ifBlank { null },
                postalCode = normalizePostalCode(request.postalCode),
                prefecture = request.prefecture.trim(),
                city = request.city.trim(),
                addressLine1 = request.addressLine1.trim(),
                addressLine2 = request.addressLine2?.trim()?.ifBlank { null },
                phoneNumber = request.phoneNumber?.trim()?.ifBlank { null },
                countryCode = request.countryCode?.trim()?.ifBlank { "JP" } ?: "JP",
                isDefault = if (request.isDefault) 1 else 0,
                addressType = addressType,
            ),
        )
        if (request.isDefault) {
            setDefaultAddress(userId, address.addressId, addressType)
        }
        return getAddressOrThrow(address.addressId, userId, addressType).toResponse()
    }

    @Transactional
    fun updateAddress(
        userId: Int,
        addressId: Int,
        addressTypeRaw: String,
        request: UpsertUserAddressRequest,
    ): UserAddressResponse {
        val addressType = normalizeAddressType(addressTypeRaw)
        validate(request)
        val address = getAddressOrThrow(addressId, userId, addressType)

        address.name = request.name.trim()
        address.nameKana = request.nameKana?.trim()?.ifBlank { null }
        address.postalCode = normalizePostalCode(request.postalCode)
        address.prefecture = request.prefecture.trim()
        address.city = request.city.trim()
        address.addressLine1 = request.addressLine1.trim()
        address.addressLine2 = request.addressLine2?.trim()?.ifBlank { null }
        address.phoneNumber = request.phoneNumber?.trim()?.ifBlank { null }
        address.countryCode = request.countryCode?.trim()?.ifBlank { "JP" } ?: "JP"
        address.isDefault = if (request.isDefault) 1 else 0

        userAddressRepository.save(address)
        if (request.isDefault) {
            setDefaultAddress(userId, addressId, addressType)
        }
        return getAddressOrThrow(addressId, userId, addressType).toResponse()
    }

    @Transactional
    fun deleteAddress(userId: Int, addressId: Int, addressTypeRaw: String) {
        val addressType = normalizeAddressType(addressTypeRaw)
        val address = getAddressOrThrow(addressId, userId, addressType)
        address.deletedAt = LocalDateTime.now()
        address.isDefault = 0
        userAddressRepository.save(address)
    }

    @Transactional
    fun setDefaultAddress(userId: Int, addressId: Int, addressTypeRaw: String): UserAddressResponse {
        val addressType = normalizeAddressType(addressTypeRaw)
        val target = getAddressOrThrow(addressId, userId, addressType)
        val addresses = userAddressRepository
            .findByUserIdAndAddressTypeAndDeletedAtIsNullOrderByIsDefaultDescUpdatedAtDesc(userId, addressType)

        addresses.forEach { address ->
            address.isDefault = if (address.addressId == target.addressId) 1 else 0
        }
        userAddressRepository.saveAll(addresses)

        return getAddressOrThrow(addressId, userId, addressType).toResponse()
    }

    private fun getAddressOrThrow(addressId: Int, userId: Int, addressType: String): UserAddress =
        userAddressRepository.findByAddressIdAndUserIdAndAddressTypeAndDeletedAtIsNull(addressId, userId, addressType)
            ?: throw AppException(ErrorCode.RESOURCE_NOT_FOUND, "Address not found")

    private fun validate(request: UpsertUserAddressRequest) {
        if (request.name.isBlank()) {
            throw AppException(ErrorCode.INVALID_INPUT, "name is required")
        }
        if (request.postalCode.isBlank() || !POSTAL_CODE_REGEX.matches(request.postalCode.trim())) {
            throw AppException(ErrorCode.INVALID_INPUT, "postalCode is invalid")
        }
        if (request.prefecture.isBlank()) {
            throw AppException(ErrorCode.INVALID_INPUT, "prefecture is required")
        }
        if (request.city.isBlank()) {
            throw AppException(ErrorCode.INVALID_INPUT, "city is required")
        }
        if (request.addressLine1.isBlank()) {
            throw AppException(ErrorCode.INVALID_INPUT, "addressLine1 is required")
        }
        if ((request.countryCode ?: "JP").trim().length != 2) {
            throw AppException(ErrorCode.INVALID_INPUT, "countryCode must be 2 characters")
        }
    }

    private fun normalizePostalCode(postalCode: String): String {
        val cleaned = postalCode.trim().replace("-", "")
        return cleaned.replace(POSTAL_CODE_CAPTURE_REGEX, "$1-$2")
    }

    private fun normalizeAddressType(addressTypeRaw: String): String {
        val normalized = addressTypeRaw.trim().uppercase()
        return when (normalized) {
            ADDRESS_TYPE_DELIVERY, ADDRESS_TYPE_SENDER -> normalized
            else -> throw AppException(ErrorCode.INVALID_INPUT, "addressType is invalid")
        }
    }

    private fun UserAddress.toResponse(): UserAddressResponse =
        UserAddressResponse(
            addressId = addressId,
            name = name,
            nameKana = nameKana,
            postalCode = postalCode,
            prefecture = prefecture,
            city = city,
            addressLine1 = addressLine1,
            addressLine2 = addressLine2,
            phoneNumber = phoneNumber,
            countryCode = countryCode,
            isDefault = isDefault.toInt() == 1,
            addressType = addressType,
        )

    companion object {
        private val POSTAL_CODE_REGEX = Regex("^\\d{3}-?\\d{4}$")
        private val POSTAL_CODE_CAPTURE_REGEX = Regex("^(\\d{3})(\\d{4})$")
        const val ADDRESS_TYPE_DELIVERY = "DELIVERY"
        const val ADDRESS_TYPE_SENDER = "SENDER"
    }
}
