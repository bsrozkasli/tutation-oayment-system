package com.group2.tuition_payment_system.repository;

import com.group2.tuition_payment_system.entity.Tuition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TuitionRepository extends JpaRepository<Tuition, Long> {

    List<Tuition> findByStudent_StudentNo(String studentNo);

    Optional<Tuition> findByStudent_StudentNoAndTerm(String studentNo, String term);


    @Query("SELECT SUM(t.amount - t.balance) FROM Tuition t WHERE t.student.studentNo = :studentNo")
    Double sumPaidByStudentNo(@Param("studentNo") String studentNo);


    @Query("SELECT SUM(t.amount) FROM Tuition t WHERE t.student.studentNo = :studentNo")
    Double sumAmountByStudentNo(@Param("studentNo") String studentNo);


    Page<Tuition> findByTermAndBalanceGreaterThan(String term, Double balance, Pageable pageable);
}