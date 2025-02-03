package com.SmartRiceAgriculture.SmartRiceAgriculture.Repository;

import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findFirstByPaymentNumberStartingWithOrderByPaymentNumberDesc(String prefix);
    boolean existsByOrderId(Long orderId);
    // Basic finder methods
    Optional<Payment> findByPaymentNumber(String paymentNumber);
    List<Payment> findByBuyerNic(String buyerNic);
    List<Payment> findByFarmerNic(String farmerNic);
    List<Payment> findByOrderId(Long orderId);
    List<Payment> findByStatus(Payment.PaymentStatus status);

    // Paginated queries
    Page<Payment> findByBuyerNic(String buyerNic, Pageable pageable);
    Page<Payment> findByFarmerNic(String farmerNic, Pageable pageable);
    Page<Payment> findByStatus(Payment.PaymentStatus status, Pageable pageable);

    // Complex queries
    @Query("SELECT p FROM Payment p WHERE p.status = :status AND p.paymentMethod = :method")
    List<Payment> findByStatusAndMethod(
            @Param("status") Payment.PaymentStatus status,
            @Param("method") Payment.PaymentMethod method
    );

    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING' AND p.createdAt < :cutoffTime")
    List<Payment> findExpiredPendingPayments(@Param("cutoffTime") LocalDateTime cutoffTime);

    @Query("SELECT p FROM Payment p WHERE p.status = 'PROCESSING' AND p.lastAttemptAt < :cutoffTime")
    List<Payment> findStaleProcessingPayments(@Param("cutoffTime") LocalDateTime cutoffTime);

    // Filtering by date range
    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    List<Payment> findPaymentsInDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // Statistics queries
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status")
    long countByStatus(@Param("status") Payment.PaymentStatus status);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED'")
    Double getTotalSuccessfulPayments();

    @Query("SELECT p.paymentMethod, COUNT(p) FROM Payment p GROUP BY p.paymentMethod")
    List<Object[]> getPaymentMethodDistribution();

    // Buyer statistics
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.buyerNic = :buyerNic AND p.status = 'COMPLETED'")
    long countCompletedPaymentsByBuyer(@Param("buyerNic") String buyerNic);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.buyerNic = :buyerNic AND p.status = 'COMPLETED'")
    Double getTotalPaymentsByBuyer(@Param("buyerNic") String buyerNic);

    // Farmer statistics
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.farmerNic = :farmerNic AND p.status = 'COMPLETED'")
    long countReceivedPaymentsByFarmer(@Param("farmerNic") String farmerNic);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.farmerNic = :farmerNic AND p.status = 'COMPLETED'")
    Double getTotalReceivedByFarmer(@Param("farmerNic") String farmerNic);

    // Custom aggregate queries
    @Query("SELECT NEW map(" +
            "p.paymentMethod as method, " +
            "COUNT(p) as count, " +
            "SUM(p.amount) as total) " +
            "FROM Payment p " +
            "WHERE p.status = 'COMPLETED' " +
            "GROUP BY p.paymentMethod")
    List<Object> getPaymentMethodStats();

    @Query("SELECT NEW map(" +
            "FUNCTION('YEAR', p.createdAt) as year, " +
            "FUNCTION('MONTH', p.createdAt) as month, " +
            "COUNT(p) as count, " +
            "SUM(p.amount) as total) " +
            "FROM Payment p " +
            "WHERE p.status = 'COMPLETED' " +
            "GROUP BY FUNCTION('YEAR', p.createdAt), FUNCTION('MONTH', p.createdAt)")
    List<Object> getMonthlyPaymentStats();

    // Validation queries
    boolean existsByOrderIdAndStatusNot(Long orderId, Payment.PaymentStatus status);
    boolean existsByPaymentNumberAndStatusNot(String paymentNumber, Payment.PaymentStatus status);

    // Search queries
    @Query("SELECT p FROM Payment p WHERE " +
            "p.paymentNumber LIKE %:keyword% OR " +
            "p.buyerNic LIKE %:keyword% OR " +
            "p.farmerNic LIKE %:keyword%")
    Page<Payment> searchPayments(@Param("keyword") String keyword, Pageable pageable);
}
