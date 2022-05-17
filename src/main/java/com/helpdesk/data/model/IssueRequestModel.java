package com.helpdesk.data.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "issue_request", schema = "help_desk")
@Entity
public class IssueRequestModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "request_body")
    private String body;

    @Column(name = "is_solved", columnDefinition = "boolean default false")
    private Boolean isSolved;

    @CreationTimestamp
    @Column(insertable = false, updatable = false)
    private Date created;

    private Date solved;
    @ManyToOne
    @JsonBackReference
    private IssueRequesterModel requester;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL)
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
