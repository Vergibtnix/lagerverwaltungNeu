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

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private LocalDate expectedDeliveryDate;

    @Column(nullable = false, length = 120)
    private String supplier;

    @Column(nullable = false)
    private LocalDate orderedDate;

    @Column
    private LocalDate receivedDate;

    protected RestockOrder() {
    }

    public RestockOrder(Product product, Integer quantity, LocalDate expectedDeliveryDate, String supplier, LocalDate orderedDate) {
        this.product = product;
        this.quantity = quantity;
        this.expectedDeliveryDate = expectedDeliveryDate;
        this.supplier = supplier;
        this.orderedDate = orderedDate;
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

    public LocalDate getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public String getSupplier() {
        return supplier;
    }

    public LocalDate getOrderedDate() {
        return orderedDate;
    }

    public LocalDate getReceivedDate() {
        return receivedDate;
    }

    public boolean isReceived() {
        return receivedDate != null;
    }

    public void markReceived(LocalDate receivedDate) {
        this.receivedDate = receivedDate;
    }
}

