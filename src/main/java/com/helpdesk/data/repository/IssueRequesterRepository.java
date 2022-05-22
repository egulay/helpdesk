package com.helpdesk.data.repository;

import com.helpdesk.data.model.IssueRequesterModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface IssueRequesterRepository extends PagingAndSortingRepository<IssueRequesterModel, Integer> {
    Optional<IssueRequesterModel> findByIdAndIsActive(Integer id, boolean isActive);

    Page<IssueRequesterModel> findAllByCreatedBeforeAndCreatedAfter(Date createdBefore, Date createdAfter,
                                                                    Pageable pageable);

    Page<IssueRequesterModel> findAllByCreatedBeforeAndCreatedAfterAndIsActive(Date createdBefore,
                                                                               Date createdAfter,
                                                                               boolean isActive,
                                                                               Pageable pageable);

    Page<IssueRequesterModel> findAllByFullNameContainingIgnoreCase(String fullName, Pageable pageable);


    Page<IssueRequesterModel> findAllByFullNameContainingIgnoreCaseAndCreatedBeforeAndCreatedAfter(String fullName,
                                                                                                   Date createdBefore,
                                                                                                   Date createdAfter,
                                                                                                   Pageable pageable);

    Page<IssueRequesterModel> findAllByEmailContainingIgnoreCase(String email, Pageable pageable);


    Page<IssueRequesterModel> findAllByEmailContainingIgnoreCaseAndCreatedBeforeAndCreatedAfter(String email,
                                                                                                Date createdBefore,
                                                                                                Date createdAfter,
                                                                                                Pageable pageable);

    boolean existsAllByEmail(String email);

    boolean existsByIdAndIsActive(Integer id, boolean isActive);
}
