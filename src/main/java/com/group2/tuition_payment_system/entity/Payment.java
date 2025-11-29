package com.group2.tuition_payment_system.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_no", nullable = false)
    private Student student;

    private String term;

    private Double amount;

    private LocalDateTime paymentDate = LocalDateTime.now();
}