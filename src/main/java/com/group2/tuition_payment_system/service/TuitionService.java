package com.group2.tuition_payment_system.service;

import com.group2.tuition_payment_system.dto.response.TuitionStatusResponse;
import com.group2.tuition_payment_system.entity.Student;
import com.group2.tuition_payment_system.entity.Tuition;
import com.group2.tuition_payment_system.repository.StudentRepository;
import com.group2.tuition_payment_system.repository.TuitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TuitionService {

    private final TuitionRepository tuitionRepository;
    private final StudentRepository studentRepository;

    public TuitionStatusResponse queryTuition(String studentNo) {
        Double totalTuition = tuitionRepository.sumAmountByStudentNo(studentNo);
        Double totalPaid = tuitionRepository.sumPaidByStudentNo(studentNo);

        if (totalTuition == null) totalTuition = 0.0;
        if (totalPaid == null) totalPaid = 0.0;

        return TuitionStatusResponse.builder()
                .studentNo(studentNo)
                .totalTuitionAmount(totalTuition)
                .currentBalance(totalTuition - totalPaid)
                .build();
    }

    public void addTuition(String studentNo, String term, Double amount) {
        Optional<Student> studentOpt = studentRepository.findByStudentNo(studentNo);
        if (studentOpt.isEmpty()) {
            throw new RuntimeException("Student not found: " + studentNo);
        }

        Tuition tuition = new Tuition();
        tuition.setStudent(studentOpt.get());
        tuition.setTerm(term);
        tuition.setAmount(amount);
        tuition.setBalance(amount);

        tuitionRepository.save(tuition);
    }

    public Page<Tuition> getUnpaidTuitions(String term, Pageable pageable) {
        return tuitionRepository.findByTermAndBalanceGreaterThan(term, 0.0, pageable);
    }


    public void payTuition(com.group2.tuition_payment_system.dto.request.PaymentRequest request) {

    }
}