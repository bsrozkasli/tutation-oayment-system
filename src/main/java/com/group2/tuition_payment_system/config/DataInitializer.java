package com.group2.tuition_payment_system.config;

import com.group2.tuition_payment_system.entity.Student;
import com.group2.tuition_payment_system.repository.StudentRepository;
import com.group2.tuition_payment_system.service.TuitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Data initializer component that runs on application startup.
 * Initializes the database with sample student and tuition data if the database is empty.
 *
 * @author Group 2
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final StudentRepository studentRepository;
    private final TuitionService tuitionService;

    @Override
    public void run(String... args) throws Exception {
        if (studentRepository.count() == 0) {
            Student s1 = new Student();
            s1.setStudentNo("2023001");
            s1.setName("Ali Veli");
            studentRepository.save(s1);

            Student s2 = new Student();
            s2.setStudentNo("2023002");
            s2.setName("Ayşe Yılmaz");
            studentRepository.save(s2);

            tuitionService.addTuition("2023001", "2024-FALL", 15000.0);
            tuitionService.addTuition("2023002", "2024-FALL", 20000.0);
        }
    }
}