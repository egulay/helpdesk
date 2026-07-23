//----------------------------------------
//IssueRequesterService.java
//----------------------------------------

package io.gulay.helpdesk.data.service;

import io.gulay.helpdesk.data.model.IssueRequesterModel;
import io.gulay.helpdesk.data.repository.IssueRequesterRepository;
import io.gulay.helpdesk.data.util.ExceptionMapperUtil;
import io.gulay.helpdesk.data.util.GenericPagedModel;
import io.gulay.helpdesk.data.util.SortDirection;
import jakarta.validation.ConstraintViolationException;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;

@Service
@Transactional(readOnly = true)
public class IssueRequesterService {
    final IssueRequesterRepository issueRequesterRepository;


    @Autowired
    public IssueRequesterService(IssueRequesterRepository issueRequesterRepository) {
        this.issueRequesterRepository = issueRequesterRepository;
    }

    public IssueRequesterModel findById(Integer id) {
        return getRequester(id);
    }

    public IssueRequesterModel findById(Integer id, boolean isActive) {
        return getRequester(id, isActive);
    }

    public GenericPagedModel<IssueRequesterModel> findAll(
            int page, int size, String sortBy, SortDirection sortDirection) {
        try {
            val requesters = sortDirection.equals(SortDirection.Ascending)
                    ? issueRequesterRepository.findAll(PageRequest.of(page, size, Sort.by(sortBy).ascending()))
                    : issueRequesterRepository.findAll(PageRequest.of(page, size, Sort.by(sortBy).descending()));
            if (requesters.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No data");
            }

            return GenericPagedModel.<IssueRequesterModel>builder()
                    .totalElements(requesters.getTotalElements())
                    .numberOfElements(requesters.getNumberOfElements())
                    .totalPages(requesters.getTotalPages())
                    .content(requesters.getContent())
                    .build();

        } catch (final ConstraintViolationException | DataIntegrityViolationException | TransactionSystemException ex) {
            throw ExceptionMapperUtil.mapPersistenceException(ex);
        }
    }

