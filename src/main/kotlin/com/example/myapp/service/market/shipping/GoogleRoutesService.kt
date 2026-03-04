package com.example.myapp.service.market.shipping

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import kotlin.math.roundToInt

data class GoogleRouteMatrixEstimate(
    val distanceKm: Double,
    val durationSeconds: Int,
)

@Service
class GoogleRoutesService(
    @Value("\${google.maps.routes-api-key:\${google.maps.api-key:}}")
    private val routesApiKey: String,
    @Value("\${google.maps.routes-api-url:https://routes.googleapis.com/distanceMatrix/v2:computeRouteMatrix}")
    private val routesApiUrl: String,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val webClient = WebClient.builder().build()

    fun computeRouteMatrix(origin: Coordinate, destination: Coordinate): GoogleRouteMatrixEstimate? {
        if (routesApiKey.isBlank()) {
            return null
        }

        val requestBody = mapOf(
            "origins" to listOf(
                mapOf(
                    "waypoint" to mapOf(
                        "location" to mapOf(
                            "latLng" to mapOf(
                                "latitude" to origin.lat,
                                "longitude" to origin.lng,
                            ),
                        ),
                    ),
                ),
            ),
            "destinations" to listOf(
                mapOf(
                    "waypoint" to mapOf(
                        "location" to mapOf(
                            "latLng" to mapOf(
                                "latitude" to destination.lat,
                                "longitude" to destination.lng,
                            ),
                        ),
                    ),
                ),
            ),
            "travelMode" to "DRIVE",
            "routingPreference" to "TRAFFIC_UNAWARE",
            "languageCode" to "ja",
            "units" to "METRIC",
        )

        return try {
            val elements = webClient.post()
                .uri(routesApiUrl)
                .header("X-Goog-Api-Key", routesApiKey)
                .header(
                    "X-Goog-FieldMask",
                    "originIndex,destinationIndex,distanceMeters,duration,status,condition",
                )
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(RouteMatrixElement::class.java)
                .collectList()
                .block()
                .orEmpty()

            val element = elements.firstOrNull { it.originIndex == 0 && it.destinationIndex == 0 }
                ?: return null

            val distanceMeters = element.distanceMeters ?: return null
            val durationSeconds = parseDurationSeconds(element.duration) ?: return null

            GoogleRouteMatrixEstimate(
                distanceKm = distanceMeters / 1000.0,
                durationSeconds = durationSeconds,
            )
        } catch (exception: Exception) {
            logger.warn("Failed to call Google Routes API ComputeRouteMatrix: {}", exception.message)
            null
        }
    }

    private fun parseDurationSeconds(duration: String?): Int? {
        if (duration.isNullOrBlank()) return null
        return duration.removeSuffix("s").toDoubleOrNull()?.roundToInt()
    }
}

data class RouteMatrixElement(
    val originIndex: Int? = null,
    val destinationIndex: Int? = null,
    val distanceMeters: Double? = null,
    val duration: String? = null,
)
