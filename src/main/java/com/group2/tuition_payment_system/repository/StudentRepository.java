package com.group2.tuition_payment_system.repository;

import com.group2.tuition_payment_system.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, String> {

    Optional<Student> findByStudentNo(String studentNo);
}