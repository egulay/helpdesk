package io.gulay.helpdesk.data.repository;

import io.gulay.helpdesk.data.model.IssueResponseModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface IssueResponseRepository extends JpaRepository<IssueResponseModel, Integer> {
    Page<IssueResponseModel> findAllByCreatedBeforeAndCreatedAfter(
            LocalDateTime createdBefore, LocalDateTime createdAfter, Pageable pageable);

    Page<IssueResponseModel> findAllByRequestId(Integer requestId, Pageable pageable);

    Page<IssueResponseModel> findAllByRequestIdAndCreatedBeforeAndCreatedAfter(
            Integer requestId, LocalDateTime createdBefore, LocalDateTime createdAfter, Pageable pageable);

    Page<IssueResponseModel> findAllByRequesterId(Integer requesterId, Pageable pageable);

    Page<IssueResponseModel> findAllByRequesterIdAndCreatedBeforeAndCreatedAfter(
            Integer requesterId, LocalDateTime createdBefore, LocalDateTime createdAfter, Pageable pageable);
}
