package at.vergibtnix.lager.lagerverwaltung.service;

import at.vergibtnix.lager.lagerverwaltung.model.ProductCategory;
import at.vergibtnix.lager.lagerverwaltung.repository.ProductCategoryRepository;
import at.vergibtnix.lager.lagerverwaltung.web.form.CategoryForm;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    private final ProductCategoryRepository categoryRepository;

    public CategoryService(ProductCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductCategory> findAll() {
        return categoryRepository.findAll().stream()
                .sorted(Comparator.comparing(ProductCategory::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional
    public ProductCategory createCategory(CategoryForm form) {
        categoryRepository.findByNameIgnoreCase(form.getName())
                .ifPresent(category -> {
                    throw new BusinessRuleException("Kategorie existiert bereits.");
                });
        ProductCategory category = new ProductCategory(form.getName().trim());
        return categoryRepository.save(category);
    }
}

