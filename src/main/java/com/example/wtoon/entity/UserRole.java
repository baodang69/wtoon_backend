package com.example.wtoon.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "user_role")
@Data
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;
}