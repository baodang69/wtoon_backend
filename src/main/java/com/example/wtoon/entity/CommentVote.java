package com.example.wtoon.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name="comment_vote", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "comment_id"})
})
@Data
public class CommentVote extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="comment_id", nullable = false)
    private Comment comment;

    @Column(nullable = false)
    private Integer voteType;
}
