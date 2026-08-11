package at.vergibtnix.lager.lagerverwaltung.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, length = 5000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private ProductCategory category;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal salePrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal purchasePrice = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer stock;

    protected Product() {
    }

    public Product(String name, String description, ProductCategory category, BigDecimal salePrice, BigDecimal purchasePrice, Integer stock) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.salePrice = salePrice;
        this.purchasePrice = purchasePrice == null ? BigDecimal.ZERO : purchasePrice;
        this.stock = stock;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public Integer getStock() {
        return stock;
    }

    public boolean isBackorderRequired() {
        return stock != null && stock < 0;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(ProductCategory category) {
        this.category = category;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice == null ? BigDecimal.ZERO : purchasePrice;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }
}

