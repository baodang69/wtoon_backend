package com.example.wtoon.entity;

import com.example.wtoon.enums.UserStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "\"users\"")
@Data
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(unique = true, length = 100)
    private String email;

    @Column(length = 50, columnDefinition = "VARCHAR(50) DEFAULT 'user'")
    private String role;

    @Column(nullable = false)
    private UserStatus status;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable( name = "user_role", joinColumns = @JoinColumn(name="user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();
}