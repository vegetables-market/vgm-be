package com.example.myapp.controller.user.address

import com.example.myapp.controller.common.getAppUser
import com.example.myapp.dto.user.address.UpsertUserAddressRequest
import com.example.myapp.dto.user.address.UserAddressResponse
import com.example.myapp.exception.AppException
import com.example.myapp.exception.ErrorCode
import com.example.myapp.service.auth.session.AppCookieService
import com.example.myapp.service.auth.session.SessionService
import com.example.myapp.service.user.address.UserAddressService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/user/addresses")
class UserAddressController(
    private val userAddressService: UserAddressService,
    private val sessionService: SessionService,
    private val appCookieService: AppCookieService,
) {
    @GetMapping
    fun getAddresses(
        servletRequest: HttpServletRequest,
    ): ResponseEntity<Map<String, List<UserAddressResponse>>> {
        val userId = getUserId(servletRequest)
        val addresses = userAddressService.getAddresses(userId)
        return ResponseEntity.ok(mapOf("addresses" to addresses))
    }

    @PostMapping
    fun createAddress(
        @RequestBody request: UpsertUserAddressRequest,
        servletRequest: HttpServletRequest,
    ): ResponseEntity<Map<String, Any>> {
        val userId = getUserId(servletRequest)
        val address = userAddressService.createAddress(userId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(
            mapOf(
                "success" to true,
                "address" to address,
            ),
        )
    }

    @PutMapping("/{addressId}")
    fun updateAddress(
        @PathVariable addressId: Int,
        @RequestBody request: UpsertUserAddressRequest,
        servletRequest: HttpServletRequest,
    ): ResponseEntity<Map<String, Any>> {
        val userId = getUserId(servletRequest)
        val address = userAddressService.updateAddress(userId, addressId, request)
        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "address" to address,
            ),
        )
    }

    @PutMapping("/{addressId}/default")
    fun setDefaultAddress(
        @PathVariable addressId: Int,
        servletRequest: HttpServletRequest,
    ): ResponseEntity<Map<String, Any>> {
        val userId = getUserId(servletRequest)
        val address = userAddressService.setDefaultAddress(userId, addressId)
        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "address" to address,
            ),
        )
    }

    @DeleteMapping("/{addressId}")
    fun deleteAddress(
        @PathVariable addressId: Int,
        servletRequest: HttpServletRequest,
    ): ResponseEntity<Map<String, Any>> {
        val userId = getUserId(servletRequest)
        userAddressService.deleteAddress(userId, addressId)
        return ResponseEntity.ok(mapOf("success" to true))
    }

    private fun getUserId(servletRequest: HttpServletRequest): Int {
        val (userId, _) = servletRequest.getAppUser(appCookieService, sessionService)
        return userId ?: throw AppException(ErrorCode.AUTH_REQUIRED, "Login required")
    }
}
