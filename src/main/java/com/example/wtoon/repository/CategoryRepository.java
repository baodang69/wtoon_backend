package com.example.wtoon.repository;

import com.example.wtoon.dto.response.CategoryResponseDTO;
import com.example.wtoon.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
}