package org.theopen.backend.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.theopen.backend.model.Server;

import java.util.List;

public interface ServerRepository extends JpaRepository<Server, Long> {
    List<Server> findByCountryEntityId(Long countryId);
}

