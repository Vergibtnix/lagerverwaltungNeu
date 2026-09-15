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
@Table(name = "restock_orders")
public class RestockOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private AppUser owner;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private LocalDate expectedDeliveryDate;

    @Column(nullable = false, length = 120)
    private String supplier;

    @Column(nullable = false)
    private LocalDate orderedDate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPurchasePrice;

    @Column
    private LocalDate receivedDate;

    @Column
    private LocalDate canceledDate;

    protected RestockOrder() {
    }

    public RestockOrder(Product product,
                        AppUser owner,
                        Integer quantity,
                        LocalDate expectedDeliveryDate,
                        String supplier,
                        LocalDate orderedDate,
                        BigDecimal unitPurchasePrice) {
        this.product = product;
        this.owner = owner;
        this.quantity = quantity;
        this.expectedDeliveryDate = expectedDeliveryDate;
        this.supplier = supplier;
        this.orderedDate = orderedDate;
        this.unitPurchasePrice = unitPurchasePrice;
    }

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public AppUser getOwner() {
        return owner;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public LocalDate getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public String getSupplier() {
        return supplier;
    }

    public LocalDate getOrderedDate() {
        return orderedDate;
    }

    public BigDecimal getUnitPurchasePrice() {
        return unitPurchasePrice;
    }

    public LocalDate getReceivedDate() {
        return receivedDate;
    }

    public LocalDate getCanceledDate() {
        return canceledDate;
    }

    public boolean isReceived() {
        return receivedDate != null;
    }

    public boolean isCanceled() {
        return canceledDate != null;
    }

    public void markReceived(LocalDate receivedDate) {
        this.receivedDate = receivedDate;
    }

    public void cancel(LocalDate cancellationDate) {
        this.canceledDate = cancellationDate;
    }
}

