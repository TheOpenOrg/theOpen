package org.theopen.backend.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.theopen.backend.model.Server;

public interface ServerRepository extends JpaRepository<Server, Long> {
}