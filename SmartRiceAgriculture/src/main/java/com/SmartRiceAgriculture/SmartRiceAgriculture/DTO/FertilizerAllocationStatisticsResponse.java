package com.SmartRiceAgriculture.SmartRiceAgriculture.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FertilizerAllocationStatisticsResponse {
    private Long totalAllocations = 0L;
    private Long collectedCount = 0L;
    private Long pendingCount = 0L;
    private Double totalAmount = 0.0;
    private Double collectedAmount = 0.0;
    private Double pendingAmount = 0.0;
    private Long mahaSeasonCount = 0L;
    private Long yalaSeasonCount = 0L;
    private Long currentYearAllocations = 0L;
    private Double currentYearAmount = 0.0;
}
