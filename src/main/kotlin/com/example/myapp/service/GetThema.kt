@GetMapping("/api/users/{id}/theme")
fun getTheme(@PathVariable id: Long): Map<String, Any> {
    val theme = userService.getTheme(id)
    return mapOf("theme" to theme)
}