package com.example.wtoon.repository.impl;

import com.example.wtoon.entity.Story;
import com.example.wtoon.repository.custom.StoryRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
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
public class StoryRepositoryImpl implements StoryRepositoryCustom {

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public List<Story> getStoriesToSyncChapters(Pageable pageable) {
        String jpql = "SELECT s FROM Story s " +
                "WHERE s.lastChapterSyncAt IS NULL OR s.updatedAt > s.lastChapterSyncAt " +
                "ORDER BY s.updatedAt DESC";
        
        TypedQuery<Story> query = entityManager.createQuery(jpql, Story.class);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        
        return query.getResultList();
    }

    @Override
    public Page<Story> getAllStoriesWithCategories(Pageable pageable) {
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
    public Optional<Story> getStoryDetailBySlug(String slug) {
        String jpql = "SELECT DISTINCT s FROM Story s " +
                "LEFT JOIN FETCH s.categories " +
                "LEFT JOIN FETCH s.chapters " +
                "WHERE s.slug = :slug";
        
        TypedQuery<Story> query = entityManager.createQuery(jpql, Story.class);
        query.setParameter("slug", slug);
        
        return query.getResultStream().findFirst();
    }

    @Override
    public Page<Story> getStoriesByCategoryId(String categoryId, Pageable pageable) {
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

    @Override
    public Page<Story> getStoriesByDaysOfWeek(String dayOfWeekStr, Pageable pageable) {
        // 1. Xử lý Input (String -> Int)
        int dayValue;
        try {
            if (dayOfWeekStr == null || dayOfWeekStr.isBlank()) {
                return Page.empty(pageable);
            }
            String normalizedDay = dayOfWeekStr.trim().toUpperCase();
            dayValue = java.time.DayOfWeek.valueOf(normalizedDay).getValue(); // 1=Mon, 7=Sun
        } catch (IllegalArgumentException e) {
            return Page.empty(pageable);
        }

        // 2. Query Lấy Dữ Liệu (Native SQL)
        // SỬA: Đổi :dayValue thành ?1
        String sql = "SELECT * FROM story " +
                "WHERE CAST(EXTRACT(ISODOW FROM updated_at) AS INTEGER) = ?1 " +
                "ORDER BY updated_at DESC";

        // map kết quả native sang Entity class Story
        Query query = entityManager.createNativeQuery(sql, Story.class);

        // SỬA: Set tham số cho vị trí số 1 (khớp với ?1 ở trên)
        query.setParameter(1, dayValue);

        // Phân trang
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<Story> stories = query.getResultList();

        // 3. Query Đếm Tổng số (Native SQL)
        // SỬA: Đổi :dayValue thành ?1
        String countSql = "SELECT COUNT(*) FROM story " +
                "WHERE CAST(EXTRACT(ISODOW FROM updated_at) AS INTEGER) = ?1";

        Query countQuery = entityManager.createNativeQuery(countSql);

        // SỬA: Set tham số cho vị trí số 1
        countQuery.setParameter(1, dayValue);

        // Ép kiểu an toàn (BigInteger -> Long)
        Number totalResult = (Number) countQuery.getSingleResult();
        long total = totalResult.longValue();

        // 4. Trả về Page
        return new PageImpl<>(stories, pageable, total);
    }
}
