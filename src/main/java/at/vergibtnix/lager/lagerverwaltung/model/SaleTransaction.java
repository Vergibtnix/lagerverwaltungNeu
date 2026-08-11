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
import java.time.LocalDate;

@Entity
@Table(name = "sales")
public class SaleTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(length = 120)
    private String customerName;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private LocalDate saleDate;

    protected SaleTransaction() {
    }

    public SaleTransaction(Product product, Integer quantity, String customerName, BigDecimal unitPrice, LocalDate saleDate) {
        this.product = product;
        this.quantity = quantity;
        this.customerName = customerName;
        this.unitPrice = unitPrice;
        this.saleDate = saleDate;
    }

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public String getCustomerName() {
        return customerName;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public LocalDate getSaleDate() {
        return saleDate;
    }
}

