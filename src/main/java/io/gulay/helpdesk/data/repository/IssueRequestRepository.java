package io.gulay.helpdesk.data.repository;

import io.gulay.helpdesk.data.model.IssueRequestModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface IssueRequestRepository extends JpaRepository<IssueRequestModel, Integer> {
    Optional<IssueRequestModel> findByIdAndIsSolved(Integer id, boolean isActive);

    Page<IssueRequestModel> findAllByRequesterId(Integer requesterId, Pageable pageable);

    Page<IssueRequestModel> findAllByRequesterIdAndCreatedBeforeAndCreatedAfter(
            Integer requesterId, LocalDateTime createdBefore, LocalDateTime createdAfter, Pageable pageable);

    Page<IssueRequestModel> findAllByCreatedBeforeAndCreatedAfter(
            LocalDateTime createdBefore, LocalDateTime createdAfter, Pageable pageable);

    Page<IssueRequestModel> findAllByCreatedBeforeAndCreatedAfterAndIsSolved(
            LocalDateTime createdBefore, LocalDateTime createdAfter, boolean isSolved, Pageable pageable);

    Page<IssueRequestModel> findAllBySolvedBeforeAndSolvedAfter(
            LocalDateTime solvedBefore, LocalDateTime solvedAfter, Pageable pageable);
}
