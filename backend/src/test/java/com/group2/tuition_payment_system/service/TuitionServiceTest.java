package com.group2.tuition_payment_system.service;

import com.group2.tuition_payment_system.dto.request.PaymentRequest;
import com.group2.tuition_payment_system.dto.response.TuitionStatusResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TuitionServiceTest {

    @Autowired
    private TuitionService tuitionService;

    @Test
    @DisplayName("Query tuition for existing student")
    void queryTuition_existingStudent_returnsStatus() {
        String studentNo = "2023001";

        TuitionStatusResponse response = tuitionService.queryTuition(studentNo);

        assertNotNull(response);
        assertEquals(studentNo, response.getStudentNo());
        assertNotNull(response.getTotalTuitionAmount());
        assertNotNull(response.getCurrentBalance());
    }

    @Test
    @DisplayName("Query tuition for non-existing student throws exception")
    void queryTuition_nonExistingStudent_throwsException() {
        String studentNo = "9999999";

        assertThrows(Exception.class, () -> {
            tuitionService.queryTuition(studentNo);
        });
    }

    @Test
    @DisplayName("Pay tuition reduces balance")
    void payTuition_validPayment_reducesBalance() {
        String studentNo = "2023001";
        String term = "2025-SUMMER";
        Double amount = 100.0;

        TuitionStatusResponse beforePayment = tuitionService.queryTuition(studentNo);
        Double balanceBefore = beforePayment.getCurrentBalance();

        PaymentRequest request = new PaymentRequest();
        request.setStudentNo(studentNo);
        request.setTerm(term);
        request.setAmount(amount);

        tuitionService.payTuition(request);

        TuitionStatusResponse afterPayment = tuitionService.queryTuition(studentNo);
        Double balanceAfter = afterPayment.getCurrentBalance();

        assertEquals(balanceBefore - amount, balanceAfter, 0.01);
    }

    @Test
    @DisplayName("Pay tuition with invalid amount throws exception")
    void payTuition_negativeAmount_throwsException() {
        PaymentRequest request = new PaymentRequest();
        request.setStudentNo("2023001");
        request.setTerm("2025-SUMMER");
        request.setAmount(-100.0);

        assertThrows(Exception.class, () -> {
            tuitionService.payTuition(request);
        });
    }

    @Test
    @DisplayName("Pay tuition for non-existing student throws exception")
    void payTuition_nonExistingStudent_throwsException() {
        PaymentRequest request = new PaymentRequest();
        request.setStudentNo("9999999");
        request.setTerm("2025-SUMMER");
        request.setAmount(100.0);

        assertThrows(Exception.class, () -> {
            tuitionService.payTuition(request);
        });
    }
}
