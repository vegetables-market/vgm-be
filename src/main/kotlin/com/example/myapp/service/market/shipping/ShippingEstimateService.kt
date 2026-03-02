package com.example.myapp.service.market.shipping

import com.example.myapp.dto.market.shipping.ShippingEstimateRequest
import com.example.myapp.dto.market.shipping.ShippingEstimateResponse
import com.example.myapp.exception.AppException
import com.example.myapp.exception.ErrorCode
import com.example.myapp.repository.market.item.ItemRepository
import com.example.myapp.repository.user.address.UserAddressRepository
import org.springframework.stereotype.Service
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@Service
class ShippingEstimateService(
    private val itemRepository: ItemRepository,
    private val userAddressRepository: UserAddressRepository,
    private val prefectureCentroidService: PrefectureCentroidService,
) {
    fun estimate(userId: Int, request: ShippingEstimateRequest): ShippingEstimateResponse {
        val item = itemRepository.findByDisplayId(request.itemId)
            ?: throw AppException(ErrorCode.RESOURCE_NOT_FOUND, "Item not found")
        val originPrefectureId = item.shippingOriginArea
            ?: throw AppException(ErrorCode.INVALID_INPUT, "Item shipping origin is not set")
        val origin = prefectureCentroidService.findByPrefectureId(originPrefectureId)
            ?: throw AppException(ErrorCode.INVALID_INPUT, "Unsupported shipping origin area")

        val destinationAddress = userAddressRepository.findByAddressIdAndUserIdAndDeletedAtIsNull(
            request.addressId,
            userId,
        ) ?: throw AppException(ErrorCode.RESOURCE_NOT_FOUND, "Address not found")

        val destination = prefectureCentroidService.findByPrefectureName(destinationAddress.prefecture)
            ?: throw AppException(ErrorCode.INVALID_INPUT, "Unsupported destination prefecture")

        val distanceKm = haversineKm(origin, destination).roundToInt()
        val estimatedDays = when {
            distanceKm < 300 -> 1
            distanceKm < 800 -> 2
            else -> 3
        }
        val distanceBand = when {
            distanceKm < 300 -> "0_300"
            distanceKm < 800 -> "300_800"
            else -> "800_plus"
        }

        return ShippingEstimateResponse(
            itemId = request.itemId,
            addressId = request.addressId,
            distanceKm = distanceKm,
            estimatedDays = estimatedDays,
            distanceBand = distanceBand,
            estimatedLabel = "お届け目安: ${estimatedDays}日",
        )
    }

    private fun haversineKm(origin: Coordinate, destination: Coordinate): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(destination.lat - origin.lat)
        val dLng = Math.toRadians(destination.lng - origin.lng)
        val lat1 = Math.toRadians(origin.lat)
        val lat2 = Math.toRadians(destination.lat)

        val a = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLng / 2).pow(2)
        val c = 2 * asin(sqrt(a))
        return earthRadiusKm * c
    }
}
