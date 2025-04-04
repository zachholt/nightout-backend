package com.zachholt.nightout.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.OneToOne;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;

@Entity(name = "users")
@Schema(description = "Represents a user in the system")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_id_seq")
    @SequenceGenerator(name = "user_id_seq", sequenceName = "user_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    @Schema(description = "Unique identifier for the user", example = "1")
    private Long id;

    @Column(name = "name", nullable = false)
    @Schema(description = "User's full name", example = "John Doe")
    private String name;

    @Column(name = "email", unique = true, nullable = false)
    @Email(message = "Invalid email address")
    @NotBlank(message = "Email is required")
    @Schema(description = "User's email address", example = "john.doe@example.com")
    private String email;

    @Column(name = "password", nullable = false)
    @NotBlank(message = "Password is required")
    @Schema(description = "User's password (hashed)", example = "********")
    private String password;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @Schema(description = "Timestamp when the user account was created", example = "2024-03-15T10:30:00")
    private LocalDateTime createdAt;

    @Column(name = "profile_image")
    @Schema(description = "URL of the user's profile image", example = "https://example.com/profile.jpg")
    private String profileImage = "https://example.com/default-profile.jpg";

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Schema(description = "User's current coordinate information")
    private Coordinate coordinate;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate coordinate) {
        if (coordinate != null) {
            coordinate.setUser(this);
        }
        this.coordinate = coordinate;
    }
}
