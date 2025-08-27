package com.helpdesk.data.service;

import com.helpdesk.data.model.IssueResponseModel;
import com.helpdesk.data.repository.IssueResponseRepository;
import com.helpdesk.data.util.ExceptionMapperUtil;
import com.helpdesk.data.util.GenericPagedModel;
import com.helpdesk.data.util.SortDirection;
import jakarta.validation.ConstraintViolationException;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;

@Service
public class IssueResponseService {
    final IssueResponseRepository issueResponseRepository;

    @Autowired
    public IssueResponseService(IssueResponseRepository issueResponseRepository) {
        this.issueResponseRepository = issueResponseRepository;
    }

    public IssueResponseModel findById(Integer id) {
        return getResponse(id);
    }

    public GenericPagedModel<IssueResponseModel> findAll(
            int page, int size, String sortBy, SortDirection sortDirection) {
        try {
            val requesters = sortDirection.equals(SortDirection.Ascending)
                    ? issueResponseRepository.findAll(PageRequest.of(page, size, Sort.by(sortBy).ascending()))
                    : issueResponseRepository.findAll(PageRequest.of(page, size, Sort.by(sortBy).descending()));
            if (requesters.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No data");
            }

            return GenericPagedModel.<IssueResponseModel>builder()
                    .totalElements(requesters.getTotalElements())
                    .numberOfElements(requesters.getNumberOfElements())
                    .totalPages(requesters.getTotalPages())
                    .content(requesters.getContent())
                    .build();

        } catch (final ConstraintViolationException | DataIntegrityViolationException | TransactionSystemException ex) {
            throw ExceptionMapperUtil.mapPersistenceException(ex);
        }
    }

