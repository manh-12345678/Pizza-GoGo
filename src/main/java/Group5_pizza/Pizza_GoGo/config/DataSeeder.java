package Group5_pizza.Pizza_GoGo.config;

import Group5_pizza.Pizza_GoGo.model.Role;
import Group5_pizza.Pizza_GoGo.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public DataSeeder(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) {
        insertRoleIfNotExists("ADMIN");
        insertRoleIfNotExists("STAFF");
        insertRoleIfNotExists("CUSTOMER");
    }

    private void insertRoleIfNotExists(String roleName) {
        if (!roleRepository.existsByRoleName(roleName)) {
            Role role = new Role();
            role.setRoleName(roleName);
            roleRepository.save(role);
        }
    }
}