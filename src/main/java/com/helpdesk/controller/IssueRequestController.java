package com.helpdesk.controller;

import com.google.protobuf.NullValue;
import com.helpdesk.data.model.IssueRequestModel;
import com.helpdesk.data.service.IssueRequestService;
import com.helpdesk.data.service.IssueRequesterService;
import com.helpdesk.data.util.GenericPagedModel;
import com.helpdesk.protoGen.*;
import com.helpdesk.util.SortDirection;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.Date;
import java.util.Objects;

import static com.helpdesk.controller.util.Parsers.*;
import static java.util.stream.Collectors.toList;

@RestController
@Slf4j
public class IssueRequestController {
    final IssueRequestService issueRequestService;

    final IssueRequesterService issueRequesterService;

    @Autowired
    public IssueRequestController(IssueRequestService issueRequestService,
                                  IssueRequesterService issueRequesterService) {
        this.issueRequestService = issueRequestService;
        this.issueRequesterService = issueRequesterService;
    }

    @RequestMapping(value = "/v1/issue_requests/{id}", method = RequestMethod.GET)
    private ResponseEntity<IssueRequest> getIssueRequestByIdV1(
            @PathVariable String id,
            @RequestParam(defaultValue = "") String issueRequestIsSolved) {
        log.info("Calling: getIssueRequestByIdV1 >> ".concat(id));

        val issueRequest = StringUtils.isBlank(issueRequestIsSolved)
                ? issueRequestService.findById(getInteger(id, "id"))
                : issueRequestService.findById(getInteger(id, "id")
                , getBoolean(issueRequestIsSolved, "issueRequestIsSolved"));

        return ResponseEntity.ok(mapIssueRequest(issueRequest));
    }

    @RequestMapping(value = "/v1/issue_requests/find_all/{requesterId}", method = RequestMethod.GET)
    private ResponseEntity<PagedData> getAllIssueRequestsByRequesterId(
            @PathVariable String requesterId,
            @RequestParam(defaultValue = "") String createdBefore,
            @RequestParam(defaultValue = "") String createdAfter,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "created") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        if (StringUtils.isNotBlank(createdBefore) && StringUtils.isNotBlank(createdAfter)) {
            log.info("Calling: getAllIssueRequestsByRequesterId >> Requester Id: ".concat(requesterId)
                    .concat(" | Created Before: ").concat(createdBefore)
                    .concat(" | Created After: ").concat(createdAfter));

            val result = issueRequestService
                    .findAllByRequesterIdAndCreatedBeforeAndCreatedAfter(getInteger(requesterId, "requesterId"),
                            new Date(getLong(createdBefore, "createdBefore")),
                            new Date(getLong(createdAfter, "createdAfter")),
                            pageNo, pageSize, sortBy, SortDirection.getSortDirection(sortDir));

            return ResponseEntity.ok(mapPaged(result));
        }

        log.info("Calling: getAllIssueRequestsByRequesterId >> Requester Id: ".concat(requesterId));

        val result = issueRequestService
                .findAllByRequesterId(getInteger(requesterId, "requesterId"), pageNo, pageSize, sortBy
                        , SortDirection.getSortDirection(sortDir));

