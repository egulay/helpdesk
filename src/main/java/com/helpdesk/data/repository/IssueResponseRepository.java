package com.helpdesk.data.repository;

import com.helpdesk.data.model.IssueResponseModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface IssueResponseRepository extends PagingAndSortingRepository<IssueResponseModel, Integer> {
    Page<IssueResponseModel> findAllByCreatedBeforeAndCreatedAfter(Date createdBefore, Date createdAfter,
                                                                   Pageable pageable);

    Page<IssueResponseModel> findAllByRequestId(Integer requestId, Pageable pageable);

    Page<IssueResponseModel> findAllByRequestIdAndCreatedBeforeAndCreatedAfter(Integer requestId,
                                                                               Date createdBefore, Date createdAfter,
                                                                               Pageable pageable);

    Page<IssueResponseModel> findAllByRequesterId(Integer requesterId, Pageable pageable);

    Page<IssueResponseModel> findAllByRequesterIdAndCreatedBeforeAndCreatedAfter(Integer requesterId,
                                                                                 Date createdBefore, Date createdAfter,
                                                                                 Pageable pageable);
}
