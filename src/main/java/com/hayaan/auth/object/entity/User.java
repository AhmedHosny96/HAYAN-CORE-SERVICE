package com.hayaan.auth.object.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hayaan.flight.object.entity.Agent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Users")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "Username")
    private String username;

    @Column(name = "Email")
    private String email;

    @Column(name = "PhoneNumber")
    private String phoneNumber;

    @Column(name = "FullName")
    private String fullName;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AgentId") // Specify the column name here
    private Agent agent;

    @Column(name = "Password")
    @JsonIgnore
    private String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roleId") // Specify the column name here
    private Role role;

    @Column(name = "Status")
    private Integer status;

    @Column(name = "CreatedBy")
    private String createdBy;

    @Column(name = "CreatedDate")
    private LocalDateTime createdDate;

    @Column(name = "IsPasswordChanged")
    private boolean isPasswordChanged;


    // --- OAuth / Social login friendly fields ---
    @Column(name = "Provider", length = 50) // e.g., "google"
    private String provider;

    @Column(name = "ProviderId", length = 255) // Google's "sub"
    private String providerId;

    @Column(name = "PictureUrl", length = 1000)
    private String pictureUrl;

    @Column(name = "EmailVerified")
    private Boolean emailVerified;

    @Column(name = "LastLoginAt")
    private LocalDateTime lastLoginAt;

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", fullName='" + fullName + '\'' +
                ", agent=" + agent +
                ", password='" + password + '\'' +
                ", role=" + role +
                ", status=" + status +
                ", createdBy='" + createdBy + '\'' +
                ", createdDate=" + createdDate +
                ", isPasswordChanged=" + isPasswordChanged +
                '}';
    }
}