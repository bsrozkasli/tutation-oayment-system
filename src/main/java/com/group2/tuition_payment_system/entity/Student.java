package com.group2.tuition_payment_system.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entity class representing a Student in the tuition payment system.
 * Stores basic student information including student number and name.
 *
 * @author Group 2
 */
@Entity
@Data
@Table(name = "students")
public class Student {

    @Id
    @Column(length = 20)
    private String studentNo;
    private String name;

}