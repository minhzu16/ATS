package fptu.sba301.ats.config;

import fptu.sba301.ats.entity.Department;
import fptu.sba301.ats.entity.User;
import fptu.sba301.ats.enums.Role;
import fptu.sba301.ats.repository.DepartmentRepository;
import fptu.sba301.ats.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Seeds initial departments and dev bootstrap user accounts.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
class UserDepartmentSeedHelper {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    // Reads OS env vars and backend/.env (imported as Spring properties); System.getenv only saw the former.
    private final Environment environment;

    void seedDepartments() {
        if (departmentRepository.count() > 0) {
            return;
        }

        log.info("Seeding Departments...");
        departmentRepository.save(Department.builder()
                .name("IT")
                .description("Information Technology Department")
                .build());

        departmentRepository.save(Department.builder()
                .name("HR")
                .description("Human Resources Department")
                .build());

        departmentRepository.save(Department.builder()
                .name("Sales")
                .description("Sales and Marketing Department")
                .build());

        log.info("Departments seeded.");
    }

    void seedUsers() {
        if (userRepository.count() > 0) {
            return;
        }

        log.info("Seeding Users...");
        Department itDept = getDepartmentByName("IT");
        Department hrDept = getDepartmentByName("HR");
        String adminEmail = Optional.ofNullable(environment.getProperty("BOOTSTRAP_ADMIN_EMAIL"))
                .filter(s -> !s.isBlank())
                .orElse("admin@ats.com");
        String devPassword = DatabaseSeeder.requireBootstrapPassword(
                environment.getProperty("BOOTSTRAP_ADMIN_PASSWORD"));
        String encodedPassword = passwordEncoder.encode(devPassword);

        userRepository.save(User.builder()
                .email(adminEmail)
                .passwordHash(encodedPassword)
                .fullName("System Administrator")
                .role(Role.SYSTEM_ADMIN)
                .department(itDept)
                .active(true)
                .build());

        userRepository.save(User.builder()
                .email("hrmanager@ats.com")
                .passwordHash(encodedPassword)
                .fullName("HR Manager")
                .role(Role.HR_MANAGER)
                .department(hrDept)
                .active(true)
                .build());

        userRepository.save(User.builder()
                .email("hr@ats.com")
                .passwordHash(encodedPassword)
                .fullName("Human Resources Staff")
                .role(Role.HR)
                .department(itDept)
                .active(true)
                .build());

        userRepository.save(User.builder()
                .email("interviewer@ats.com")
                .passwordHash(encodedPassword)
                .fullName("Technical Interviewer")
                .role(Role.INTERVIEWER)
                .department(itDept)
                .active(true)
                .build());

        log.info("Users seeded.");
    }

    Department getDepartmentByName(String name) {
        return departmentRepository.findAll().stream()
                .filter(department -> department.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    User getUserByEmail(String email) {
        Optional<User> userOptional = userRepository.findByEmailAndDeletedFalse(email);
        return userOptional.orElse(null);
    }
}
