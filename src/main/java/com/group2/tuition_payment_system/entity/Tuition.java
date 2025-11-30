package com.group2.tuition_payment_system.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entity class representing a Tuition record.
 * Stores tuition fee information for a specific student and term,
 * including the total amount and remaining balance.
 *
 * @author Group 2
 */
@Entity
@Data
@Table(name = "tuitions")
public class Tuition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_no", nullable = false)
    private Student student;

    private String term;

    private Double amount;

    private Double balance;
}