    public GenericPagedModel<IssueRequesterModel> findAllByFullNameContainingIgnoreCase(
            String fullName, int page, int size, String sortBy, SortDirection sortDirection) {
        try {
            val requesters = sortDirection.equals(SortDirection.Ascending)
                    ? issueRequesterRepository.findAllByFullNameContainingIgnoreCase(fullName,
                    PageRequest.of(page, size, Sort.by(sortBy).ascending()))
                    : issueRequesterRepository.findAllByFullNameContainingIgnoreCase(fullName,
                    PageRequest.of(page, size, Sort.by(sortBy).descending()));
            if (requesters.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "fullName:".concat(fullName));
            }

            return GenericPagedModel.<IssueRequesterModel>builder()
                    .totalElements(requesters.getTotalElements())
                    .numberOfElements(requesters.getNumberOfElements())
                    .totalPages(requesters.getTotalPages())
                    .content(requesters.getContent())
                    .build();

        } catch (final ConstraintViolationException | DataIntegrityViolationException | TransactionSystemException ex) {
            throw ExceptionMapperUtil.mapPersistenceException(ex);
        }
    }

    public GenericPagedModel<IssueRequesterModel> findAllByFullNameContainingIgnoreCaseAndCreatedBeforeAndCreatedAfter(
            String fullName, Date createdBefore, Date createdAfter, int page, int size, String sortBy,
            SortDirection sortDirection) {
        try {
            val requesters = sortDirection.equals(SortDirection.Ascending)
                    ? issueRequesterRepository.findAllByFullNameContainingIgnoreCaseAndCreatedBeforeAndCreatedAfter(
                    fullName, createdBefore, createdAfter,
                    PageRequest.of(page, size, Sort.by(sortBy).ascending()))
                    : issueRequesterRepository.findAllByFullNameContainingIgnoreCaseAndCreatedBeforeAndCreatedAfter(
                    fullName, createdBefore, createdAfter,
                    PageRequest.of(page, size, Sort.by(sortBy).descending()));
            if (requesters.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "fullName:".concat(fullName)
                                .concat(",createdBefore:")
                                .concat(String.valueOf(createdBefore.toInstant().toEpochMilli()))
                                .concat(",createdAfter:")
                                .concat(String.valueOf(createdAfter.toInstant().toEpochMilli())));
            }

            return GenericPagedModel.<IssueRequesterModel>builder()
                    .totalElements(requesters.getTotalElements())
                    .numberOfElements(requesters.getNumberOfElements())
                    .totalPages(requesters.getTotalPages())
                    .content(requesters.getContent())
                    .build();

        } catch (final ConstraintViolationException | DataIntegrityViolationException | TransactionSystemException ex) {
            throw ExceptionMapperUtil.mapPersistenceException(ex);
        }
    }

    public GenericPagedModel<IssueRequesterModel> findAllByEmailContainingIgnoreCase(
            String email, int page, int size, String sortBy, SortDirection sortDirection) {
        try {
            val requesters = sortDirection.equals(SortDirection.Ascending)
                    ? issueRequesterRepository.findAllByEmailContainingIgnoreCase(email,
                    PageRequest.of(page, size, Sort.by(sortBy).ascending()))
                    : issueRequesterRepository.findAllByEmailContainingIgnoreCase(email,
                    PageRequest.of(page, size, Sort.by(sortBy).descending()));
            if (requesters.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "email:".concat(email));
            }

            return GenericPagedModel.<IssueRequesterModel>builder()
                    .totalElements(requesters.getTotalElements())
                    .numberOfElements(requesters.getNumberOfElements())
                    .totalPages(requesters.getTotalPages())
                    .content(requesters.getContent())
                    .build();

        } catch (final ConstraintViolationException | DataIntegrityViolationException | TransactionSystemException ex) {
            throw ExceptionMapperUtil.mapPersistenceException(ex);
        }
    }

    public GenericPagedModel<IssueRequesterModel> findAllByEmailContainingIgnoreCaseAndCreatedBeforeAndCreatedAfter(
            String email, Date createdBefore, Date createdAfter, int page, int size, String sortBy,
            SortDirection sortDirection) {
        try {
            val requesters = sortDirection.equals(SortDirection.Ascending)
                    ? issueRequesterRepository.findAllByEmailContainingIgnoreCaseAndCreatedBeforeAndCreatedAfter(
                    email, createdBefore, createdAfter,
                    PageRequest.of(page, size, Sort.by(sortBy).ascending()))
                    : issueRequesterRepository.findAllByEmailContainingIgnoreCaseAndCreatedBeforeAndCreatedAfter(
                    email, createdBefore, createdAfter,
                    PageRequest.of(page, size, Sort.by(sortBy).descending()));
            if (requesters.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "email:".concat(email)
                                .concat(",createdBefore:")
                                .concat(String.valueOf(createdBefore.toInstant().toEpochMilli()))
                                .concat(",createdAfter:")
                                .concat(String.valueOf(createdAfter.toInstant().toEpochMilli())));
            }

            return GenericPagedModel.<IssueRequesterModel>builder()
                    .totalElements(requesters.getTotalElements())
                    .numberOfElements(requesters.getNumberOfElements())
                    .totalPages(requesters.getTotalPages())
                    .content(requesters.getContent())
                    .build();

        } catch (final ConstraintViolationException | DataIntegrityViolationException | TransactionSystemException ex) {
            throw ExceptionMapperUtil.mapPersistenceException(ex);
        }
    }

    public GenericPagedModel<IssueRequesterModel> findAllByCreatedBeforeAndCreatedAfterAndIsActive(
            Date createdBefore, Date createdAfter, boolean isActive, int page, int size, String sortBy,
            SortDirection sortDirection) {
        try {
            val requesters = sortDirection.equals(SortDirection.Ascending)
                    ? issueRequesterRepository.findAllByCreatedBeforeAndCreatedAfterAndIsActive(
                    createdBefore, createdAfter, isActive,
                    PageRequest.of(page, size, Sort.by(sortBy).ascending()))
                    : issueRequesterRepository.findAllByCreatedBeforeAndCreatedAfterAndIsActive(
                    createdBefore, createdAfter, isActive,
                    PageRequest.of(page, size, Sort.by(sortBy).descending()));
            if (requesters.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "isActive:".concat(String.valueOf(isActive))
                                .concat(",createdBefore:")
                                .concat(String.valueOf(createdBefore.toInstant().toEpochMilli()))
                                .concat(",createdAfter:")
                                .concat(String.valueOf(createdAfter.toInstant().toEpochMilli())));
            }

            return GenericPagedModel.<IssueRequesterModel>builder()
                    .totalElements(requesters.getTotalElements())
                    .numberOfElements(requesters.getNumberOfElements())
                    .totalPages(requesters.getTotalPages())
                    .content(requesters.getContent())
                    .build();

        } catch (final ConstraintViolationException | DataIntegrityViolationException | TransactionSystemException ex) {
            throw ExceptionMapperUtil.mapPersistenceException(ex);
        }
    }

    public GenericPagedModel<IssueRequesterModel> findAllByCreatedBeforeAndCreatedAfter(
            Date createdBefore, Date createdAfter, int page, int size, String sortBy, SortDirection sortDirection) {
        try {
            val requesters = sortDirection.equals(SortDirection.Ascending)
                    ? issueRequesterRepository.findAllByCreatedBeforeAndCreatedAfter(createdBefore, createdAfter,
                    PageRequest.of(page, size, Sort.by(sortBy).ascending()))
                    : issueRequesterRepository.findAllByCreatedBeforeAndCreatedAfter(createdBefore, createdAfter,
                    PageRequest.of(page, size, Sort.by(sortBy).descending()));
            if (requesters.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "createdBefore:".concat(String.valueOf(createdBefore.toInstant().toEpochMilli()))
                                .concat(",createdAfter:")
                                .concat(String.valueOf(createdAfter.toInstant().toEpochMilli())));
            }

            return GenericPagedModel.<IssueRequesterModel>builder()
                    .totalElements(requesters.getTotalElements())
                    .numberOfElements(requesters.getNumberOfElements())
                    .totalPages(requesters.getTotalPages())
                    .content(requesters.getContent())
                    .build();

        } catch (final ConstraintViolationException | DataIntegrityViolationException | TransactionSystemException ex) {
            throw ExceptionMapperUtil.mapPersistenceException(ex);
        }
    }

    @Transactional
    public IssueRequesterModel toggleActivation(Integer id) {
        try {
            val requester = getRequester(id);

            requester.setIsActive(!requester.getIsActive());

            return issueRequesterRepository.save(requester);

        } catch (final ConstraintViolationException | DataIntegrityViolationException | TransactionSystemException ex) {
            throw ExceptionMapperUtil.mapPersistenceException(ex);
        }
    }

    public Boolean isExistsAndActive(Integer id, Boolean isActive) {
        try {
            return issueRequesterRepository.existsByIdAndIsActive(id, isActive);

        } catch (final ConstraintViolationException | DataIntegrityViolationException | TransactionSystemException ex) {
            throw ExceptionMapperUtil.mapPersistenceException(ex);
        }
    }

    @Transactional
    public IssueRequesterModel save(IssueRequesterModel model) {
        try {
            val id = model.getId();

            if (id == null || id <= 0) {
                model.setId(null);

                if (issueRequesterRepository.existsAllByEmail(model.getEmail())) {
                    throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                            "email:".concat(model.getEmail())
                                    .concat(",isActive:").concat(String.valueOf(model.getIsActive())));
                }

                return issueRequesterRepository.saveAndFlush(model);
            }

            if (!issueRequesterRepository.existsById(id)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "requesterId:".concat(id.toString()));
            }

            return issueRequesterRepository.saveAndFlush(model); // UPDATE

        } catch (final ConstraintViolationException | DataIntegrityViolationException | TransactionSystemException ex) {
            throw ExceptionMapperUtil.mapPersistenceException(ex);
        }
    }

    @Transactional
    public IssueRequesterModel hardDelete(Integer id) {
        try {
            val requesterToHardDelete = getRequester(id);

            issueRequesterRepository.delete(requesterToHardDelete);

            return requesterToHardDelete;

        } catch (final ConstraintViolationException | DataIntegrityViolationException | TransactionSystemException ex) {
            throw ExceptionMapperUtil.mapPersistenceException(ex);
        }
    }

    // WARNING: TEST PURPOSES ONLY! DO NOT IMPLEMENT AN ENDPOINT (at least for now) >>
    @Transactional
    public void hardDeleteAll() {
        try {
            issueRequesterRepository.deleteAll();

        } catch (final ConstraintViolationException | DataIntegrityViolationException | TransactionSystemException ex) {
            throw ExceptionMapperUtil.mapPersistenceException(ex);
        }
    }
    // << WARNING: TEST PURPOSES ONLY! DO NOT IMPLEMENT AN ENDPOINT (at least for now)


    private IssueRequesterModel getRequester(Integer id) {
        try {
            val requester = issueRequesterRepository.findById(id);
            if (requester.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "requesterId:".concat(id.toString()));
            }

            return requester.get();

        } catch (final ConstraintViolationException | DataIntegrityViolationException | TransactionSystemException ex) {
            throw ExceptionMapperUtil.mapPersistenceException(ex);
        }
    }

    private IssueRequesterModel getRequester(Integer id, boolean isActive) {
        try {
            val requester = issueRequesterRepository.findByIdAndIsActive(id, isActive);
            if (requester.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "requesterId:".concat(id.toString())
                                .concat(",isActive:").concat(String.valueOf(isActive)));
            }

            return requester.get();

        } catch (final ConstraintViolationException | DataIntegrityViolationException | TransactionSystemException ex) {
            throw ExceptionMapperUtil.mapPersistenceException(ex);
        }
    }
}
