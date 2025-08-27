package com.helpdesk.controller;

import com.google.protobuf.NullValue;
import com.helpdesk.data.model.IssueRequesterModel;
import com.helpdesk.data.service.IssueRequesterService;
import com.helpdesk.data.util.GenericPagedModel;
import com.helpdesk.protoGen.*;
import com.helpdesk.util.SortDirection;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Date;
import java.util.Objects;

import static com.helpdesk.controller.util.Parsers.*;
import static java.util.stream.Collectors.toList;

@RestController
@Slf4j
public class IssueRequesterController {
    final IssueRequesterService issueRequesterService;

    @Autowired
    public IssueRequesterController(IssueRequesterService issueRequesterService) {
        this.issueRequesterService = issueRequesterService;
    }

    @RequestMapping(value = "/v1/issue_requesters/{id}", method = RequestMethod.GET)
    private ResponseEntity<IssueRequester> getIssueRequesterByIdV1(
            @PathVariable String id,
            @RequestParam(defaultValue = "") String issueRequestIsSolved) {
        log.info("Calling: getIssueRequesterByIdV1 >> ".concat(id));

        val issueRequester = StringUtils.isBlank(issueRequestIsSolved)
                ? issueRequesterService.findById(tryParseInteger(id, "id"))
                : issueRequesterService.findById(tryParseInteger(id, "id")
                , tryParseBoolean(issueRequestIsSolved, "issueRequestIsSolved"));

        return ResponseEntity.ok(mapIssueRequester(issueRequester));
    }

    @RequestMapping(value = "/v1/issue_requesters/find_all", method = RequestMethod.GET)
    private ResponseEntity<PagedData> getAllIssueRequestersByCreatedBeforeAndCreatedAfter(
            @RequestParam(defaultValue = "") String createdBefore,
            @RequestParam(defaultValue = "") String createdAfter,
            @RequestParam(defaultValue = "") String isActive,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "created") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        val logMessage = "Calling: getAllIssueRequestersByCreatedBeforeAndCreatedAfter >> Created Before: "
                .concat(createdBefore)
                .concat(" | Created After: ").concat(createdAfter);

        if (StringUtils.isNotBlank(createdBefore) && StringUtils.isNotBlank(createdAfter)
                && StringUtils.isNotBlank(isActive)) {
            log.info(logMessage
                    .concat(" | Is Active: ").concat(isActive));

            val result = issueRequesterService
                    .findAllByCreatedBeforeAndCreatedAfterAndIsActive(
                            new Date(tryParseLong(createdBefore, "createdBefore")),
                            new Date(tryParseLong(createdAfter, "createdAfter")),
                            tryParseBoolean(isActive, "isActive"),
                            pageNo, pageSize, sortBy, SortDirection.of(sortDir));

            return ResponseEntity.ok(mapPaged(result));
        }

        if (StringUtils.isNotBlank(createdBefore) && StringUtils.isNotBlank(createdAfter)) {
            log.info(logMessage);

            val result = issueRequesterService
                    .findAllByCreatedBeforeAndCreatedAfter(new Date(tryParseLong(createdBefore, "createdBefore")),
                            new Date(tryParseLong(createdAfter, "createdAfter")),
                            pageNo, pageSize, sortBy, SortDirection.of(sortDir));

            return ResponseEntity.ok(mapPaged(result));
        }

        log.info("Calling: getAllIssueRequestersByCreatedBeforeAndCreatedAfter");

        val result = issueRequesterService
                .findAll(pageNo, pageSize, sortBy
                        , SortDirection.of(sortDir));

