package at.vergibtnix.lager.lagerverwaltung.web.form;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class ProductForm {

    @NotBlank(message = "Produktname ist erforderlich.")
    @Size(max = 100, message = "Produktname darf maximal 100 Zeichen haben.")
    private String name;

    @NotBlank(message = "Beschreibung ist erforderlich.")
    @Size(max = 5000, message = "Beschreibung darf maximal 5000 Zeichen haben.")
    private String description;

    @NotNull(message = "Kategorie ist erforderlich.")
    private Long categoryId;

    @NotNull(message = "Verkaufspreis ist erforderlich.")
    @DecimalMin(value = "0.01", message = "Verkaufspreis muss groesser als 0 sein.")
    private BigDecimal salePrice;

    @DecimalMin(value = "0.00", message = "Einkaufspreis darf nicht negativ sein.")
    private BigDecimal purchasePrice;

    @NotNull(message = "Lagerbestand ist erforderlich.")
    @Min(value = -100000, message = "Lagerbestand zu klein.")
    @Max(value = 100000, message = "Lagerbestand zu gross.")
    private Integer stock;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }
}

