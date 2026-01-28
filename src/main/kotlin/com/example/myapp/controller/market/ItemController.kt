import com.example.myapp.dto.market.CreateItemRequest
import com.example.myapp.dto.market.ItemResponse
import com.example.myapp.entity.auth.UserSession
import com.example.myapp.service.market.ItemService
import com.example.myapp.service.market.MediaService
import com.example.myapp.service.market.UploadTokenResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/market/items")
class ItemController(
    private val itemService: ItemService,
    private val mediaService: MediaService
) {

    @PostMapping("/upload-token")
    fun getUploadToken(@AuthenticationPrincipal userSession: UserSession): UploadTokenResponse {
        return mediaService.generateUploadToken(userSession.userId, "USER")
    }

    @PostMapping
    fun createItem(
        @AuthenticationPrincipal userSession: UserSession,
        @RequestBody request: CreateItemRequest
    ): ResponseEntity<ItemResponse> {
        val response = itemService.createItem(userSession.userId, request)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/me")
    fun getMyItems(
        @AuthenticationPrincipal userSession: UserSession
    ): ResponseEntity<List<ItemResponse>> {
        val response = itemService.getMyItems(userSession.userId)
        return ResponseEntity.ok(response)
    }
}