    public GenericPagedModel<IssueResponseModel> findAllByCreatedBeforeAndCreatedAfter(
            Date createdBefore, Date createdAfter, int page, int size, String sortBy, SortDirection sortDirection) {
        try {
            val responses = sortDirection.equals(SortDirection.Ascending)
                    ? issueResponseRepository.findAllByCreatedBeforeAndCreatedAfter(createdBefore, createdAfter,
                    PageRequest.of(page, size, Sort.by(sortBy).ascending()))
                    : issueResponseRepository.findAllByCreatedBeforeAndCreatedAfter(createdBefore, createdAfter,
                    PageRequest.of(page, size, Sort.by(sortBy).descending()));
            if (responses.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "createdBefore:".concat(String.valueOf(createdBefore.toInstant().toEpochMilli()))
                                .concat(",createdAfter:")
                                .concat(String.valueOf(createdAfter.toInstant().toEpochMilli())));
            }

            return GenericPagedModel.<IssueResponseModel>builder()
                    .totalElements(responses.getTotalElements())
                    .numberOfElements(responses.getNumberOfElements())
                    .totalPages(responses.getTotalPages())
                    .content(responses.getContent())
                    .build();

        } catch (final ConstraintViolationException | DataIntegrityViolationException | TransactionSystemException ex) {
            throw ExceptionMapperUtil.mapPersistenceException(ex);
        }
    }

    public GenericPagedModel<IssueResponseModel> findAllByRequestId(
            Integer requestId, int page, int size, String sortBy, SortDirection sortDirection) {
        try {
            val responses = sortDirection.equals(SortDirection.Ascending)
                    ? issueResponseRepository.findAllByRequestId(requestId,
                    PageRequest.of(page, size, Sort.by(sortBy).ascending()))
                    : issueResponseRepository.findAllByRequestId(requestId,
                    PageRequest.of(page, size, Sort.by(sortBy).descending()));
            if (responses.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "requestId:".concat(requestId.toString()));
            }

            return GenericPagedModel.<IssueResponseModel>builder()
                    .totalElements(responses.getTotalElements())
                    .numberOfElements(responses.getNumberOfElements())
                    .totalPages(responses.getTotalPages())
                    .content(responses.getContent())
                    .build();

        } catch (final ConstraintViolationException | DataIntegrityViolationException | TransactionSystemException ex) {
            throw ExceptionMapperUtil.mapPersistenceException(ex);
        }
    }

    public GenericPagedModel<IssueResponseModel> findAllByRequestIdAndCreatedBeforeAndCreatedAfter(
            Integer requestId, Date createdBefore, Date createdAfter, int page, int size, String sortBy,
            SortDirection sortDirection) {
        try {
            val responses = sortDirection.equals(SortDirection.Ascending)
                    ? issueResponseRepository.findAllByRequestIdAndCreatedBeforeAndCreatedAfter(
                    requestId, createdBefore, createdAfter,
                    PageRequest.of(page, size, Sort.by(sortBy).ascending()))
                    : issueResponseRepository.findAllByRequestIdAndCreatedBeforeAndCreatedAfter(
                    requestId, createdBefore, createdAfter,
                    PageRequest.of(page, size, Sort.by(sortBy).descending()));
            if (responses.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "requestId:".concat(requestId.toString())
                                .concat(",createdBefore:")
                                .concat(String.valueOf(createdBefore.toInstant().toEpochMilli()))
                                .concat(",createdAfter:")
                                .concat(String.valueOf(createdAfter.toInstant().toEpochMilli())));
            }

            return GenericPagedModel.<IssueResponseModel>builder()
                    .totalElements(responses.getTotalElements())
                    .numberOfElements(responses.getNumberOfElements())
                    .totalPages(responses.getTotalPages())
                    .content(responses.getContent())
                    .build();

        } catch (final ConstraintViolationException | DataIntegrityViolationException | TransactionSystemException ex) {
            throw ExceptionMapperUtil.mapPersistenceException(ex);
        }
    }

    public GenericPagedModel<IssueResponseModel> findAllByRequesterId(
            Integer requesterId, int page, int size, String sortBy, SortDirection sortDirection) {
        try {
            val responses = sortDirection.equals(SortDirection.Ascending)
                    ? issueResponseRepository.findAllByRequesterId(requesterId,
                    PageRequest.of(page, size, Sort.by(sortBy).ascending()))
                    : issueResponseRepository.findAllByRequesterId(requesterId,
                    PageRequest.of(page, size, Sort.by(sortBy).descending()));
            if (responses.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "requesterId:".concat(requesterId.toString()));
            }

            return GenericPagedModel.<IssueResponseModel>builder()
                    .totalElements(responses.getTotalElements())
                    .numberOfElements(responses.getNumberOfElements())
                    .totalPages(responses.getTotalPages())
                    .content(responses.getContent())
                    .build();

        } catch (final ConstraintViolationException | DataIntegrityViolationException | TransactionSystemException ex) {
            throw ExceptionMapperUtil.mapPersistenceException(ex);
        }
    }

    public GenericPagedModel<IssueResponseModel> findAllByRequesterIdAndCreatedBeforeAndCreatedAfter(
            Integer requesterId, Date createdBefore, Date createdAfter, int page, int size, String sortBy,
            SortDirection sortDirection) {
        try {
            val responses = sortDirection.equals(SortDirection.Ascending)
                    ? issueResponseRepository.findAllByRequesterIdAndCreatedBeforeAndCreatedAfter(
                    requesterId, createdBefore, createdAfter,
                    PageRequest.of(page, size, Sort.by(sortBy).ascending()))
                    : issueResponseRepository.findAllByRequesterIdAndCreatedBeforeAndCreatedAfter(
                    requesterId, createdBefore, createdAfter,
                    PageRequest.of(page, size, Sort.by(sortBy).descending()));
            if (responses.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "requesterId:".concat(requesterId.toString())
                                .concat(",createdBefore:")
                                .concat(String.valueOf(createdBefore.toInstant().toEpochMilli()))
                                .concat(",createdAfter:")
                                .concat(String.valueOf(createdAfter.toInstant().toEpochMilli())));
            }

            return GenericPagedModel.<IssueResponseModel>builder()
                    .totalElements(responses.getTotalElements())
                    .numberOfElements(responses.getNumberOfElements())
                    .totalPages(responses.getTotalPages())
                    .content(responses.getContent())
                    .build();

        } catch (final ConstraintViolationException | DataIntegrityViolationException | TransactionSystemException ex) {
            throw ExceptionMapperUtil.mapPersistenceException(ex);
        }
    }

    public IssueResponseModel save(IssueResponseModel model) {
        try {
            val id = model.getId();
            if (id == null || id <= 0) {
                model.setId(null);
                return issueResponseRepository.save(model);
            }

            if (!issueResponseRepository.existsById(id)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "responseId:".concat(id.toString()));
            }

            return issueResponseRepository.save(model);

        } catch (final ConstraintViolationException | DataIntegrityViolationException | TransactionSystemException ex) {
            throw ExceptionMapperUtil.mapPersistenceException(ex);
        }
    }

    public IssueResponseModel hardDelete(Integer id) {
        try {
            val responseToHardDelete = getResponse(id);

            issueResponseRepository.delete(responseToHardDelete);

            return responseToHardDelete;

        } catch (final ConstraintViolationException | DataIntegrityViolationException | TransactionSystemException ex) {
            throw ExceptionMapperUtil.mapPersistenceException(ex);
        }
    }

    // WARNING: TEST PURPOSES ONLY! DO NOT IMPLEMENT AN ENDPOINT (at least for now) >>
    public void hardDeleteAll() {
        try {
            issueResponseRepository.deleteAll();

        } catch (final ConstraintViolationException | DataIntegrityViolationException | TransactionSystemException ex) {
            throw ExceptionMapperUtil.mapPersistenceException(ex);
        }
    }
    // << WARNING: TEST PURPOSES ONLY! DO NOT IMPLEMENT AN ENDPOINT (at least for now)

    private IssueResponseModel getResponse(Integer id) {
        try {
            val request = issueResponseRepository.findById(id);
            if (request.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "responseId:".concat(id.toString()));
            }

            return request.get();

        } catch (final ConstraintViolationException | DataIntegrityViolationException | TransactionSystemException ex) {
            throw ExceptionMapperUtil.mapPersistenceException(ex);
        }
    }
}
