package com.group2.tuition_payment_system.repository;

import com.group2.tuition_payment_system.entity.Tuition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface TuitionRepository extends JpaRepository<Tuition, Long> {

    List<Tuition> findByStudent_StudentNo(String studentNo);


    Optional<Tuition> findByStudent_StudentNoAndTerm(String studentNo, String term);


    @Query("SELECT t FROM Tuition t WHERE t.term = :term AND t.paidAmount < t.amount")
    Page<Tuition> findUnpaidTuitionsByTerm(String term, Pageable pageable);
}
