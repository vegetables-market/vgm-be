@Service
class UserSettingService(
    private val repo: UserSettingRepository
) {
    fun getTheme(userId: Long): String =
        repo.findById(userId).map { it.theme }.orElse("light")
}