@RestController
@RequestMapping("/api/items")
class SearchController(
    private val itemSearch: ItemSearch
) {

    @GetMapping("/search")
    fun search(
        @RequestParam keyword: String
    ): List<ItemSearchResult> {
        val synonyms = listOf("関連語1", "関連語2")
        return itemSearch.search(keyword, synonyms)
    }
}