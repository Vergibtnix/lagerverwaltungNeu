package at.vergibtnix.lager.lagerverwaltung.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public class RestockForm {

    @NotNull(message = "Produkt ist erforderlich.")
    private Long productId;

    @NotNull(message = "Bestellmenge ist erforderlich.")
    @Positive(message = "Bestellmenge muss groesser als 0 sein.")
    private Integer quantity;

    @NotNull(message = "Voraussichtliches Lieferdatum ist erforderlich.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate expectedDeliveryDate;

    @NotBlank(message = "Lieferant ist erforderlich.")
    @Size(max = 120, message = "Lieferant darf maximal 120 Zeichen haben.")
    private String supplier;

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

    public LocalDate getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }
}

