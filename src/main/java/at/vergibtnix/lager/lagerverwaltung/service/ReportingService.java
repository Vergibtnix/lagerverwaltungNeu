package at.vergibtnix.lager.lagerverwaltung.service;

import at.vergibtnix.lager.lagerverwaltung.model.Product;
import at.vergibtnix.lager.lagerverwaltung.model.RestockOrder;
import at.vergibtnix.lager.lagerverwaltung.model.SaleTransaction;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportingService {

    private final SaleService saleService;
    private final RestockService restockService;
    private final ProductService productService;

    public ReportingService(SaleService saleService, RestockService restockService, ProductService productService) {
        this.saleService = saleService;
        this.restockService = restockService;
        this.productService = productService;
    }

    @Transactional(readOnly = true)
    public List<TransactionRow> getTransactionHistory() {
        List<TransactionRow> rows = new ArrayList<>();

        for (SaleTransaction sale : saleService.findAll()) {
            rows.add(new TransactionRow(
                    "VERKAUF",
                    sale.getId(),
                    sale.getProduct(),
                    sale.getSaleDate(),
                    sale.getQuantity(),
                    sale.getCustomerName(),
                    sale.getUnitPrice(),
                    sale.getProduct().isBackorderRequired() ? "nachzuliefern" : "ok"
            ));
        }

        for (RestockOrder restock : restockService.findAll()) {
            rows.add(new TransactionRow(
                    "NACHBESTELLUNG",
                    restock.getId(),
                    restock.getProduct(),
                    restock.getOrderedDate(),
                    restock.getQuantity(),
                    restock.getSupplier(),
                    null,
                    restock.isReceived() ? "eingegangen" : "offen"
            ));
        }

        rows.sort(Comparator.comparing(TransactionRow::date)
                .thenComparing(TransactionRow::transactionId)
                .reversed());
        return rows;
    }

    @Transactional(readOnly = true)
    public List<ProductProfitRow> getProfitByProduct() {
        List<Product> products = productService.findAll();
        Map<Long, List<SaleTransaction>> salesByProduct = saleService.findAll().stream()
                .collect(Collectors.groupingBy(sale -> sale.getProduct().getId()));

        return products.stream()
                .map(product -> buildProfitRow(product, salesByProduct.getOrDefault(product.getId(), List.of())))
                .sorted(Comparator.comparing(row -> row.product().getName(), String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private ProductProfitRow buildProfitRow(Product product, List<SaleTransaction> sales) {
        int soldQuantity = sales.stream().mapToInt(SaleTransaction::getQuantity).sum();
        BigDecimal revenue = sales.stream()
                .map(sale -> sale.getUnitPrice().multiply(BigDecimal.valueOf(sale.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal estimatedCost = product.getPurchasePrice().multiply(BigDecimal.valueOf(soldQuantity));
        BigDecimal profit = revenue.subtract(estimatedCost);

        return new ProductProfitRow(product, soldQuantity, revenue, estimatedCost, profit);
    }
}

