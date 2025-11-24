package com.example.wtoon.repository.impl;

import com.example.wtoon.entity.Story;
import com.example.wtoon.repository.custom.StoryRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class StoryRepositoryCustomImpl implements StoryRepositoryCustom {

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public List<Story> findStoriesToSyncChapters(Pageable pageable) {
        String jpql = "SELECT s FROM Story s " +
                "WHERE s.lastChapterSyncAt IS NULL OR s.updatedAt > s.lastChapterSyncAt " +
                "ORDER BY s.updatedAt DESC";
        
        TypedQuery<Story> query = entityManager.createQuery(jpql, Story.class);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        
        return query.getResultList();
    }

    @Override
    public Page<Story> findAllWithCategories(Pageable pageable) {
        String jpql = "SELECT DISTINCT s FROM Story s LEFT JOIN FETCH s.categories";
        
        TypedQuery<Story> query = entityManager.createQuery(jpql, Story.class);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        
        List<Story> stories = query.getResultList();
        
        String countJpql = "SELECT COUNT(DISTINCT s) FROM Story s";
        Long total = entityManager.createQuery(countJpql, Long.class).getSingleResult();
        
        return new PageImpl<>(stories, pageable, total);
    }

    @Override
    public Optional<Story> findBySlugWithDetails(String slug) {
        String jpql = "SELECT DISTINCT s FROM Story s " +
                "LEFT JOIN FETCH s.categories " +
                "LEFT JOIN FETCH s.chapters " +
                "WHERE s.slug = :slug";
        
        TypedQuery<Story> query = entityManager.createQuery(jpql, Story.class);
        query.setParameter("slug", slug);
        
        return query.getResultStream().findFirst();
    }

    @Override
    public Page<Story> findAllByCategory(String categoryId, Pageable pageable) {
        String jpql = "SELECT s FROM Story s JOIN s.categories c WHERE c.id = :categoryId";
        
        TypedQuery<Story> query = entityManager.createQuery(jpql, Story.class);
        query.setParameter("categoryId", categoryId);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        
        List<Story> stories = query.getResultList();
        
        String countJpql = "SELECT COUNT(s) FROM Story s JOIN s.categories c WHERE c.id = :categoryId";
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);
        countQuery.setParameter("categoryId", categoryId);
        Long total = countQuery.getSingleResult();
        
        return new PageImpl<>(stories, pageable, total);
    }
}
