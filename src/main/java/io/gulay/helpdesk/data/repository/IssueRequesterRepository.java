package io.gulay.helpdesk.data.repository;

import io.gulay.helpdesk.data.model.IssueRequesterModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface IssueRequesterRepository extends JpaRepository<IssueRequesterModel, Integer> {
    Optional<IssueRequesterModel> findByIdAndIsActive(Integer id, boolean isActive);

    Page<IssueRequesterModel> findAllByCreatedBeforeAndCreatedAfter(
            LocalDateTime createdBefore, LocalDateTime createdAfter, Pageable pageable);

    Page<IssueRequesterModel> findAllByCreatedBeforeAndCreatedAfterAndIsActive(
            LocalDateTime createdBefore, LocalDateTime createdAfter, boolean isActive, Pageable pageable);

    Page<IssueRequesterModel> findAllByFullNameContainingIgnoreCase(String fullName, Pageable pageable);

    Page<IssueRequesterModel> findAllByFullNameContainingIgnoreCaseAndCreatedBeforeAndCreatedAfter(
            String fullName, LocalDateTime createdBefore, LocalDateTime createdAfter, Pageable pageable);

    Page<IssueRequesterModel> findAllByEmailContainingIgnoreCase(String email, Pageable pageable);

    Page<IssueRequesterModel> findAllByEmailContainingIgnoreCaseAndCreatedBeforeAndCreatedAfter(
            String email, LocalDateTime createdBefore, LocalDateTime createdAfter, Pageable pageable);

    boolean existsAllByEmail(String email);

    boolean existsByIdAndIsActive(Integer id, boolean isActive);
}
