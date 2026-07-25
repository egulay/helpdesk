package io.gulay.helpdesk.data.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.ColumnDefault;
import jakarta.persistence.FetchType;


import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;

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

    @CreationTimestamp
    @Column(insertable = false, updatable = false)
    @PastOrPresent(message = "created must be in the past or present")
    private LocalDateTime created;

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
