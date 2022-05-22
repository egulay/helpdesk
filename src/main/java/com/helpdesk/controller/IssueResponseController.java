package com.helpdesk.controller;

import com.google.protobuf.NullValue;
import com.helpdesk.IssueResponse;
import com.helpdesk.IssueResponses;
import com.helpdesk.NullableInt64;
import com.helpdesk.PagedData;
import com.helpdesk.data.model.IssueResponseModel;
import com.helpdesk.data.service.IssueRequestService;
import com.helpdesk.data.service.IssueRequesterService;
import com.helpdesk.data.service.IssueResponseService;
import com.helpdesk.data.util.GenericPagedModel;
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

import static com.helpdesk.controller.util.Parsers.getInteger;
import static com.helpdesk.controller.util.Parsers.getLong;
import static java.util.stream.Collectors.toList;

@RestController
@Slf4j
public class IssueResponseController {
    final IssueResponseService issueResponseService;

    final IssueRequesterService issueRequesterService;

    final IssueRequestService issueRequestService;

    @Autowired
    public IssueResponseController(IssueResponseService issueResponseService,
                                   IssueRequesterService issueRequesterService,
                                   IssueRequestService issueRequestService) {
        this.issueResponseService = issueResponseService;
        this.issueRequesterService = issueRequesterService;
        this.issueRequestService = issueRequestService;
    }

    @RequestMapping(value = "/v1/issue_responses/{id}", method = RequestMethod.GET)
    private ResponseEntity<IssueResponse> getIssueResponsesByIdV1(@PathVariable String id) {
        log.info("Calling: getIssueResponsesByIdV1 >> ".concat(id));

        val issueResponse = issueResponseService.findById(getInteger(id, "id"));

        return ResponseEntity.ok(mapIssueResponse(issueResponse));
    }

    @RequestMapping(value = "/v1/issue_responses/find_all", method = RequestMethod.GET)
    private ResponseEntity<PagedData> getAllIssueResponsesByCreatedBeforeAndCreatedAfter(
            @RequestParam(defaultValue = "")
            String createdBefore,
            @RequestParam(defaultValue = "")
            String createdAfter,
            @RequestParam(defaultValue = "0")
            int pageNo,
            @RequestParam(defaultValue = "10")
            int pageSize,
            @RequestParam(defaultValue = "created")
            String sortBy,
            @RequestParam(defaultValue = "desc")
            String sortDir) {

        if (StringUtils.isNotBlank(createdBefore) && StringUtils.isNotBlank(createdAfter)) {
            log.info("Calling: getAllIssueResponsesByCreatedBeforeAndCreatedAfter >> Created Before: "
                    .concat(createdBefore)
                    .concat(" | Created After: ").concat(createdAfter));

            val result = issueResponseService
                    .findAllByCreatedBeforeAndCreatedAfter(new Date(getLong(createdBefore, "createdBefore")),
                            new Date(getLong(createdAfter, "createdAfter")),
                            pageNo, pageSize, sortBy, SortDirection.getSortDirection(sortDir));

            return ResponseEntity.ok(mapPaged(result));
        }

        log.info("Calling: getAllIssueRequestersByCreatedBeforeAndCreatedAfter");

        val result = issueResponseService
                .findAll(pageNo, pageSize, sortBy
                        , SortDirection.getSortDirection(sortDir));

        return ResponseEntity.ok(mapPaged(result));
    }

    @RequestMapping(value = "/v1/issue_responses/find_all_by_requester/{requesterId}", method = RequestMethod.GET)
    private ResponseEntity<PagedData> getAllIssueResponsesByRequesterIdAndCreatedBeforeAndCreatedAfter(
            @PathVariable String requesterId,
            @RequestParam(defaultValue = "")
            String createdBefore,
            @RequestParam(defaultValue = "")
            String createdAfter,
            @RequestParam(defaultValue = "0")
            int pageNo,
            @RequestParam(defaultValue = "10")
            int pageSize,
            @RequestParam(defaultValue = "created")
            String sortBy,
            @RequestParam(defaultValue = "desc")
            String sortDir) {

        if (StringUtils.isNotBlank(createdBefore) && StringUtils.isNotBlank(createdAfter)) {
            log.info("Calling: getAllIssueResponsesByRequesterIdAndCreatedBeforeAndCreatedAfter >> Requester Id: "
                    .concat(requesterId)
                    .concat(" | Created Before: ").concat(createdBefore)
                    .concat(" | Created After: ").concat(createdAfter));

            val result = issueResponseService
                    .findAllByRequesterIdAndCreatedBeforeAndCreatedAfter(getInteger(requesterId, "requesterId"),
                            new Date(getLong(createdBefore, "createdBefore")),
                            new Date(getLong(createdAfter, "createdAfter")),
                            pageNo, pageSize, sortBy, SortDirection.getSortDirection(sortDir));

            return ResponseEntity.ok(mapPaged(result));
        }

        log.info("Calling: getAllIssueResponsesByRequesterIdAndCreatedBeforeAndCreatedAfter >> Requester Id: "
                .concat(requesterId));

        val result = issueResponseService
                .findAllByRequesterId(getInteger(requesterId, "requesterId"), pageNo, pageSize, sortBy
                        , SortDirection.getSortDirection(sortDir));

        return ResponseEntity.ok(mapPaged(result));
    }

