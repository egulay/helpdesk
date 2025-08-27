package com.helpdesk.data.model;

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


import java.util.Collection;
import java.util.Date;
import java.util.Objects;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@Table(name = "issue_requester", schema = "help_desk")
@Entity
public class IssueRequesterModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "full_name")
    @NotBlank(message = "fullName must not be blank")
    @Size(max = 255, message = "fullName length must be <= 255")
    private String fullName;

    @NotBlank(message = "email must not be blank")
    @Email(message = "email must be a valid address")
    @Size(max = 320, message = "email length must be <= 320")
    private String email;

    @Column(name = "is_active", nullable = false)
    @ColumnDefault("true")
    @NotNull(message = "isActive must not be null")
    private Boolean isActive;

    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    @Column(insertable = false, updatable = false)
    private Date created;

    @OneToMany(mappedBy = "requester", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference
    @ToString.Exclude
    private Collection<IssueRequestModel> requests;

    @OneToMany(mappedBy = "requester", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
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
