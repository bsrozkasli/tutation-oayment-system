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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

/**
 * Service class for tuition and payment operations.
 * Handles business logic for querying tuition status, processing payments,
 * adding tuition records, batch uploading CSV files, and retrieving unpaid tuitions.
 *
 * @author Group 2
 */
@Service
@RequiredArgsConstructor
public class TuitionService {

    private final TuitionRepository tuitionRepository;
    private final StudentRepository studentRepository;
    private final PaymentRepository paymentRepository;

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


    @Transactional
    public void payTuition(PaymentRequest request) {

        if (request == null) {
            throw new RuntimeException("Payment request cannot be null");
        }
        if (request.getStudentNo() == null || request.getStudentNo().trim().isEmpty()) {
            throw new RuntimeException("Student number cannot be null or empty");
        }
        if (request.getTerm() == null || request.getTerm().trim().isEmpty()) {
            throw new RuntimeException("Term cannot be null or empty");
        }
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new RuntimeException("Payment amount must be greater than 0");
        }

        Optional<Student> studentOpt = studentRepository.findByStudentNo(request.getStudentNo());
        if (studentOpt.isEmpty()) {
            throw new RuntimeException("Student not found: " + request.getStudentNo());
        }

        List<Tuition> tuitions = tuitionRepository.findByStudent_StudentNoAndTerm(request.getStudentNo(), request.getTerm());

        if (tuitions.isEmpty()) {
            throw new RuntimeException("Tuition record not found for student: " + request.getStudentNo() + " and term: " + request.getTerm());
        }


        Double totalBalance = tuitions.stream()
                .mapToDouble(t -> t.getBalance() != null ? t.getBalance() : 0.0)
                .sum();

        if (totalBalance <= 0) {
            throw new RuntimeException("No outstanding balance for student: " + request.getStudentNo() + " and term: " + request.getTerm());
        }


        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new RuntimeException("Payment amount must be greater than 0");
        }


        Double remainingPayment = request.getAmount();
        
        for (Tuition tuition : tuitions) {
            if (remainingPayment <= 0) {
                break;
            }
            
            Double currentBalance = tuition.getBalance();
            if (currentBalance != null && currentBalance > 0) {
                if (remainingPayment >= currentBalance) {

                    tuition.setBalance(0.0);
                    remainingPayment -= currentBalance;
                } else {

                    tuition.setBalance(currentBalance - remainingPayment);
                    remainingPayment = 0.0;
                }
                tuitionRepository.save(tuition);
            }
        }


        Payment payment = new Payment();
        payment.setStudent(studentOpt.get());
        payment.setTerm(request.getTerm());
        payment.setAmount(request.getAmount());
        paymentRepository.save(payment);
    }

    @Transactional
    public int processBatchUpload(MultipartFile file) {
        int processedCount = 0;
        int errorCount = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {

                if (line.trim().isEmpty()) {
                    continue;
                }


                if (isFirstLine && (line.toLowerCase().contains("student") || line.toLowerCase().contains("term"))) {
                    isFirstLine = false;
                    continue;
                }
                isFirstLine = false;

                try {

                    String[] parts = line.split(",");
                    if (parts.length != 3) {
                        errorCount++;
                        continue;
                    }

                    String studentNo = parts[0].trim();
                    String term = parts[1].trim();
                    Double amount = Double.parseDouble(parts[2].trim());


                    addTuition(studentNo, term, amount);
                    processedCount++;

                } catch (Exception e) {
                    errorCount++;

                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error reading CSV file: " + e.getMessage(), e);
        }

        return processedCount;
    }
}