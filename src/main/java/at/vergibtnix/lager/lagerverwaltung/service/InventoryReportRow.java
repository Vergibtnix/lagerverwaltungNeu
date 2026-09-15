package at.vergibtnix.lager.lagerverwaltung.service;

import at.vergibtnix.lager.lagerverwaltung.model.Product;
import java.math.BigDecimal;

public record InventoryReportRow(
        Product product,
        Integer quantity,
        BigDecimal weightedPurchasePrice,
        BigDecimal totalValue
) {
}

