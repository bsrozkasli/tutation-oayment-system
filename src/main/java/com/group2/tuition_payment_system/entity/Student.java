package com.group2.tuition_payment_system.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "students")
public class Student {

    @Id
    @Column(length = 20)
    private String studentNo;
    private String name;

}