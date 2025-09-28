package Group5_pizza.Pizza_GoGo.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "Roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RoleId")
    private Integer roleId;

    @Column(name = "RoleName", nullable = false, length = 50)
    private String roleName;

    @OneToMany(mappedBy = "role")
    private List<Account> accounts;
}