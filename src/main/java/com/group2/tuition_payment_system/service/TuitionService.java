package com.group2.tuition_payment_system.service;

import com.group2.tuition_payment_system.dto.request.PaymentRequest;
import com.group2.tuition_payment_system.dto.response.TuitionStatusResponse;
import com.group2.tuition_payment_system.entity.Payment;
import com.group2.tuition_payment_system.entity.Student;
import com.group2.tuition_payment_system.entity.Tuition;
import com.group2.tuition_payment_system.repository.PaymentRepository;
import com.group2.tuition_payment_system.repository.StudentRepository;
import com.group2.tuition_payment_system.repository.TuitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class TuitionService {

    private final StudentRepository studentRepository;
    private final TuitionRepository tuitionRepository;
    private final PaymentRepository paymentRepository;


    public TuitionStatusResponse queryTuition(String studentNo) {
        Student student = studentRepository.findById(studentNo)
                .orElseThrow(() -> new RuntimeException("Student not found: " + studentNo));
        List<Tuition> tuitions = tuitionRepository.findByStudent_StudentNo(studentNo);
        double totalTuition = tuitions.stream().mapToDouble(Tuition::getAmount).sum();
        double totalPaid = tuitions.stream().mapToDouble(Tuition::getPaidAmount).sum();
        double currentBalance = totalTuition - totalPaid;
        return TuitionStatusResponse.builder()
                .studentNo(student.getStudentNo())
                .totalTuitionAmount(totalTuition)
                .currentBalance(currentBalance)
                .build();
    }

    @Transactional
    public void payTuition(PaymentRequest request) {
        Student student = studentRepository.findById(request.getStudentNo())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        Tuition tuition = tuitionRepository.findByStudent_StudentNoAndTerm(request.getStudentNo(), request.getTerm())
                .orElseThrow(() -> new RuntimeException("No tuition record found for term: " + request.getTerm()));
        tuition.setPaidAmount(tuition.getPaidAmount() + request.getAmount());
        tuitionRepository.save(tuition);
        Payment payment = new Payment();
        payment.setStudent(student);
        payment.setTerm(request.getTerm());
        payment.setAmount(request.getAmount());
        paymentRepository.save(payment);
    }

    @Transactional
    public void addTuition(String studentNo, String term, Double amount) {
        Student student = studentRepository.findById(studentNo)
                .orElseThrow(() -> new RuntimeException("Student not found: " + studentNo));
        Tuition tuition = new Tuition();
        tuition.setStudent(student);
        tuition.setTerm(term);
        tuition.setAmount(amount);
        tuition.setPaidAmount(0.0);
        tuitionRepository.save(tuition);
    }

    @Transactional
    public void addTuitionBatch(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 3) {
                    addTuition(data[0].trim(), data[1].trim(), Double.parseDouble(data[2].trim()));
                }
            }
        } catch (IOException | NumberFormatException e) {
            throw new RuntimeException("CSV Error: " + e.getMessage());
        }
    }

    public Page<Tuition> getUnpaidTuitions(String term, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return tuitionRepository.findUnpaidTuitionsByTerm(term, pageable);
    }
}