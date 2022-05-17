package com.helpdesk.data.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "issue_response", schema = "help_desk")
@Entity
public class IssueResponseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "response_body")
    private String body;

    @CreationTimestamp
    @Column(insertable = false, updatable = false)
    private Date created;

    @ManyToOne
    @JsonBackReference
    private IssueRequesterModel requester;

    @ManyToOne
    @JsonBackReference
    private IssueRequestModel request;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        val that = (IssueResponseModel) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
