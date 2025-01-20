package com.SmartRiceAgriculture.SmartRiceAgriculture.Repository;

import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Support;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SupportRepository extends JpaRepository<Support, Long> {
    List<Support> findByUserNic(String userNic);
    List<Support> findByStatus(Support.TicketStatus status);
    List<Support> findByUserNicOrderByCreatedAtDesc(String userNic);
    List<Support> findAllByOrderByCreatedAtDesc();
}