package at.vergibtnix.lager.lagerverwaltung.service;

import at.vergibtnix.lager.lagerverwaltung.model.Product;
import at.vergibtnix.lager.lagerverwaltung.model.ProductCategory;
import at.vergibtnix.lager.lagerverwaltung.repository.ProductCategoryRepository;
import at.vergibtnix.lager.lagerverwaltung.repository.ProductRepository;
import at.vergibtnix.lager.lagerverwaltung.repository.RestockOrderRepository;
import at.vergibtnix.lager.lagerverwaltung.repository.SaleTransactionRepository;
import at.vergibtnix.lager.lagerverwaltung.web.form.ProductFilterForm;
import at.vergibtnix.lager.lagerverwaltung.web.form.ProductForm;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Locale;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final SaleTransactionRepository saleRepository;
    private final RestockOrderRepository restockRepository;

    public ProductService(ProductRepository productRepository,
                          ProductCategoryRepository categoryRepository,
                          SaleTransactionRepository saleRepository,
                          RestockOrderRepository restockRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.saleRepository = saleRepository;
        this.restockRepository = restockRepository;
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
                generateArticleNumber(),
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

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = getById(productId);
        long saleCount = saleRepository.countByProductId(productId);
        long restockCount = restockRepository.countByProductId(productId);
        if (saleCount > 0 || restockCount > 0) {
            throw new BusinessRuleException("Produkt kann wegen vorhandener Buchungen nicht geloescht werden.");
        }
        productRepository.delete(product);
    }

    private ProductCategory resolveCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessRuleException("Kategorie nicht gefunden."));
    }

    private BigDecimal defaultCost(BigDecimal purchasePrice) {
        return purchasePrice == null ? BigDecimal.ZERO : purchasePrice;
    }

    private String generateArticleNumber() {
        for (int i = 0; i < 10; i++) {
            String candidate = "ART-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
            if (productRepository.findByArticleNumberIgnoreCase(candidate).isEmpty()) {
                return candidate;
            }
        }
        throw new BusinessRuleException("Artikelnummer konnte nicht erzeugt werden. Bitte erneut versuchen.");
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

