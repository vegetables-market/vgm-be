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
    fun getAddresses(userId: Int): List<UserAddressResponse> =
        userAddressRepository
            .findByUserIdAndDeletedAtIsNullOrderByIsDefaultDescUpdatedAtDesc(userId)
            .map { it.toResponse() }

    @Transactional
    fun createAddress(userId: Int, request: UpsertUserAddressRequest): UserAddressResponse {
        validate(request)
        val address = userAddressRepository.save(
            UserAddress(
                userId = userId,
                postalCode = normalizePostalCode(request.postalCode),
                prefecture = request.prefecture.trim(),
                city = request.city.trim(),
                addressLine1 = request.addressLine1.trim(),
                addressLine2 = request.addressLine2?.trim()?.ifBlank { null },
                countryCode = request.countryCode?.trim()?.ifBlank { "JP" } ?: "JP",
                isDefault = if (request.isDefault) 1 else 0,
            ),
        )
        if (request.isDefault) {
            setDefaultAddress(userId, address.addressId)
        }
        return getAddressOrThrow(address.addressId, userId).toResponse()
    }

    @Transactional
    fun updateAddress(userId: Int, addressId: Int, request: UpsertUserAddressRequest): UserAddressResponse {
        validate(request)
        val address = getAddressOrThrow(addressId, userId)

        address.postalCode = normalizePostalCode(request.postalCode)
        address.prefecture = request.prefecture.trim()
        address.city = request.city.trim()
        address.addressLine1 = request.addressLine1.trim()
        address.addressLine2 = request.addressLine2?.trim()?.ifBlank { null }
        address.countryCode = request.countryCode?.trim()?.ifBlank { "JP" } ?: "JP"
        address.isDefault = if (request.isDefault) 1 else 0

        userAddressRepository.save(address)
        if (request.isDefault) {
            setDefaultAddress(userId, addressId)
        }
        return getAddressOrThrow(addressId, userId).toResponse()
    }

    @Transactional
    fun deleteAddress(userId: Int, addressId: Int) {
        val address = getAddressOrThrow(addressId, userId)
        address.deletedAt = LocalDateTime.now()
        address.isDefault = 0
        userAddressRepository.save(address)
    }

    @Transactional
    fun setDefaultAddress(userId: Int, addressId: Int): UserAddressResponse {
        val target = getAddressOrThrow(addressId, userId)
        val addresses = userAddressRepository.findByUserIdAndDeletedAtIsNullOrderByIsDefaultDescUpdatedAtDesc(userId)

        addresses.forEach { address ->
            address.isDefault = if (address.addressId == target.addressId) 1 else 0
        }
        userAddressRepository.saveAll(addresses)

        return getAddressOrThrow(addressId, userId).toResponse()
    }

    private fun getAddressOrThrow(addressId: Int, userId: Int): UserAddress =
        userAddressRepository.findByAddressIdAndUserIdAndDeletedAtIsNull(addressId, userId)
            ?: throw AppException(ErrorCode.RESOURCE_NOT_FOUND, "Address not found")

    private fun validate(request: UpsertUserAddressRequest) {
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

    private fun UserAddress.toResponse(): UserAddressResponse =
        UserAddressResponse(
            addressId = addressId,
            postalCode = postalCode,
            prefecture = prefecture,
            city = city,
            addressLine1 = addressLine1,
            addressLine2 = addressLine2,
            countryCode = countryCode,
            isDefault = isDefault.toInt() == 1,
        )

    companion object {
        private val POSTAL_CODE_REGEX = Regex("^\\d{3}-?\\d{4}$")
        private val POSTAL_CODE_CAPTURE_REGEX = Regex("^(\\d{3})(\\d{4})$")
    }
}
