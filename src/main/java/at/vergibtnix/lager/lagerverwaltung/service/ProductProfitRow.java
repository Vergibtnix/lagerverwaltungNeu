package at.vergibtnix.lager.lagerverwaltung.service;

import at.vergibtnix.lager.lagerverwaltung.model.Product;
import java.math.BigDecimal;

public record ProductProfitRow(
        Product product,
        int soldQuantity,
        BigDecimal revenue,
        BigDecimal estimatedCost,
        BigDecimal profit
) {
}

