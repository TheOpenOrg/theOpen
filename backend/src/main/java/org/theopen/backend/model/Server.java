package org.theopen.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "servers")
@Data
public class Server {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String name;
    @Column
    private String country;
    @Column(name = "api_token")
    private String apiToken;
    @Column(name = "api_url")
    private String apiUrl;
}
