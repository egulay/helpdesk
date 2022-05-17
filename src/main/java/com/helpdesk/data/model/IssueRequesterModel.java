package com.helpdesk.data.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

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
@Table(name = "issue_requester", schema = "help_desk")
@Entity
public class IssueRequesterModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "full_name")
    private String fullName;

    private String email;

    @Column(name = "is_active", insertable = false)
    @Generated(GenerationTime.INSERT)
    private Boolean isActive;

    @CreationTimestamp
    @Column(insertable = false, updatable = false)
    private Date created;

    @OneToMany(mappedBy = "requester", cascade = CascadeType.ALL)
    @JsonManagedReference
    @ToString.Exclude
    private Collection<IssueRequestModel> requests;

    @OneToMany(mappedBy = "requester", cascade = CascadeType.ALL)
    @JsonManagedReference
    @ToString.Exclude
    private Collection<IssueResponseModel> responses;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        val that = (IssueRequesterModel) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
