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
                    sale.getCostPrice(),
                    sale.isCanceled() ? "storniert" : (sale.getProduct().isBackorderRequired() ? "nachzuliefern" : "ok"),
                    sale.getOwner() == null ? "-" : sale.getOwner().getUsername()
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
                    restock.getUnitPurchasePrice(),
                    restock.isCanceled() ? "storniert" : (restock.isReceived() ? "eingegangen" : "offen"),
                    restock.getOwner() == null ? "-" : restock.getOwner().getUsername()
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
                .filter(sale -> !sale.isCanceled())
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
        BigDecimal estimatedCost = sales.stream()
                .map(sale -> sale.getCostPrice().multiply(BigDecimal.valueOf(sale.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal profit = revenue.subtract(estimatedCost);

        return new ProductProfitRow(product, soldQuantity, revenue, estimatedCost, profit);
    }

    @Transactional(readOnly = true)
    public List<InventoryReportRow> getInventoryReportRows() {
        return getInventoryReportRows(productService.findAll());
    }

    @Transactional(readOnly = true)
    public List<InventoryReportRow> getInventoryReportRows(List<Product> products) {
        return products.stream()
                .map(this::toInventoryRow)
                .sorted(Comparator.comparing(row -> row.product().getName(), String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InventoryReportRow> getTopProductsByInventoryValue(int limit) {
        return getInventoryReportRows().stream()
                .sorted(Comparator.comparing(InventoryReportRow::totalValue).reversed())
                .limit(limit)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserFinanceSummary getFinanceSummaryForCurrentUser() {
        BigDecimal income = saleService.findAll().stream()
                .filter(sale -> !sale.isCanceled())
                .map(sale -> sale.getUnitPrice().multiply(BigDecimal.valueOf(sale.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal expense = restockService.findAll().stream()
                .filter(restock -> restock.isReceived() && !restock.isCanceled())
                .map(restock -> restock.getUnitPurchasePrice().multiply(BigDecimal.valueOf(restock.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new UserFinanceSummary(income, expense, income.subtract(expense));
    }

    private InventoryReportRow toInventoryRow(Product product) {
        BigDecimal totalValue = product.getPurchasePrice().multiply(BigDecimal.valueOf(product.getStock()));
        return new InventoryReportRow(product, product.getStock(), product.getPurchasePrice(), totalValue);
    }
}

