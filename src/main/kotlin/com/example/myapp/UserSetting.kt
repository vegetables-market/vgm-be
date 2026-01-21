@Entity
@Table(name = "usersetting")
data class UserSetting(
    @Id
    val userId: Long,
    val theme: String
)