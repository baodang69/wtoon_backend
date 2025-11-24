package com.example.wtoon.repository.custom;

import com.example.wtoon.entity.Chapter;
import com.example.wtoon.entity.ChapterImage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChapterImageRepositoryCustomImpl implements ChapterImageRepositoryCustom {

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public List<ChapterImage> findAllByChapterOrderByImagePageAsc(Chapter chapter) {
        String jpql = "SELECT ci FROM ChapterImage ci WHERE ci.chapter = :chapter ORDER BY ci.imagePage ASC";
        
        TypedQuery<ChapterImage> query = entityManager.createQuery(jpql, ChapterImage.class);
        query.setParameter("chapter", chapter);
        
        return query.getResultList();
    }
}
