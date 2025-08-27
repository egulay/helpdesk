package com.helpdesk.data.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.ColumnDefault;
import jakarta.persistence.FetchType;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.JoinColumn;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;


import java.util.Collection;
import java.util.Date;
import java.util.Objects;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@Table(name = "issue_request", schema = "help_desk")
@Entity
public class IssueRequestModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "request_body")
    @NotBlank(message = "body must not be blank")
    private String body;

    @Column(name = "is_solved", nullable = false)
    @ColumnDefault("false")
    @NotNull(message = "isSolved must not be null")
    private Boolean isSolved;

    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    @Column(insertable = false, updatable = false)
    @PastOrPresent(message = "created must be in the past or present")
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    @PastOrPresent(message = "solved must be in the past or present")
    private Date solved;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id")
    @JsonBackReference
    @NotNull(message = "requester must not be null")
    private IssueRequesterModel requester;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference
    @ToString.Exclude
    private Collection<IssueResponseModel> responses;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        val that = (IssueRequestModel) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
