package com.helpdesk.data.util;

import lombok.Builder;
import lombok.Data;

import java.util.Collection;

@Builder
@Data
public class GenericPagedModel<T> {
    public long totalElements;
    public int totalPages;
    public int numberOfElements;
    public Collection<T> content;
}
