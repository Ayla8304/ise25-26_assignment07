package de.seuhd.campuscoffee.domain.implementation;

import de.seuhd.campuscoffee.domain.configuration.ApprovalConfiguration;
import de.seuhd.campuscoffee.domain.model.objects.Review;
import de.seuhd.campuscoffee.domain.ports.api.ReviewService;
import de.seuhd.campuscoffee.domain.ports.data.CrudDataService;
import de.seuhd.campuscoffee.domain.ports.data.PosDataService;
import de.seuhd.campuscoffee.domain.ports.data.ReviewDataService;
import de.seuhd.campuscoffee.domain.ports.data.UserDataService;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of the Review service that handles business logic related to review entities.
 */
@Slf4j
@Service
public class ReviewServiceImpl extends CrudServiceImpl<Review, Long> implements ReviewService {
    private final ReviewDataService reviewDataService;
    private final UserDataService userDataService;
    private final PosDataService posDataService;
    // TODO: Try to find out the purpose of this class and how it is connected to the application.yaml configuration file. (Done)
    private final ApprovalConfiguration approvalConfiguration;

    public ReviewServiceImpl(@NonNull ReviewDataService reviewDataService,
                             @NonNull UserDataService userDataService,
                             @NonNull PosDataService posDataService,
                             @NonNull ApprovalConfiguration approvalConfiguration) {
        super(Review.class);
        this.reviewDataService = reviewDataService;
        this.userDataService = userDataService;
        this.posDataService = posDataService;
        this.approvalConfiguration = approvalConfiguration;
    }

    @Override
    protected CrudDataService<Review, Long> dataService() {
        return reviewDataService;
    }

    @Override
    @Transactional
    public @NonNull Review upsert(@NonNull Review review) {
        // TODO: Implement the missing business logic here (Done)
        log.info("Upserting review for POS '{}' by user '{}'...",
                review.pos()!=null ? review.pos().getId() : null,
                review.author()!=null ? review.author().getId() : null);

         if (review.pos() == null || review.pos().getId() == null) {
            throw new ValidationException("POS must be set for a review.");
        }
        if (review.author() == null || review.author().getId() == null) {
            throw new ValidationException("Author must be set for a review.");
        }

        var pos = posDataService.getById(review.pos().getId());

        if (!reviewDataService.filter(pos, review.author()).isEmpty()) {
            throw new ValidationException("User cannot create more than one review fot this POS.");
        }

        Review toPersist = updateApprovalStatus(review);
        return super.upsert(toPersist);
    }

    @Override
    @Transactional(readOnly = true)
    public @NonNull List<Review> filter(@NonNull Long posId, @NonNull Boolean approved) {
        return reviewDataService.filter(posDataService.getById(posId), approved);
    }

    @Override
    @Transactional
    public @NonNull Review approve(@NonNull Review review, @NonNull Long userId) {
        log.info("Processing approval request for review with ID '{}' by user with ID '{}'...",
                review.getId(), userId);

        // validate that the user exists
        // TODO: Implement the required business logic here (Done)
        var user = userDataService.getById(userId);


        // validate that the review exists
        // TODO: Implement the required business logic here (Done)
        Review persistedReview = reviewDataService.getById(review.getId());

        // a user cannot approve their own review
        // TODO: Implement the required business logic here (Done)
        if (persistedReview.author().getId() != null
                && persistedReview.author().getId().equals(user.getId())) {
            throw new ValidationException("user cannot approve their own review.");
        }   

        // increment approval count
        // TODO: Implement the required business logic here (Done)
        Review updatedReview = persistedReview.toBuilder()
                .approvalCount(persistedReview.approvalCount() + 1)
                .build();

        review = updatedReview;

        // update approval status to determine if the review now reaches the approval quorum
        // TODO: Implement the required business logic here (Done)
        updatedReview = updateApprovalStatus(review);

        return reviewDataService.upsert(updatedReview);
    }

    /**
     * Calculates and updates the approval status of a review based on the approval count.
     * Business rule: A review is approved when it reaches the configured minimum approval count threshold.
     *
     * @param review The review to calculate approval status for
     * @return The review with updated approval status
     */
    Review updateApprovalStatus(Review review) {
        log.debug("Updating approval status of review with ID '{}'...", review.getId());
        return review.toBuilder()
                .approved(isApproved(review))
                .build();
    }
    
    /**
     * Determines if a review meets the minimum approval threshold.
     * 
     * @param review The review to check
     * @return true if the review meets or exceeds the minimum approval count, false otherwise
     */
    private boolean isApproved(Review review) {
        return review.approvalCount() >= approvalConfiguration.minCount();
    }
}
