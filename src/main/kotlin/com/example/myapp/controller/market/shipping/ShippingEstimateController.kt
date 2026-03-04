package com.example.myapp.controller.market.shipping

import com.example.myapp.controller.market.getMarketUser
import com.example.myapp.dto.market.shipping.ShippingEstimateRequest
import com.example.myapp.dto.market.shipping.ShippingEstimateResponse
import com.example.myapp.exception.AppException
import com.example.myapp.exception.ErrorCode
import com.example.myapp.service.auth.session.AppCookieService
import com.example.myapp.service.auth.session.SessionService
import com.example.myapp.service.market.shipping.ShippingEstimateService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/market/shipping")
class ShippingEstimateController(
    private val shippingEstimateService: ShippingEstimateService,
    private val sessionService: SessionService,
    private val appCookieService: AppCookieService,
) {
    @PostMapping("/estimate")
    fun estimate(
        @RequestBody request: ShippingEstimateRequest,
        servletRequest: HttpServletRequest,
    ): ResponseEntity<ShippingEstimateResponse> {
        val (userId, _) = servletRequest.getMarketUser(appCookieService, sessionService)
        if (userId == null) {
            throw AppException(ErrorCode.AUTH_REQUIRED, "Login required")
        }
        return ResponseEntity.ok(shippingEstimateService.estimate(userId, request))
    }
}
