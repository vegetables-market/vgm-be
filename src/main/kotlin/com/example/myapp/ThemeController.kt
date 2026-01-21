@Entity
@Table(name = "usersetting")
public class UserSetting {

    @Id
    private Long f_user_id;

    private String theme; // "dark" or "light"

    // getter/setter
}