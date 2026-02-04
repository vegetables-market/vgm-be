package com.example.myapp.controller.app

import com.example.myapp.repository.common.AppVersionRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class AppVersionResponse(
    val version: String
)

@RestController
@RequestMapping("/v1")
class AppVersionsController(
    val appVersionRepository: AppVersionRepository
) {
    @GetMapping("/version")
    fun getVersion(): AppVersionResponse {
        val latestVersion = appVersionRepository.findTopByOrderByVersionCodeDesc()
        val versionName = latestVersion?.versionName ?: "1.0.2"
        return AppVersionResponse(version = versionName)
    }
}