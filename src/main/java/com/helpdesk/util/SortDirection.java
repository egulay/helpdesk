package com.helpdesk.util;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public enum SortDirection {
    Ascending("ascending", "asc", "a"),
    Descending("descending", "desc", "dsc", "d"),
    ;

    private final String[] aliases;

    private static final Map<String, SortDirection> sortDirections = new HashMap<>();

    static {
        for (val sd : SortDirection.values()) {
            for (String alias : sd.aliases) {
                sortDirections.put(alias, sd);
            }
        }
    }

    SortDirection(String... aliases) {
        this.aliases = aliases;
    }

    public static SortDirection of(String alias) {
        if (StringUtils.isBlank(alias)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "SortDirection cannot be empty");
        }

        val sd = sortDirections.get(alias.toLowerCase());
        if (Objects.isNull(sd)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No enum alias for sort direction " + SortDirection.class.getCanonicalName() + "." + alias);
        }
        return sd;
    }
}