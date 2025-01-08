package com.SmartRiceAgriculture.SmartRiceAgriculture.controller;


import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.BidCreateRequest;
import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.BidOfferRequest;
import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.BidResponse;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Bid;
import com.SmartRiceAgriculture.SmartRiceAgriculture.service.BidService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bids")
@RequiredArgsConstructor
public class BidController {
    private final BidService bidService;

    // Create new bid (Farmer only)
    @PostMapping
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<BidResponse> createBid(@RequestBody BidCreateRequest request) {
        return ResponseEntity.ok(bidService.createBid(request));
    }

    // Place a bid (Buyer only)
    @PostMapping("/offer")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<BidResponse> placeBid(@RequestBody BidOfferRequest request) {
        return ResponseEntity.ok(bidService.placeBid(request));
    }

    // Cancel bid (Farmer only)
    @PostMapping("/{bidId}/cancel")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<BidResponse> cancelBid(@PathVariable Long bidId) {
        return ResponseEntity.ok(bidService.cancelBid(bidId));
    }

    // Get filtered and sorted bids (Public)
    @GetMapping("/active")
    public ResponseEntity<List<BidResponse>> getFilteredBids(
            @RequestParam(required = false) Bid.RiceVariety riceVariety,
            @RequestParam(required = false) Float minPrice,
            @RequestParam(required = false) Float maxPrice,
            @RequestParam(required = false) String location,
            @RequestParam(required = false, defaultValue = "date") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection) {
        return ResponseEntity.ok(bidService.getFilteredBids(
                riceVariety, minPrice, maxPrice, location, sortBy, sortDirection));
    }

    // Admin endpoints
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BidResponse>> getAllBids() {
        return ResponseEntity.ok(bidService.getAllBids());
    }

    @PutMapping("/admin/{bidId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BidResponse> updateBidStatus(
            @PathVariable Long bidId,
            @RequestParam Bid.BidStatus status) {
        return ResponseEntity.ok(bidService.updateBidStatus(bidId, status));
    }

    @PutMapping("/admin/{bidId}/force-complete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BidResponse> forceCompleteBid(
            @PathVariable Long bidId,
            @RequestParam String buyerNic,
            @RequestParam Float amount) {
        return ResponseEntity.ok(bidService.forceCompleteBid(bidId, buyerNic, amount));
    }

    @GetMapping("/admin/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getBidStatistics() {
        return ResponseEntity.ok(bidService.getBidStatistics());
    }

    // Get farmer's bids (Farmer and Admin only)
    @GetMapping("/farmer/{farmerNic}")
    @PreAuthorize("hasRole('ADMIN') or #farmerNic == authentication.name")
    public ResponseEntity<List<BidResponse>> getFarmerBids(@PathVariable String farmerNic) {
        return ResponseEntity.ok(bidService.getFarmerBids(farmerNic));
    }

    // Get buyer's winning bids (Buyer and Admin only)
    @GetMapping("/buyer/{buyerNic}/winning")
    @PreAuthorize("hasRole('ADMIN') or #buyerNic == authentication.name")
    public ResponseEntity<List<BidResponse>> getBuyerWinningBids(@PathVariable String buyerNic) {
        return ResponseEntity.ok(bidService.getBuyerWinningBids(buyerNic));
    }

    // Get single bid details
    @GetMapping("/{bidId}")
    public ResponseEntity<BidResponse> getBidDetails(@PathVariable Long bidId) {
        return ResponseEntity.ok(bidService.getBidDetails(bidId));
    }
}