        return ResponseEntity.ok(mapPaged(result));
    }

    @RequestMapping(value = "/v1/issue_requesters/find_all_by_full_name/{fullName}", method = RequestMethod.GET)
    private ResponseEntity<PagedData> getAllIssueRequestersByFullName(
            @PathVariable String fullName,
            @RequestParam(defaultValue = "") String createdBefore,
            @RequestParam(defaultValue = "") String createdAfter,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "created") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        if (StringUtils.isNotBlank(createdBefore) && StringUtils.isNotBlank(createdAfter)) {
            log.info("Calling: getAllIssueRequestersByFullName >> Full Name: ".concat(fullName)
                    .concat(" | Created Before: ").concat(createdBefore)
                    .concat(" | Created After: ").concat(createdAfter));

            val result = issueRequesterService
                    .findAllByFullNameContainingIgnoreCaseAndCreatedBeforeAndCreatedAfter(fullName,
                            new Date(tryParseLong(createdBefore, "createdBefore")),
                            new Date(tryParseLong(createdAfter, "createdAfter")),
                            pageNo, pageSize, sortBy, SortDirection.of(sortDir));

            return ResponseEntity.ok(mapPaged(result));
        }

        log.info("Calling: getAllIssueRequestersByRequesterId >> Full Name: ".concat(fullName));

        val result = issueRequesterService
                .findAllByFullNameContainingIgnoreCase(fullName, pageNo, pageSize, sortBy
                        , SortDirection.of(sortDir));

        return ResponseEntity.ok(mapPaged(result));
    }

    @RequestMapping(value = "/v1/issue_requesters/find_all_by_email/{email}", method = RequestMethod.GET)
    private ResponseEntity<PagedData> getAllIssueRequestersByEmail(
            @PathVariable String email,
            @RequestParam(defaultValue = "") String createdBefore,
            @RequestParam(defaultValue = "") String createdAfter,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "created") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        if (StringUtils.isNotBlank(createdBefore) && StringUtils.isNotBlank(createdAfter)) {
            log.info("Calling: getAllIssueRequestersByEmail >> E-mail: ".concat(email)
                    .concat(" | Created Before: ").concat(createdBefore)
                    .concat(" | Created After: ").concat(createdAfter));

            val result = issueRequesterService
                    .findAllByEmailContainingIgnoreCaseAndCreatedBeforeAndCreatedAfter(email,
                            new Date(tryParseLong(createdBefore, "createdBefore")),
                            new Date(tryParseLong(createdAfter, "createdAfter")),
                            pageNo, pageSize, sortBy, SortDirection.of(sortDir));

            return ResponseEntity.ok(mapPaged(result));
        }

        log.info("Calling: getAllIssueRequestersByEmail >> E-mail: ".concat(email));

        val result = issueRequesterService
                .findAllByEmailContainingIgnoreCase(email, pageNo, pageSize, sortBy
                        , SortDirection.of(sortDir));

        return ResponseEntity.ok(mapPaged(result));
    }

    @RequestMapping(value = "/v1/issue_requesters/toggle_activation/{id}", method = RequestMethod.PUT)
    private ResponseEntity<IssueRequester> toggleIssueRequesterActivationV1(@PathVariable String id) {
        log.info("Calling: toggleIssueRequesterActivationV1 >> ".concat(id));

        val requester = issueRequesterService.toggleActivation(tryParseInteger(id, "id"));

        return ResponseEntity.ok(mapIssueRequester(requester));
    }

    @RequestMapping(value = "/v1/issue_requesters/delete/{id}", method = RequestMethod.DELETE)
    private ResponseEntity<IssueRequester> deleteIssueRequesterV1(@PathVariable String id) {
        log.info("Calling: deleteIssueRequesterV1 >> ".concat(id));

        val result = issueRequesterService.hardDelete(tryParseInteger(id, "id"));

        return ResponseEntity.ok(mapIssueRequester(result));
    }

    @RequestMapping(value = "/v1/issue_requesters/save", method = RequestMethod.POST)
    private ResponseEntity<IssueRequester> saveIssueRequesterV1(@RequestBody IssueRequester issueRequester) {
        log.info("Calling: saveIssueRequesterV1 >> ".concat(issueRequester.toString()));

        val saved = issueRequesterService.save(IssueRequesterModel
                .builder()
                .id(issueRequester.getId())
                .isActive(issueRequester.getIsActive().getData())
                .fullName(issueRequester.getFullName())
                .email(issueRequester.getEmail())
                .build());

        return ResponseEntity.ok(mapIssueRequester(saved));
    }

    private PagedData mapPaged(GenericPagedModel<IssueRequesterModel> model) {
        return PagedData.newBuilder()
                .setTotalElements(model.getTotalElements())
                .setNumberOfElements(model.getNumberOfElements())
                .setTotalPages(model.getTotalPages())
                .setIssueRequesters(mapIssueRequesters(model.getContent()))
                .build();
    }

    private IssueRequesters mapIssueRequesters(Collection<IssueRequesterModel> models) {
        return IssueRequesters.newBuilder()
                .addAllIssueRequesters(models
                        .stream()
                        .map(this::mapIssueRequester)
                        .collect(toList()))
                .build();
    }

    private IssueRequester mapIssueRequester(IssueRequesterModel model) {
        return IssueRequester.newBuilder()
                .setId(model.getId())
                .setFullName(model.getFullName())
                .setEmail(model.getEmail())
                .setIsActive(Objects.nonNull(model.getIsActive())
                        ? NullableBoolean.newBuilder().setData(model.getIsActive()).build()
                        : NullableBoolean.newBuilder().setNull(NullValue.NULL_VALUE).build())
                .setCreated(Objects.nonNull(model.getCreated())
                        ? NullableInt64.newBuilder().setData(model.getCreated()
                        .toInstant().toEpochMilli()).build()
                        : NullableInt64.newBuilder().setNull(NullValue.NULL_VALUE).build())
                .build();
    }
}
