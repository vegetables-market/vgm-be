package com.example.myapp.repository.user.address

import com.example.myapp.entity.user.address.UserAddress
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserAddressRepository : JpaRepository<UserAddress, Int> {
    fun findByUserIdAndDeletedAtIsNullOrderByIsDefaultDescUpdatedAtDesc(userId: Int): List<UserAddress>
    fun findByAddressIdAndUserIdAndDeletedAtIsNull(addressId: Int, userId: Int): UserAddress?
    fun findByUserIdAndAddressTypeAndDeletedAtIsNullOrderByIsDefaultDescUpdatedAtDesc(
        userId: Int,
        addressType: String,
    ): List<UserAddress>
    fun findByAddressIdAndUserIdAndAddressTypeAndDeletedAtIsNull(
        addressId: Int,
        userId: Int,
        addressType: String,
    ): UserAddress?
}
