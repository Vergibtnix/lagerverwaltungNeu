package at.vergibtnix.lager.lagerverwaltung.service;

import at.vergibtnix.lager.lagerverwaltung.model.Product;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRow(
        String type,
        Long transactionId,
        Product product,
        LocalDate date,
        Integer quantity,
        String customerOrSupplier,
        BigDecimal unitPrice,
        String status
) {
}