    @RequestMapping(value = "/v1/issue_responses/find_all_by_request/{requestId}", method = RequestMethod.GET)
    private ResponseEntity<PagedData> getAllIssueResponsesByRequestIdAndCreatedBeforeAndCreatedAfter(
            @PathVariable String requestId,
            @RequestParam(defaultValue = "")
            String createdBefore,
            @RequestParam(defaultValue = "")
            String createdAfter,
            @RequestParam(defaultValue = "0")
            int pageNo,
            @RequestParam(defaultValue = "10")
            int pageSize,
            @RequestParam(defaultValue = "created")
            String sortBy,
            @RequestParam(defaultValue = "desc")
            String sortDir) {

        if (StringUtils.isNotBlank(createdBefore) && StringUtils.isNotBlank(createdAfter)) {
            log.info("Calling: getAllIssueResponsesByRequestIdAndCreatedBeforeAndCreatedAfter >> Request Id: "
                    .concat(requestId)
                    .concat(" | Created Before: ").concat(createdBefore)
                    .concat(" | Created After: ").concat(createdAfter));

            val result = issueResponseService
                    .findAllByRequestIdAndCreatedBeforeAndCreatedAfter(getInteger(requestId, "requestId"),
                            new Date(getLong(createdBefore, "createdBefore")),
                            new Date(getLong(createdAfter, "createdAfter")),
                            pageNo, pageSize, sortBy, SortDirection.getSortDirection(sortDir));

            return ResponseEntity.ok(mapPaged(result));
        }

        log.info("Calling: getAllIssueResponsesByRequestIdAndCreatedBeforeAndCreatedAfter >> Requester Id: "
                .concat(requestId));

        val result = issueResponseService
                .findAllByRequestId(getInteger(requestId, "requestId"), pageNo, pageSize, sortBy
                        , SortDirection.getSortDirection(sortDir));

        return ResponseEntity.ok(mapPaged(result));
    }

    @RequestMapping(value = "/v1/issue_responses/delete/{id}", method = RequestMethod.DELETE)
    private ResponseEntity<IssueResponse> deleteIssueResponseV1(@PathVariable String id) {
        log.info("Calling: deleteIssueResponseV1 >> ".concat(id));

        val result = issueResponseService.hardDelete(getInteger(id, "id"));

        return ResponseEntity.ok(mapIssueResponse(result));
    }


    @RequestMapping(value = "/v1/issue_responses/save", method = RequestMethod.POST)
    private ResponseEntity<IssueResponse> saveIssueResponseV1(@RequestBody IssueResponse issueResponse) {
        log.info("Calling: saveIssueResponseV1 >> ".concat(issueResponse.toString()));

        if (!issueRequesterService.isExistsAndActive(issueResponse.getRequesterId(), true)) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                    "requesterId:".concat(String.valueOf(issueResponse.getRequesterId()))
                            .concat(",isActive:true"));
        }

        if (!issueRequestService.isExists(issueResponse.getRequestId())) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                    "requestId:".concat(String.valueOf(issueResponse.getRequestId())));
        }

        val requester = issueRequesterService.findById(issueResponse.getRequesterId(), true);
        val request = issueRequestService.findById(issueResponse.getRequestId());

        val saved = issueResponseService.save(IssueResponseModel
                .builder()
                .id(issueResponse.getId())
                .requester(requester)
                .request(request)
                .body(issueResponse.getBody())
                .build());

        return ResponseEntity.ok(mapIssueResponse(saved));
    }

    private PagedData mapPaged(GenericPagedModel<IssueResponseModel> model) {
        return PagedData.newBuilder()
                .setTotalElements(model.getTotalElements())
                .setNumberOfElements(model.getNumberOfElements())
                .setTotalPages(model.getTotalPages())
                .setIssueResponses(mapIssueResponses(model.getContent()))
                .build();
    }

    private IssueResponses mapIssueResponses(Collection<IssueResponseModel> models) {
        return IssueResponses.newBuilder()
                .addAllIssueResponses(models
                        .stream()
                        .map(this::mapIssueResponse)
                        .collect(toList()))
                .build();
    }

    private IssueResponse mapIssueResponse(IssueResponseModel model) {
        return IssueResponse.newBuilder()
                .setId(model.getId())
                .setRequesterId(model.getRequester().getId())
                .setRequestId(model.getRequest().getId())
                .setBody(model.getBody())
                .setCreated(Objects.nonNull(model.getCreated())
                        ? NullableInt64.newBuilder().setData(model.getCreated()
                        .toInstant().toEpochMilli()).build()
                        : NullableInt64.newBuilder().setNull(NullValue.NULL_VALUE).build())
                .build();
    }
}
