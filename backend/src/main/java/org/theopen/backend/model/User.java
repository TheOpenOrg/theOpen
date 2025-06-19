package org.theopen.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @Column(name = "tg_id")
    private Long tgId;
    @Column
    private String name;
}
