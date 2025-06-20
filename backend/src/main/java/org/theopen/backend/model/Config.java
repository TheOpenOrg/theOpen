package org.theopen.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "config")
@Data
public class Config {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "BINARY(16)")
    private UUID id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "server_id")
    private Server server;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "uid", referencedColumnName = "tg_id")
    private User user;
    @Column(name="month_amount")
    private Integer monthAmount;
    @Column(name="buy_time")
    private LocalDateTime buyTime;
    @Column(name="is_active")
    private Boolean isActive;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "payment_id")
    private Payment payment;
    @Column(name="is_trial")
    private Boolean isTrial;
    @Column(name = "name")
    private String name;
}
