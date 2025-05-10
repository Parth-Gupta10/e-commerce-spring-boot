package com.ecommerce.web.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @OneToOne(mappedBy = "payment",
            cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE},
            orphanRemoval = true)
    private Order order;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;

    @Column(name = "payment_status")
    private String paymentStatus;

    @Column(name = "payment_gateway_response")
    private String paymentGatewayResponse;

    @Column(name = "payment_gateway_id")
    private String paymentGatewayId;

    public Payment(String paymentMethod, String paymentStatus, String paymentGatewayResponse, String paymentGatewayId) {
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.paymentGatewayResponse = paymentGatewayResponse;
        this.paymentGatewayId = paymentGatewayId;
    }
}
