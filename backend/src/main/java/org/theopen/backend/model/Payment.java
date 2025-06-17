package org.theopen.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "payment")
@Data
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "paymentId")
    private Long paymentId;
    @Column(name = "terminal_key")
    private String terminalKey;
    @Column(name = "amount")
    private Long amount;
    @Column(name = "order_id")
    private String orderId;
    @Column(name = "description")
    private String description;
    @Column(name = "token")
    private String token;
    @Column(name = "url")
    private String url;
    @Column(name = "status")
    private String status;
}

