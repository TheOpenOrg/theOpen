package org.theopen.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "config")
@Data
public class Config {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
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
