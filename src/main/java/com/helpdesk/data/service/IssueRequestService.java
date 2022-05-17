package com.helpdesk.data.service;

import com.helpdesk.data.model.IssueRequestModel;
import com.helpdesk.data.repository.IssueRequestRepository;
import com.helpdesk.data.util.GenericPagedModel;
import com.helpdesk.data.validator.IssueRequestValidator;
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

import java.util.Calendar;
import java.util.Date;


// TODO: ADD IS SOLVED AS SEPARATE METHOD BOTH HERE AND IN REPO!
@Service
public class IssueRequestService {
    final IssueRequestRepository issueRequestRepository;
    final IssueRequestValidator issueRequestValidator;

    @Autowired
    public IssueRequestService(IssueRequestRepository issueRequestRepository,
                               IssueRequestValidator issueRequestValidator) {
        this.issueRequestRepository = issueRequestRepository;
        this.issueRequestValidator = issueRequestValidator;
    }

    public IssueRequestModel findById(Integer id) {
        return getRequest(id);
    }

    public IssueRequestModel findById(Integer id, boolean isSolved) {
        return getRequest(id, isSolved);
    }

    public GenericPagedModel<IssueRequestModel> findAllByCreatedBeforeAndCreatedAfter(Date createdBefore,
                                                                                      Date createdAfter,
                                                                                      int page, int size,
                                                                                      String sortBy,
                                                                                      SortDirection sortDirection) {
        try {
            val requests = sortDirection.equals(SortDirection.Ascending)
                    ? issueRequestRepository.findAllByCreatedBeforeAndCreatedAfter(createdBefore, createdAfter,
                    PageRequest.of(page, size, Sort.by(sortBy).ascending()))
                    : issueRequestRepository.findAllByCreatedBeforeAndCreatedAfter(createdBefore, createdAfter,
                    PageRequest.of(page, size, Sort.by(sortBy).descending()));
            if (requests.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "createdBefore:".concat(String.valueOf(createdBefore.toInstant().toEpochMilli()))
                                .concat(",createdAfter:")
                                .concat(String.valueOf(createdAfter.toInstant().toEpochMilli())));
            }

            return GenericPagedModel.<IssueRequestModel>builder()
                    .totalElements(requests.getTotalElements())
                    .numberOfElements(requests.getNumberOfElements())
                    .totalPages(requests.getTotalPages())
                    .content(requests.getContent())
                    .build();

        } catch (final DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ExceptionUtils.getStackTrace(ex));
        }
    }

    public GenericPagedModel<IssueRequestModel> findAllByCreatedBeforeAndCreatedAfterAndIsSolved(Date createdBefore,
                                                                                                 Date createdAfter,
                                                                                                 boolean isSolved,
                                                                                                 int page, int size,
                                                                                                 String sortBy,
                                                                                                 SortDirection sortDirection) {
        try {
            val requests = sortDirection.equals(SortDirection.Ascending)
                    ? issueRequestRepository.findAllByCreatedBeforeAndCreatedAfterAndIsSolved(createdBefore, createdAfter,
                    isSolved, PageRequest.of(page, size, Sort.by(sortBy).ascending()))
                    : issueRequestRepository.findAllByCreatedBeforeAndCreatedAfterAndIsSolved(createdBefore, createdAfter,
                    isSolved, PageRequest.of(page, size, Sort.by(sortBy).descending()));
            if (requests.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "createdBefore:".concat(String.valueOf(createdBefore.toInstant().toEpochMilli()))
                                .concat(",createdAfter:")
                                .concat(String.valueOf(createdAfter.toInstant().toEpochMilli()))
                                .concat(",isSolved:").concat(String.valueOf(isSolved)));
            }

            return GenericPagedModel.<IssueRequestModel>builder()
                    .totalElements(requests.getTotalElements())
                    .numberOfElements(requests.getNumberOfElements())
                    .totalPages(requests.getTotalPages())
                    .content(requests.getContent())
                    .build();

        } catch (final DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ExceptionUtils.getStackTrace(ex));
        }
    }

    public GenericPagedModel<IssueRequestModel> findAllBySolvedBeforeAndSolvedAfter(Date solvedBefore,
                                                                                    Date solvedAfter,
                                                                                    int page, int size,
                                                                                    String sortBy,
                                                                                    SortDirection sortDirection) {
        try {
            val requests = sortDirection.equals(SortDirection.Ascending)
                    ? issueRequestRepository.findAllBySolvedBeforeAndSolvedAfter(solvedBefore, solvedAfter,
                    PageRequest.of(page, size, Sort.by(sortBy).ascending()))
                    : issueRequestRepository.findAllBySolvedBeforeAndSolvedAfter(solvedBefore, solvedAfter,
                    PageRequest.of(page, size, Sort.by(sortBy).descending()));
            if (requests.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "solvedBefore:".concat(String.valueOf(solvedBefore.toInstant().toEpochMilli()))
                                .concat(",solvedAfter:")
                                .concat(String.valueOf(solvedAfter.toInstant().toEpochMilli())));
            }

            return GenericPagedModel.<IssueRequestModel>builder()
                    .totalElements(requests.getTotalElements())
                    .numberOfElements(requests.getNumberOfElements())
                    .totalPages(requests.getTotalPages())
                    .content(requests.getContent())
                    .build();

        } catch (final DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ExceptionUtils.getStackTrace(ex));
        }
    }

    public GenericPagedModel<IssueRequestModel> findAllByRequesterId(Integer requesterId, int page, int size,
                                                                     String sortBy,
                                                                     SortDirection sortDirection) {
        try {
            val requests = sortDirection.equals(SortDirection.Ascending)
                    ? issueRequestRepository.findAllByRequesterId(requesterId,
                    PageRequest.of(page, size, Sort.by(sortBy).ascending()))
                    : issueRequestRepository.findAllByRequesterId(requesterId,
                    PageRequest.of(page, size, Sort.by(sortBy).descending()));
            if (requests.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "requesterId:".concat(requesterId.toString()));
            }

            return GenericPagedModel.<IssueRequestModel>builder()
                    .totalElements(requests.getTotalElements())
                    .numberOfElements(requests.getNumberOfElements())
                    .totalPages(requests.getTotalPages())
                    .content(requests.getContent())
                    .build();

        } catch (final DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ExceptionUtils.getStackTrace(ex));
        }
    }

    public GenericPagedModel<IssueRequestModel> findAllByRequesterIdAndCreatedBeforeAndCreatedAfter
            (Integer requesterId, Date createdBefore, Date createdAfter, int page, int size, String sortBy,
             SortDirection sortDirection) {
        try {
            val requests = sortDirection.equals(SortDirection.Ascending)
                    ? issueRequestRepository.findAllByRequesterIdAndCreatedBeforeAndCreatedAfter(requesterId,
                    createdBefore, createdAfter, PageRequest.of(page, size, Sort.by(sortBy).ascending()))
                    : issueRequestRepository.findAllByRequesterIdAndCreatedBeforeAndCreatedAfter(requesterId,
                    createdBefore, createdAfter, PageRequest.of(page, size, Sort.by(sortBy).descending()));
            if (requests.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "requesterId:".concat(requesterId.toString())
                                .concat(",createdBefore:")
                                .concat(String.valueOf(createdBefore.toInstant().toEpochMilli()))
                                .concat(",createdAfter:")
                                .concat(String.valueOf(createdAfter.toInstant().toEpochMilli())));
            }

            return GenericPagedModel.<IssueRequestModel>builder()
                    .totalElements(requests.getTotalElements())
                    .numberOfElements(requests.getNumberOfElements())
                    .totalPages(requests.getTotalPages())
                    .content(requests.getContent())
                    .build();

        } catch (final DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ExceptionUtils.getStackTrace(ex));
        }
    }

    public IssueRequestModel solveIssue(Integer id) {
        try {
            val issueRequest = getRequest(id);

            issueRequest.setIsSolved(true);
            issueRequest.setSolved(Calendar.getInstance().getTime());

            return issueRequestRepository.save(issueRequest);

        } catch (final DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ExceptionUtils.getStackTrace(ex));
        }
    }

    public IssueRequestModel save(IssueRequestModel model) {
        try {
            issueRequestValidator.validate(model);

            if (!issueRequestRepository.existsByRequesterId(model.getRequester().getId())) {
                throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                        "requesterId:".concat(model.getRequester().getId().toString()));
            }
            return issueRequestRepository.save(model);

        } catch (final DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ExceptionUtils.getStackTrace(ex));
        }
    }

    public IssueRequestModel hardDelete(Integer id) {
        try {
            val requestToHardDelete = getRequest(id);

            issueRequestRepository.delete(requestToHardDelete);

            return requestToHardDelete;

        } catch (final DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ExceptionUtils.getStackTrace(ex));
        }
    }

    // WARNING: TEST PURPOSES ONLY! DO NOT IMPLEMENT AN ENDPOINT (at least for now) >>
    public void hardDeleteAll() {
        try {
            issueRequestRepository.deleteAll();

        } catch (final DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ExceptionUtils.getStackTrace(ex));
        }
    }
    // << WARNING: TEST PURPOSES ONLY! DO NOT IMPLEMENT AN ENDPOINT (at least for now)

    private IssueRequestModel getRequest(Integer id) {
        try {
            val request = issueRequestRepository.findById(id);
            if (request.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "requesterId:".concat(id.toString()));
            }

            return request.get();

        } catch (final DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ExceptionUtils.getStackTrace(ex));
        }
    }

    private IssueRequestModel getRequest(Integer id, boolean isSolved) {
        try {
            val request = issueRequestRepository.findByIdAndIsSolved(id, isSolved);
            if (request.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "requestId:".concat(id.toString())
                                .concat(",isSolved:").concat(String.valueOf(isSolved)));
            }

            return request.get();

        } catch (final DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ExceptionUtils.getStackTrace(ex));
        }
    }
}
