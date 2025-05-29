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
    @JoinColumn(name = "uid")
    private User user;
    @Column(name="month_amount")
    private Integer monthAmount;
    @Column(name="buy_time")
    private LocalDateTime buyTime;
    @Column(name="is_active")
    private Boolean isActive;
    @Column(name="payment_status")
    private String paymentStatus;
    @Column(name="payment_link")
    private String paymentLink;
    @Column(name="is_trial")
    private Boolean isTrial;
    @Column(name="extra_config")
    private String extraConfig;
}
