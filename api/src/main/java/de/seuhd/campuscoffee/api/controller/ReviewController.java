package de.seuhd.campuscoffee.api.controller;

import de.seuhd.campuscoffee.api.dtos.ReviewDto;
import de.seuhd.campuscoffee.api.mapper.DtoMapper;
import de.seuhd.campuscoffee.api.mapper.ReviewDtoMapper;
import de.seuhd.campuscoffee.api.openapi.CrudOperation;
import de.seuhd.campuscoffee.domain.model.objects.Review;
import de.seuhd.campuscoffee.domain.ports.api.CrudService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import jakarta.validation.Valid;
import de.seuhd.campuscoffee.domain.ports.api.ReviewService;

import static de.seuhd.campuscoffee.api.openapi.Operation.*;
import static de.seuhd.campuscoffee.api.openapi.Resource.REVIEW;

/**
 * Controller for handling reviews for POS, authored by users.
 */
@Tag(name="Reviews", description="Operations for managing reviews for points of sale.")
@Controller
@RequestMapping("/api/reviews")
@Slf4j
@RequiredArgsConstructor
public class ReviewController extends CrudController<Review, ReviewDto, Long> {

    // TODO: Correctly implement the service() and mapper() methods. Note the IntelliJ warning resulting from the @NonNull annotation. (Done)
    private final ReviewService reviewService;
    private final ReviewDtoMapper reviewDtoMapper;

    @Override
    protected @NonNull CrudService<Review, Long> service() {
        return reviewService;
    }

    @Override
    protected @NonNull DtoMapper<Review, ReviewDto> mapper() {
        return reviewDtoMapper;
    }

    @Operation
    @CrudOperation(operation=GET_ALL, resource=REVIEW)
    @GetMapping("")
    public @NonNull ResponseEntity<List<ReviewDto>> getAll() {
        return super.getAll();
    }

    // TODO: Implement the missing methods/endpoints. (Done)
    @Operation
    @CrudOperation(operation=GET_BY_ID, resource=REVIEW)
    @GetMapping("/{id}")
    public @NonNull ResponseEntity<ReviewDto> getById(@PathVariable Long id) {
        return super.getById(id);
    }

    @Operation
    @CrudOperation(operation=CREATE, resource=REVIEW)
    @PostMapping("")
    public @NonNull ResponseEntity<ReviewDto> create(@RequestBody @Valid ReviewDto dto) {
        return super.create(dto);
    }

    @Operation
    @CrudOperation(operation=UPDATE, resource=REVIEW)
    @PutMapping("/{id}")
    public @NonNull ResponseEntity<ReviewDto> update(@PathVariable Long id, @RequestBody ReviewDto dto) {
        return super.update(id, dto);
    } 

    @Operation
    @CrudOperation(operation=DELETE, resource=REVIEW)
    @DeleteMapping("/{id}")
    public @NonNull ResponseEntity<Void> delete(@PathVariable Long id) {
        return super.delete(id);
    }

    @Operation
    @GetMapping("/filter")
    public ResponseEntity<List<ReviewDto>> filter(
            @RequestParam("pos_id") Long posId,
            @RequestParam("approved") Boolean approved
    ) {
        List<Review> reviews = reviewService.filter(posId, approved);
        List<ReviewDto> dtos = reviews.stream()
                .map(reviewDtoMapper::fromDomain)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @Operation
    @PostMapping("/{id}/approve")
    public ResponseEntity<ReviewDto> approve(
        @PathVariable Long id, 
        @RequestParam("user_id") Long userId
    ) {
        Review review = service().getById(id);
        Review approvedReview = reviewService.approve(review, userId);
        ReviewDto dto = reviewDtoMapper.fromDomain(approvedReview);
        return ResponseEntity.ok(dto);
    }
}
