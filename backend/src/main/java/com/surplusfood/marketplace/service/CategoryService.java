package com.surplusfood.marketplace.service;

import com.surplusfood.marketplace.dto.CategoryResponse;
import com.surplusfood.marketplace.repository.CategoryRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllActiveCategories() {
        return categoryRepository.findByActiveTrue().stream()
                .map(cat -> new CategoryResponse(cat.getId(), cat.getName(), cat.isActive()))
                .collect(Collectors.toList());
    }
}
