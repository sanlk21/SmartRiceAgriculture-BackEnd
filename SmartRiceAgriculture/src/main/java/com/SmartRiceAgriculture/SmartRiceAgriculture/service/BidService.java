package com.SmartRiceAgriculture.SmartRiceAgriculture.service;

import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.BidCreateRequest;
import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.BidOfferRequest;
import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.BidOfferResponse;
import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.BidResponse;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Bid;
import com.SmartRiceAgriculture.SmartRiceAgriculture.Repository.BidRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class BidService {
    private final BidRepository bidRepository;

    // Create a new bid listing (by farmer)
    public BidResponse createBid(BidCreateRequest request) {
        String farmerNic = SecurityContextHolder.getContext().getAuthentication().getName();

        Bid bid = new Bid();
        bid.setFarmerNic(farmerNic);
        bid.setQuantity(request.getQuantity());
        bid.setMinimumPrice(request.getMinimumPrice());
        bid.setRiceVariety(request.getRiceVariety());
        bid.setDescription(request.getDescription());
        bid.setLocation(request.getLocation());

        return convertToResponse(bidRepository.save(bid));
    }

    // Place a bid (by buyer)
    public BidResponse placeBid(BidOfferRequest request) {
        String buyerNic = SecurityContextHolder.getContext().getAuthentication().getName();

        Bid bid = bidRepository.findById(request.getBidId())
                .orElseThrow(() -> new EntityNotFoundException("Bid not found"));

        if (bid.getStatus() != Bid.BidStatus.ACTIVE) {
            throw new IllegalStateException("Bid is not active");
        }

        if (request.getBidAmount() < bid.getMinimumPrice()) {
            throw new IllegalStateException("Bid amount is below minimum price");
        }

        Bid.BidOffer bidOffer = new Bid.BidOffer(buyerNic, request.getBidAmount(), LocalDateTime.now());
        bid.getBidOffers().add(bidOffer);

        return convertToResponse(bidRepository.save(bid));
    }

    // Cancel bid (by farmer)
    public BidResponse cancelBid(Long bidId) {
        String farmerNic = SecurityContextHolder.getContext().getAuthentication().getName();

        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new EntityNotFoundException("Bid not found"));

        if (!bid.getFarmerNic().equals(farmerNic)) {
            throw new IllegalStateException("Not authorized to cancel this bid");
        }

        if (bid.getStatus() != Bid.BidStatus.ACTIVE) {
            throw new IllegalStateException("Can only cancel active bids");
        }

        bid.setStatus(Bid.BidStatus.CANCELLED);
        return convertToResponse(bidRepository.save(bid));
    }

    // Get filtered and sorted bids
    public List<BidResponse> getFilteredBids(
            Bid.RiceVariety riceVariety,
            Float minPrice,
            Float maxPrice,
            String location,
            String sortBy,
            String sortDirection) {

        List<Bid> bids = bidRepository.findByStatus(Bid.BidStatus.ACTIVE);

        // Apply filters
        Stream<Bid> bidStream = bids.stream();

        if (riceVariety != null) {
            bidStream = bidStream.filter(bid -> bid.getRiceVariety() == riceVariety);
        }

        if (minPrice != null) {
            bidStream = bidStream.filter(bid -> bid.getMinimumPrice() >= minPrice);
        }

        if (maxPrice != null) {
            bidStream = bidStream.filter(bid -> bid.getMinimumPrice() <= maxPrice);
        }

        if (location != null && !location.isEmpty()) {
            bidStream = bidStream.filter(bid ->
                    bid.getLocation() != null &&
                            bid.getLocation().toLowerCase().contains(location.toLowerCase())
            );
        }

        // Apply sorting
        if (sortBy != null && !sortBy.isEmpty()) {
            Comparator<Bid> comparator = switch (sortBy.toLowerCase()) {
                case "price" -> Comparator.comparing(Bid::getMinimumPrice);
                case "quantity" -> Comparator.comparing(Bid::getQuantity);
                case "date" -> Comparator.comparing(Bid::getPostedDate);
                default -> Comparator.comparing(Bid::getPostedDate);
            };

            if ("desc".equalsIgnoreCase(sortDirection)) {
                comparator = comparator.reversed();
            }

            bidStream = bidStream.sorted(comparator);
        }

        return bidStream
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Admin methods
    public List<BidResponse> getAllBids() {
        return bidRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public BidResponse updateBidStatus(Long bidId, Bid.BidStatus newStatus) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new EntityNotFoundException("Bid not found"));

        bid.setStatus(newStatus);
        return convertToResponse(bidRepository.save(bid));
    }

    public BidResponse forceCompleteBid(Long bidId, String buyerNic, Float amount) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new EntityNotFoundException("Bid not found"));

        bid.setWinningBuyerNic(buyerNic);
        bid.setWinningBidAmount(amount);
        bid.setWinningBidDate(LocalDateTime.now());
        bid.setStatus(Bid.BidStatus.COMPLETED);

        return convertToResponse(bidRepository.save(bid));
    }

    public Map<String, Object> getBidStatistics() {
        List<Bid> allBids = bidRepository.findAll();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalActiveBids", allBids.stream()
                .filter(b -> b.getStatus() == Bid.BidStatus.ACTIVE).count());
        stats.put("totalCompletedBids", allBids.stream()
                .filter(b -> b.getStatus() == Bid.BidStatus.COMPLETED).count());
        stats.put("averagePrice", allBids.stream()
                .mapToDouble(Bid::getMinimumPrice)
                .average()
                .orElse(0.0));
        stats.put("totalQuantity", allBids.stream()
                .mapToDouble(Bid::getQuantity)
                .sum());

        return stats;
    }

    // Get single bid details
    public BidResponse getBidDetails(Long bidId) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new EntityNotFoundException("Bid not found with id: " + bidId));
        return convertToResponse(bid);
    }

    // Get farmer's bids
    public List<BidResponse> getFarmerBids(String farmerNic) {
        return bidRepository.findByFarmerNic(farmerNic)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Get buyer's winning bids
    public List<BidResponse> getBuyerWinningBids(String buyerNic) {
        return bidRepository.findByWinningBuyerNic(buyerNic)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Scheduled task to process expired bids
    @Scheduled(fixedRate = 3600000) // Run every hour
    public void processExpiredBids() {
        List<Bid> expiredBids = bidRepository.findExpiredBids(LocalDateTime.now());

        for (Bid bid : expiredBids) {
            if (!bid.getBidOffers().isEmpty()) {
                // Find the highest bid
                Bid.BidOffer winningOffer = bid.getBidOffers().stream()
                        .max(Comparator
                                .comparing(Bid.BidOffer::getBidAmount)
                                .thenComparing(Bid.BidOffer::getBidDate))
                        .get();

                bid.setWinningBuyerNic(winningOffer.getBuyerNic());
                bid.setWinningBidAmount(winningOffer.getBidAmount());
                bid.setWinningBidDate(winningOffer.getBidDate());
                bid.setStatus(Bid.BidStatus.COMPLETED);
            } else {
                bid.setStatus(Bid.BidStatus.EXPIRED);
            }
            bidRepository.save(bid);
        }
    }

    private BidResponse convertToResponse(Bid bid) {
        BidResponse response = new BidResponse();
        response.setId(bid.getId());
        response.setFarmerNic(bid.getFarmerNic());
        response.setQuantity(bid.getQuantity());
        response.setMinimumPrice(bid.getMinimumPrice());
        response.setRiceVariety(bid.getRiceVariety());
        response.setDescription(bid.getDescription());
        response.setLocation(bid.getLocation());
        response.setPostedDate(bid.getPostedDate());
        response.setExpiryDate(bid.getExpiryDate());
        response.setStatus(bid.getStatus());
        response.setBidOffers(bid.getBidOffers().stream()
                .map(this::convertToOfferResponse)
                .collect(Collectors.toList()));
        response.setWinningBuyerNic(bid.getWinningBuyerNic());
        response.setWinningBidAmount(bid.getWinningBidAmount());
        response.setWinningBidDate(bid.getWinningBidDate());
        return response;
    }

    private BidOfferResponse convertToOfferResponse(Bid.BidOffer offer) {
        BidOfferResponse response = new BidOfferResponse();
        response.setBuyerNic(offer.getBuyerNic());
        response.setBidAmount(offer.getBidAmount());
        response.setBidDate(offer.getBidDate());
        return response;
    }
}
