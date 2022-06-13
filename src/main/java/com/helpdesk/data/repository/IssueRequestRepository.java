package com.helpdesk.data.repository;

import com.helpdesk.data.model.IssueRequestModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface IssueRequestRepository extends PagingAndSortingRepository<IssueRequestModel, Integer> {
    Optional<IssueRequestModel> findByIdAndIsSolved(Integer id, boolean isActive);
    Page<IssueRequestModel> findAllByRequesterId(Integer requesterId, Pageable pageable);
    Page<IssueRequestModel> findAllByRequesterIdAndCreatedBeforeAndCreatedAfter(
            Integer requesterId, Date createdBefore, Date createdAfter, Pageable pageable);
    Page<IssueRequestModel> findAllByCreatedBeforeAndCreatedAfter(
            Date createdBefore, Date createdAfter, Pageable pageable);
    Page<IssueRequestModel> findAllByCreatedBeforeAndCreatedAfterAndIsSolved(
            Date createdBefore, Date createdAfter, boolean isSolved, Pageable pageable);
    Page<IssueRequestModel> findAllBySolvedBeforeAndSolvedAfter(
            Date solvedBefore, Date solvedAfter, Pageable pageable);
}
