package at.vergibtnix.lager.lagerverwaltung.service;

import at.vergibtnix.lager.lagerverwaltung.model.Product;
import at.vergibtnix.lager.lagerverwaltung.model.ProductCategory;
import at.vergibtnix.lager.lagerverwaltung.repository.ProductCategoryRepository;
import at.vergibtnix.lager.lagerverwaltung.repository.ProductRepository;
import at.vergibtnix.lager.lagerverwaltung.web.form.ProductFilterForm;
import at.vergibtnix.lager.lagerverwaltung.web.form.ProductForm;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, ProductCategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<Product> findAllFiltered(ProductFilterForm filter) {
        Comparator<Product> comparator = buildComparator(filter.getSortBy());
        if ("desc".equalsIgnoreCase(filter.getSortDir())) {
            comparator = comparator.reversed();
        }

        return productRepository.findAll().stream()
                .filter(product -> matchesName(product, filter.getName()))
                .filter(product -> matchesCategory(product, filter.getCategoryId()))
                .filter(product -> matchesStock(product, filter.getStockMin(), filter.getStockMax()))
                .filter(product -> matchesPrice(product, filter.getPriceMin(), filter.getPriceMax()))
                .sorted(comparator)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Product getById(Long productId) {
        return productRepository.findWithCategoryById(productId)
                .orElseThrow(() -> new BusinessRuleException("Produkt nicht gefunden."));
    }

    @Transactional
    public Product createProduct(ProductForm form) {
        productRepository.findByNameIgnoreCase(form.getName())
                .ifPresent(existing -> {
                    throw new BusinessRuleException("Produktname ist bereits vergeben.");
                });

        ProductCategory category = resolveCategory(form.getCategoryId());
        Product product = new Product(
                form.getName().trim(),
                form.getDescription().trim(),
                category,
                form.getSalePrice(),
                defaultCost(form.getPurchasePrice()),
                form.getStock()
        );
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long productId, ProductForm form) {
        Product product = getById(productId);
        productRepository.findByNameIgnoreCase(form.getName())
                .filter(existing -> !existing.getId().equals(productId))
                .ifPresent(existing -> {
                    throw new BusinessRuleException("Produktname ist bereits vergeben.");
                });

        product.setName(form.getName().trim());
        product.setDescription(form.getDescription().trim());
        product.setCategory(resolveCategory(form.getCategoryId()));
        product.setSalePrice(form.getSalePrice());
        product.setPurchasePrice(defaultCost(form.getPurchasePrice()));
        product.setStock(form.getStock());
        return product;
    }

    private ProductCategory resolveCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessRuleException("Kategorie nicht gefunden."));
    }

    private BigDecimal defaultCost(BigDecimal purchasePrice) {
        return purchasePrice == null ? BigDecimal.ZERO : purchasePrice;
    }

    private Comparator<Product> buildComparator(String sortBy) {
        if ("category".equalsIgnoreCase(sortBy)) {
            return Comparator.comparing(product -> product.getCategory().getName(), String.CASE_INSENSITIVE_ORDER);
        }
        if ("stock".equalsIgnoreCase(sortBy)) {
            return Comparator.comparing(Product::getStock);
        }
        if ("price".equalsIgnoreCase(sortBy)) {
            return Comparator.comparing(Product::getSalePrice);
        }
        return Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER);
    }

    private boolean matchesName(Product product, String nameFilter) {
        if (nameFilter == null || nameFilter.isBlank()) {
            return true;
        }
        return product.getName().toLowerCase().contains(nameFilter.trim().toLowerCase());
    }

    private boolean matchesCategory(Product product, Long categoryId) {
        if (categoryId == null) {
            return true;
        }
        return product.getCategory().getId().equals(categoryId);
    }

    private boolean matchesStock(Product product, Integer stockMin, Integer stockMax) {
        if (stockMin != null && product.getStock() < stockMin) {
            return false;
        }
        return stockMax == null || product.getStock() <= stockMax;
    }

    private boolean matchesPrice(Product product, BigDecimal priceMin, BigDecimal priceMax) {
        if (priceMin != null && product.getSalePrice().compareTo(priceMin) < 0) {
            return false;
        }
        return priceMax == null || product.getSalePrice().compareTo(priceMax) <= 0;
    }
}

