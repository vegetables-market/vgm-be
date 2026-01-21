@RestController
@RequestMapping("/api")
class ThemeController(
    private val userSettingService: UserSettingService
) {
    @GetMapping("/theme/{userId}")
    fun getTheme(@PathVariable userId: Long): Map<String, String> =
        mapOf("theme" to userSettingService.getTheme(userId))
}