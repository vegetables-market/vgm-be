package com.example.myapp

import com.example.myapp.entity.common.AppVersion
import com.example.myapp.repository.AppVersionRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

data class AppVersionResponse(
    val version: String
)

@RestController
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