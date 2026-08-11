package at.vergibtnix.lager.lagerverwaltung.web.form;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public class SaleForm {

    @NotNull(message = "Produkt ist erforderlich.")
    private Long productId;

    @NotNull(message = "Stueckzahl ist erforderlich.")
    @Positive(message = "Stueckzahl muss groesser als 0 sein.")
    private Integer quantity;

    @Size(max = 120, message = "Kundenname darf maximal 120 Zeichen haben.")
    private String customerName;

    @DecimalMin(value = "0.01", message = "Stueckpreis muss groesser als 0 sein.")
    private BigDecimal unitPrice;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate saleDate;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public LocalDate getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDate saleDate) {
        this.saleDate = saleDate;
    }
}

