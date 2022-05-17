package com.helpdesk.data.service;

import com.helpdesk.data.model.IssueRequesterModel;
import com.helpdesk.data.repository.IssueRequesterRepository;
import com.helpdesk.data.util.GenericPagedModel;
import com.helpdesk.data.validator.IssueRequesterValidator;
import com.helpdesk.util.SortDirection;
import lombok.val;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.Objects;

@Service
public class IssueRequesterService {
    final IssueRequesterRepository issueRequesterRepository;

    final IssueRequesterValidator issueRequesterValidator;

    @Autowired
    public IssueRequesterService(IssueRequesterRepository issueRequesterRepository,
                                 IssueRequesterValidator issueRequesterValidator) {
        this.issueRequesterRepository = issueRequesterRepository;
        this.issueRequesterValidator = issueRequesterValidator;
    }

    public IssueRequesterModel findById(Integer id) {
        return getRequester(id);
    }

    public IssueRequesterModel findById(Integer id, boolean isActive) {
        return getRequester(id, isActive);
    }

    public GenericPagedModel<IssueRequesterModel> findAll(int page, int size,
                                                          String sortBy, SortDirection sortDirection) {
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

        } catch (final DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ExceptionUtils.getStackTrace(ex));
        }
    }

    public GenericPagedModel<IssueRequesterModel> findAllByFullNameContainingIgnoreCase(String fullName,
                                                                                        int page, int size,
                                                                                        String sortBy,
                                                                                        SortDirection sortDirection) {
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

        } catch (final DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ExceptionUtils.getStackTrace(ex));
        }
    }

    public GenericPagedModel<IssueRequesterModel> findAllByFullNameContainingIgnoreCaseAndCreatedBeforeAndCreatedAfter
            (String fullName, Date createdBefore, Date createdAfter, int page, int size,
             String sortBy, SortDirection sortDirection) {
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

        } catch (final DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ExceptionUtils.getStackTrace(ex));
        }
    }

    public GenericPagedModel<IssueRequesterModel> findAllByEmailContainingIgnoreCase(String email, int page, int size,
                                                                                     String sortBy, SortDirection sortDirection) {
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

        } catch (final DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ExceptionUtils.getStackTrace(ex));
        }
    }

    public GenericPagedModel<IssueRequesterModel> findAllByEmailContainingIgnoreCaseAndCreatedBeforeAndCreatedAfter
            (String email, Date createdBefore, Date createdAfter, int page, int size,
             String sortBy, SortDirection sortDirection) {
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
                                .concat(",createdAfter")
                                .concat(String.valueOf(createdAfter.toInstant().toEpochMilli())));
            }

            return GenericPagedModel.<IssueRequesterModel>builder()
                    .totalElements(requesters.getTotalElements())
                    .numberOfElements(requesters.getNumberOfElements())
                    .totalPages(requesters.getTotalPages())
                    .content(requesters.getContent())
                    .build();

        } catch (final DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ExceptionUtils.getStackTrace(ex));
        }
    }

    public GenericPagedModel<IssueRequesterModel> findAllByCreatedBeforeAndCreatedAfterAndIsActive
            (Date createdBefore, Date createdAfter, boolean isActive, int page, int size,
             String sortBy, SortDirection sortDirection) {
        try {
            val requesters = sortDirection.equals(SortDirection.Ascending)
                    ? issueRequesterRepository.findAllByCreatedBeforeAndCreatedAfterAndIsActive(
                    createdBefore, createdAfter,isActive,
                    PageRequest.of(page, size, Sort.by(sortBy).ascending()))
                    : issueRequesterRepository.findAllByCreatedBeforeAndCreatedAfterAndIsActive(
                    createdBefore, createdAfter,isActive,
                    PageRequest.of(page, size, Sort.by(sortBy).descending()));
            if (requesters.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "isActive:".concat(String.valueOf(isActive))
                                .concat(",createdBefore:")
                                .concat(String.valueOf(createdBefore.toInstant().toEpochMilli()))
                                .concat(",createdAfter")
                                .concat(String.valueOf(createdAfter.toInstant().toEpochMilli())));
            }

            return GenericPagedModel.<IssueRequesterModel>builder()
                    .totalElements(requesters.getTotalElements())
                    .numberOfElements(requesters.getNumberOfElements())
                    .totalPages(requesters.getTotalPages())
                    .content(requesters.getContent())
                    .build();

        } catch (final DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ExceptionUtils.getStackTrace(ex));
        }
    }

    public GenericPagedModel<IssueRequesterModel> findAllByCreatedBeforeAndCreatedAfter(Date createdBefore,
                                                                                        Date createdAfter,
                                                                                        int page, int size,
                                                                                        String sortBy,
                                                                                        SortDirection sortDirection) {
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

        } catch (final DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ExceptionUtils.getStackTrace(ex));
        }
    }

    public IssueRequesterModel toggleActivation(Integer id) {
        try {
            val requester = getRequester(id);

            requester.setIsActive(!requester.getIsActive());

            return issueRequesterRepository.save(requester);

        } catch (final DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ExceptionUtils.getStackTrace(ex));
        }
    }

    public IssueRequesterModel save(IssueRequesterModel model) {
        try {
            issueRequesterValidator.validate(model);

            if (Objects.isNull(model.getId()) && issueRequesterRepository.existsAllByEmail(model.getEmail())) {
                throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                        "email:".concat(model.getEmail())
                                .concat(",isActive:").concat(String.valueOf(model.getIsActive())));
            }

            return issueRequesterRepository.save(model);

        } catch (final DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ExceptionUtils.getStackTrace(ex));
        }
    }

    public IssueRequesterModel hardDelete(Integer id) {
        try {
            val requesterToHardDelete = getRequester(id);

            issueRequesterRepository.delete(requesterToHardDelete);

            return requesterToHardDelete;

        } catch (final DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ExceptionUtils.getStackTrace(ex));
        }
    }

    // WARNING: TEST PURPOSES ONLY! DO NOT IMPLEMENT AN ENDPOINT (at least for now) >>
    public void hardDeleteAll() {
        try {
            issueRequesterRepository.deleteAll();

        } catch (final DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ExceptionUtils.getStackTrace(ex));
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

        } catch (final DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ExceptionUtils.getStackTrace(ex));
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

        } catch (final DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ExceptionUtils.getStackTrace(ex));
        }
    }
}
