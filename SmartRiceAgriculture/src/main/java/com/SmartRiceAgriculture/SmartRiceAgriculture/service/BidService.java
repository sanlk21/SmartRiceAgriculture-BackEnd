package com.SmartRiceAgriculture.SmartRiceAgriculture.service;

import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.*;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Bid;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Notification;
import com.SmartRiceAgriculture.SmartRiceAgriculture.Repository.BidRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class BidService {
    private final BidRepository bidRepository;
    private final NotificationService notificationService;
    private final OrderService orderService;

    public BidResponse createBid(BidCreateRequest request) {
        // Validate the request
        if (request.getFarmerNic() == null || request.getFarmerNic().isEmpty()) {
            throw new IllegalArgumentException("Farmer NIC is required");
        }

        Bid bid = new Bid();
        bid.setFarmerNic(request.getFarmerNic());
        bid.setQuantity(request.getQuantity());
        bid.setMinimumPrice(request.getMinimumPrice());
        bid.setRiceVariety(request.getRiceVariety());
        bid.setDescription(request.getDescription());
        bid.setLocation(request.getLocation());
        bid.setHarvestDate(request.getHarvestDate());
        bid.setPostedDate(LocalDateTime.now());
        bid.setStatus(Bid.BidStatus.ACTIVE);

        Bid savedBid = bidRepository.save(bid);

        // Send notification to farmer confirming bid creation
        notificationService.createBidNotification(
                request.getFarmerNic(),
                savedBid.getId(),
                Notification.NotificationType.BID_PLACED,
                savedBid.getMinimumPrice().toString()
        );

        return convertToResponse(savedBid);
    }

    // Place a bid (by buyer)
    public BidResponse placeBid(BidOfferRequest request) {
        // Remove this line since we're not using security context
        // String buyerNic = SecurityContextHolder.getContext().getAuthentication().getName();

        Bid bid = bidRepository.findById(request.getBidId())
                .orElseThrow(() -> new EntityNotFoundException("Bid not found"));

        if (bid.getStatus() != Bid.BidStatus.ACTIVE) {
            throw new IllegalStateException("Bid is not active");
        }

        if (request.getBidAmount() < bid.getMinimumPrice()) {
            throw new IllegalStateException("Bid amount is below minimum price");
        }

        // Use buyerNic from request
        Bid.BidOffer bidOffer = new Bid.BidOffer(
                request.getBuyerNic(),  // Use NIC from request
                request.getBidAmount(),
                LocalDateTime.now()
        );

        bid.getBidOffers().add(bidOffer);
        Bid savedBid = bidRepository.save(bid);

        // Notify farmer about new bid
        notificationService.createBidNotification(
                bid.getFarmerNic(),
                bid.getId(),
                Notification.NotificationType.BID_PLACED,
                request.getBidAmount().toString()
        );

        return convertToResponse(savedBid);
    }

    @Transactional
    public BidResponse acceptOffer(Long bidId, String buyerNic) {
        // Find the bid
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new EntityNotFoundException("Bid not found"));

        // Validate bid status
        if (bid.getStatus() != Bid.BidStatus.ACTIVE) {
            throw new IllegalStateException("Bid must be active to accept offers. Current status: " + bid.getStatus());
        }

        // Find the specific offer
        Bid.BidOffer winningOffer = bid.getBidOffers().stream()
                .filter(offer -> offer.getBuyerNic().equals(buyerNic))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Offer not found for this buyer"));

        try {
            // Store the harvest date before any status changes
            LocalDateTime harvestDate = bid.getHarvestDate();

            // Update bid status and winning details
            bid.setWinningBuyerNic(buyerNic);
            bid.setWinningBidAmount(winningOffer.getBidAmount());
            bid.setWinningBidDate(LocalDateTime.now());
            bid.setStatus(Bid.BidStatus.ACCEPTED);
            bid.setHarvestDate(harvestDate); // Ensure harvest date is preserved

            // Save bid before creating order
            Bid savedBid = bidRepository.save(bid);

            // Create order
            orderService.createOrder(
                    savedBid.getId(),
                    buyerNic,
                    savedBid.getFarmerNic(),
                    savedBid.getQuantity(),
                    winningOffer.getBidAmount()
            );

            // Update bid status to COMPLETED only after successful order creation
            savedBid.setStatus(Bid.BidStatus.COMPLETED);
            savedBid.setHarvestDate(harvestDate); // Ensure harvest date is preserved
            Bid completedBid = bidRepository.save(savedBid);

            // Notify winning buyer
            notificationService.createBidNotification(
                    buyerNic,
                    completedBid.getId(),
                    Notification.NotificationType.BID_ACCEPTED,
                    winningOffer.getBidAmount().toString()
            );

            // Notify other buyers about rejection
            bid.getBidOffers().stream()
                    .filter(offer -> !offer.getBuyerNic().equals(buyerNic))
                    .forEach(offer -> notificationService.createBidNotification(
                            offer.getBuyerNic(),
                            completedBid.getId(),
                            Notification.NotificationType.BID_REJECTED,
                            offer.getBidAmount().toString()
                    ));

            return convertToResponse(completedBid);

        } catch (Exception e) {
            // If order creation fails, revert bid status to ACTIVE
            LocalDateTime harvestDate = bid.getHarvestDate(); // Store harvest date
            bid.setStatus(Bid.BidStatus.ACTIVE);
            bid.setWinningBuyerNic(null);
            bid.setWinningBidAmount(null);
            bid.setWinningBidDate(null);
            bid.setHarvestDate(harvestDate); // Restore harvest date
            bidRepository.save(bid);
            throw e;
        }
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
        Bid savedBid = bidRepository.save(bid);

        // Notify all bidders about cancellation
        for (Bid.BidOffer offer : bid.getBidOffers()) {
            notificationService.createBidNotification(
                    offer.getBuyerNic(),
                    bid.getId(),
                    Notification.NotificationType.BID_REJECTED,
                    offer.getBidAmount().toString()
            );
        }

        return convertToResponse(savedBid);
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

    // Get farmer's bids
    public List<BidResponse> getFarmerBids(String farmerNic) {
        return bidRepository.findByFarmerNic(farmerNic)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Get buyer's winning bids
    public List<BidResponse> getBuyerWinningBids(String buyerNic) {
        if (buyerNic == null || buyerNic.isEmpty()) {
            throw new IllegalArgumentException("Buyer NIC is required");
        }

        List<Bid> winningBids = bidRepository.findByWinningBuyerNic(buyerNic);
        return winningBids.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Get single bid details
    public BidResponse getBidDetails(Long bidId) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new EntityNotFoundException("Bid not found"));
        return convertToResponse(bid);
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

    public Map<String, Object> getBidStatistics() {
        List<Bid> allBids = bidRepository.findAll();

        return Map.of(
                "totalActiveBids", allBids.stream()
                        .filter(b -> b.getStatus() == Bid.BidStatus.ACTIVE).count(),
                "totalCompletedBids", allBids.stream()
                        .filter(b -> b.getStatus() == Bid.BidStatus.COMPLETED).count(),
                "averagePrice", allBids.stream()
                        .mapToDouble(Bid::getMinimumPrice)
                        .average()
                        .orElse(0.0),
                "totalQuantity", allBids.stream()
                        .mapToDouble(Bid::getQuantity)
                        .sum()
        );
    }
    // Add this method to your BidService class
    public BidResponse forceCompleteBid(Long bidId, String buyerNic, Float amount) {
        // Find the bid
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new EntityNotFoundException("Bid not found"));

        // Update bid status and winning details
        bid.setWinningBuyerNic(buyerNic);
        bid.setWinningBidAmount(amount);
        bid.setWinningBidDate(LocalDateTime.now());
        bid.setStatus(Bid.BidStatus.COMPLETED);

        // Create order
        orderService.createOrder(
                bid.getId(),
                buyerNic,
                bid.getFarmerNic(),
                bid.getQuantity(),
                amount
        );

        // Save the updated bid
        Bid savedBid = bidRepository.save(bid);

        // Notify winning buyer
        notificationService.createBidNotification(
                buyerNic,
                bid.getId(),
                Notification.NotificationType.BID_ACCEPTED,
                amount.toString()
        );

        // Notify farmer
        notificationService.createBidNotification(
                bid.getFarmerNic(),
                bid.getId(),
                Notification.NotificationType.BID_ACCEPTED,
                amount.toString()
        );

        // Notify other bidders their offers were rejected
        if (bid.getBidOffers() != null) {
            bid.getBidOffers().stream()
                    .filter(offer -> !offer.getBuyerNic().equals(buyerNic))
                    .forEach(offer -> notificationService.createBidNotification(
                            offer.getBuyerNic(),
                            bid.getId(),
                            Notification.NotificationType.BID_REJECTED,
                            offer.getBidAmount().toString()
                    ));
        }

        return convertToResponse(savedBid);
    }

    private BidResponse convertToResponse(Bid bid) {
        BidResponse response = new BidResponse();
        response.setId(bid.getId());
        response.setFarmerNic(bid.getFarmerNic());          // Keep actual farmer NIC
        response.setQuantity(bid.getQuantity());
        response.setMinimumPrice(bid.getMinimumPrice());
        response.setRiceVariety(bid.getRiceVariety());
        response.setDescription(bid.getDescription());
        response.setLocation(bid.getLocation());
        response.setPostedDate(bid.getPostedDate());
        response.setExpiryDate(bid.getExpiryDate());
        response.setStatus(bid.getStatus());
        response.setHarvestDate(bid.getHarvestDate());
        response.setBidOffers(bid.getBidOffers().stream()
                .map(this::convertToOfferResponse)          // Keep actual buyer NICs
                .collect(Collectors.toList()));
        response.setWinningBuyerNic(bid.getWinningBuyerNic()); // Keep actual winning buyer NIC
        response.setWinningBidAmount(bid.getWinningBidAmount());
        response.setWinningBidDate(bid.getWinningBidDate());
        return response;
    }

    private BidOfferResponse convertToOfferResponse(Bid.BidOffer offer) {
        BidOfferResponse response = new BidOfferResponse();
        response.setBuyerNic(offer.getBuyerNic());        // Keep actual buyer NIC
        response.setBidAmount(offer.getBidAmount());
        response.setBidDate(offer.getBidDate());
        return response;
    }
}