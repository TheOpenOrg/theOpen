package org.theopen.backend.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.theopen.backend.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
}

