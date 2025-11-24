package com.example.wtoon.repository;

import com.example.wtoon.entity.Story;
import com.example.wtoon.repository.custom.StoryRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoryRepository extends JpaRepository<Story, String>, StoryRepositoryCustom {
    Optional<Story> findBySlug(String slug);
}

