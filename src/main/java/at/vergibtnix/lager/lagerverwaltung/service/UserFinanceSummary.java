package at.vergibtnix.lager.lagerverwaltung.service;

import java.math.BigDecimal;

public record UserFinanceSummary(
        BigDecimal income,
        BigDecimal expense,
        BigDecimal balance
) {
}

