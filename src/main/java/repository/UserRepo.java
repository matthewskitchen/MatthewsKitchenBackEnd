package repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import dbmodel.users;

public interface UserRepo extends JpaRepository<users, String> {
    Optional<users> findByEmail(String email);
}