        return ResponseEntity.ok(mapPaged(result));
    }

    @RequestMapping(value = "/v1/issue_requests/find_all", method = RequestMethod.GET)
    private ResponseEntity<PagedData> getAllIssueRequestsByCreatedBeforeAndCreatedAfter(
            @RequestParam(defaultValue = "") String createdBefore,
            @RequestParam(defaultValue = "") String createdAfter,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "created") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        if (StringUtils.isNotBlank(createdBefore) && StringUtils.isNotBlank(createdAfter)) {
            log.info("Calling: getAllIssueRequestsByCreatedBeforeAndCreatedAfter >> Created Before: ".concat(createdBefore)
                    .concat(" | Created After: ").concat(createdAfter));

            val result = issueRequestService
                    .findAllByCreatedBeforeAndCreatedAfter(new Date(getLong(createdBefore, "createdBefore")),
                            new Date(getLong(createdAfter, "createdAfter")),
                            pageNo, pageSize, sortBy, SortDirection.getSortDirection(sortDir));

            return ResponseEntity.ok(mapPaged(result));
        }

        log.info("Calling: getAllIssueRequestsByCreatedBeforeAndCreatedAfter");

        val result = issueRequestService
                .findAll(pageNo, pageSize, sortBy, SortDirection.getSortDirection(sortDir));

        return ResponseEntity.ok(mapPaged(result));
    }

    @RequestMapping(value = "/v1/issue_requests/find_all_solved/{isSolved}", method = RequestMethod.GET)
    private ResponseEntity<PagedData> getAllIssueRequestsByCreatedBeforeAndCreatedAfterAndIsSolved(
            @PathVariable String isSolved,
            @RequestParam(defaultValue = "") String createdBefore,
            @RequestParam(defaultValue = "") String createdAfter,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "created") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        log.info("Calling: getAllIssueRequestsByCreatedBeforeAndCreatedAfterAndIsSolved >> Created Before: "
                .concat(createdBefore)
                .concat(" | Created After: ").concat(createdAfter)
                .concat(" | Is Solved: ").concat(isSolved));

        val result = issueRequestService
                .findAllByCreatedBeforeAndCreatedAfterAndIsSolved(
                        new Date(getLong(createdBefore, "createdBefore")),
                        new Date(getLong(createdAfter, "createdAfter")),
                        getBoolean(isSolved, "isSolved"),
                        pageNo, pageSize, sortBy, SortDirection.getSortDirection(sortDir));

        return ResponseEntity.ok(mapPaged(result));
    }

    @RequestMapping(value = "/v1/issue_requests/find_all_solved", method = RequestMethod.GET)
    private ResponseEntity<PagedData> getAllIssueRequestsBySolvedBeforeAndSolvedAfter(
            @RequestParam(defaultValue = "") String solvedBefore,
            @RequestParam(defaultValue = "") String solvedAfter,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "solved") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        log.info("Calling: getAllIssueRequestsBySolvedBeforeAndSolvedAfter >> Solved Before: ".concat(solvedBefore)
                .concat(" | Solved After: ").concat(solvedAfter));

        val result = issueRequestService
                .findAllBySolvedBeforeAndSolvedAfter(
                        new Date(getLong(solvedBefore, "solvedBefore")),
                        new Date(getLong(solvedAfter, "solvedAfter")),
                        pageNo, pageSize, sortBy, SortDirection.getSortDirection(sortDir));

        return ResponseEntity.ok(mapPaged(result));
    }

    @RequestMapping(value = "/v1/issue_requests/solve/{id}", method = RequestMethod.PUT)
    private ResponseEntity<IssueRequest> putSolveIssueRequestV1(@PathVariable String id) {
        log.info("Calling: putSolveIssueRequestV1 >> ".concat(id));

        val result = issueRequestService.solveIssue(getInteger(id, "id"));

        return ResponseEntity.ok(mapIssueRequest(result));
    }

    @RequestMapping(value = "/v1/issue_requests/delete/{id}", method = RequestMethod.DELETE)
    private ResponseEntity<IssueRequest> deleteIssueRequestV1(@PathVariable String id) {
        log.info("Calling: deleteIssueRequestV1 >> ".concat(id));

        val result = issueRequestService.hardDelete(getInteger(id, "id"));

        return ResponseEntity.ok(mapIssueRequest(result));
    }


    @RequestMapping(value = "/v1/issue_requests/save", method = RequestMethod.POST)
    private ResponseEntity<IssueRequest> saveIssueRequestV1(@RequestBody IssueRequest issueRequest) {
        log.info("Calling: saveIssueRequestV1 >> ".concat(issueRequest.toString()));

        if (!issueRequesterService.isExistsAndActive(issueRequest.getRequesterId(), true)) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                    "requesterId:".concat(String.valueOf(issueRequest.getRequesterId()))
                            .concat(",isActive:true"));
        }

        val requester = issueRequesterService.findById(issueRequest.getRequesterId(), true);

        val saved = issueRequestService.save(IssueRequestModel
                .builder()
                .id(issueRequest.getId())
                .requester(requester)
                .body(issueRequest.getBody())
                .build());

        return ResponseEntity.ok(mapIssueRequest(saved));
    }

    private PagedData mapPaged(GenericPagedModel<IssueRequestModel> model) {
        return PagedData.newBuilder()
                .setTotalElements(model.getTotalElements())
                .setNumberOfElements(model.getNumberOfElements())
                .setTotalPages(model.getTotalPages())
                .setIssueRequests(mapIssueRequests(model.getContent()))
                .build();
    }

    private IssueRequests mapIssueRequests(Collection<IssueRequestModel> models) {
        return IssueRequests.newBuilder()
                .addAllIssueRequests(models
                        .stream()
                        .map(this::mapIssueRequest)
                        .collect(toList()))
                .build();
    }

    private IssueRequest mapIssueRequest(IssueRequestModel model) {
        return IssueRequest.newBuilder()
                .setId(model.getId())
                .setRequesterId(model.getRequester().getId())
                .setBody(model.getBody())
                .setIsSolved(Objects.nonNull(model.getIsSolved())
                        ? NullableBoolean.newBuilder().setData(model.getIsSolved()).build()
                        : NullableBoolean.newBuilder().setNull(NullValue.NULL_VALUE).build())
                .setCreated(Objects.nonNull(model.getCreated())
                        ? NullableInt64.newBuilder().setData(model.getCreated()
                        .toInstant().toEpochMilli()).build()
                        : NullableInt64.newBuilder().setNull(NullValue.NULL_VALUE).build())
                .setSolved(Objects.nonNull(model.getSolved())
                        ? NullableInt64.newBuilder().setData(model.getSolved()
                        .toInstant().toEpochMilli()).build()
                        : NullableInt64.newBuilder().setNull(NullValue.NULL_VALUE).build())
                .build();
    }
}
