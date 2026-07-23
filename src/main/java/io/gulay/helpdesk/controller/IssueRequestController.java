package io.gulay.helpdesk.controller;

import com.google.protobuf.NullValue;
import io.gulay.helpdesk.data.model.IssueRequestModel;
import io.gulay.helpdesk.data.service.IssueRequestService;
import io.gulay.helpdesk.data.service.IssueRequesterService;
import io.gulay.helpdesk.data.util.GenericPagedModel;
import io.gulay.helpdesk.protoGen.*;
import io.gulay.helpdesk.data.util.SortDirection;
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

import static io.gulay.helpdesk.controller.util.Parsers.*;
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

    @RequestMapping(value = {"/api/v1/issue-requests/{id}", "/v1/issue_requests/{id}"}, method = RequestMethod.GET)
    private ResponseEntity<IssueRequest> getIssueRequestByIdV1(
            @PathVariable String id,
            @RequestParam(defaultValue = "") String issueRequestIsSolved) {
        log.info("Calling: getIssueRequestByIdV1 >> ".concat(id));

        val issueRequest = StringUtils.isBlank(issueRequestIsSolved)
                ? issueRequestService.findById(tryParseInteger(id, "id"))
                : issueRequestService.findById(tryParseInteger(id, "id")
                , tryParseBoolean(issueRequestIsSolved, "issueRequestIsSolved"));

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
                    .findAllByRequesterIdAndCreatedBeforeAndCreatedAfter(tryParseInteger(requesterId, "requesterId"),
                            new Date(tryParseLong(createdBefore, "createdBefore")),
                            new Date(tryParseLong(createdAfter, "createdAfter")),
                            pageNo, pageSize, sortBy, SortDirection.of(sortDir));

            return ResponseEntity.ok(mapPaged(result));
        }

        log.info("Calling: getAllIssueRequestsByRequesterId >> Requester Id: ".concat(requesterId));

        val result = issueRequestService
                .findAllByRequesterId(tryParseInteger(requesterId, "requesterId"), pageNo, pageSize, sortBy
                        , SortDirection.of(sortDir));

        return ResponseEntity.ok(mapPaged(result));
    }

    @RequestMapping(value = {"/api/v1/issue-requests", "/v1/issue_requests/find_all"}, method = RequestMethod.GET)
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
                    .findAllByCreatedBeforeAndCreatedAfter(new Date(tryParseLong(createdBefore, "createdBefore")),
                            new Date(tryParseLong(createdAfter, "createdAfter")),
                            pageNo, pageSize, sortBy, SortDirection.of(sortDir));

            return ResponseEntity.ok(mapPaged(result));
        }

        log.info("Calling: getAllIssueRequestsByCreatedBeforeAndCreatedAfter");

        val result = issueRequestService
                .findAll(pageNo, pageSize, sortBy, SortDirection.of(sortDir));

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
                        new Date(tryParseLong(createdBefore, "createdBefore")),
                        new Date(tryParseLong(createdAfter, "createdAfter")),
                        tryParseBoolean(isSolved, "isSolved"),
                        pageNo, pageSize, sortBy, SortDirection.of(sortDir));

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
                        new Date(tryParseLong(solvedBefore, "solvedBefore")),
                        new Date(tryParseLong(solvedAfter, "solvedAfter")),
                        pageNo, pageSize, sortBy, SortDirection.of(sortDir));

        return ResponseEntity.ok(mapPaged(result));
    }

    @RequestMapping(value = {"/api/v1/issue-requests/{id}/resolution", "/v1/issue_requests/solve/{id}"}, method = RequestMethod.PUT)
    private ResponseEntity<IssueRequest> putSolveIssueRequestV1(@PathVariable String id) {
        log.info("Calling: putSolveIssueRequestV1 >> ".concat(id));

        val result = issueRequestService.solveIssue(tryParseInteger(id, "id"));

        return ResponseEntity.ok(mapIssueRequest(result));
    }

    @RequestMapping(value = {"/api/v1/issue-requests/{id}", "/v1/issue_requests/delete/{id}"}, method = RequestMethod.DELETE)
    private ResponseEntity<IssueRequest> deleteIssueRequestV1(@PathVariable String id) {
        log.info("Calling: deleteIssueRequestV1 >> ".concat(id));

        val result = issueRequestService.hardDelete(tryParseInteger(id, "id"));

        return ResponseEntity.ok(mapIssueRequest(result));
    }


    @RequestMapping(value = {"/api/v1/issue-requests", "/v1/issue_requests/save"}, method = RequestMethod.POST)
    private ResponseEntity<IssueRequest> saveIssueRequestV1(@RequestBody IssueRequest issueRequest) {
        log.info("Saving issue request id={} requesterId={}", issueRequest.getId(), issueRequest.getRequesterId());

        if (!issueRequesterService.isExistsAndActive(issueRequest.getRequesterId(), true)) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                    "requesterId:".concat(String.valueOf(issueRequest.getRequesterId()))
                            .concat(",isActive:true"));
        }

        val requester = issueRequesterService.findById(issueRequest.getRequesterId(), true);

        val saved = issueRequestService.save(IssueRequestModel
                .builder()
                .id(issueRequest.getId())
                .isSolved(issueRequest.getIsSolved().getData())
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
