package at.vergibtnix.lager.lagerverwaltung;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import at.vergibtnix.lager.lagerverwaltung.model.Product;
import at.vergibtnix.lager.lagerverwaltung.model.ProductCategory;
import at.vergibtnix.lager.lagerverwaltung.model.RestockOrder;
import at.vergibtnix.lager.lagerverwaltung.repository.ProductCategoryRepository;
import at.vergibtnix.lager.lagerverwaltung.repository.ProductRepository;
import at.vergibtnix.lager.lagerverwaltung.repository.RestockOrderRepository;
import at.vergibtnix.lager.lagerverwaltung.repository.SaleTransactionRepository;
import at.vergibtnix.lager.lagerverwaltung.service.BusinessRuleException;
import at.vergibtnix.lager.lagerverwaltung.service.RestockService;
import at.vergibtnix.lager.lagerverwaltung.service.SaleService;
import at.vergibtnix.lager.lagerverwaltung.web.form.RestockForm;
import at.vergibtnix.lager.lagerverwaltung.web.form.SaleForm;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class InventoryFlowIntegrationTest {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductCategoryRepository categoryRepository;
    @Autowired
    private SaleTransactionRepository saleRepository;
    @Autowired
    private RestockOrderRepository restockRepository;
    @Autowired
    private SaleService saleService;
    @Autowired
    private RestockService restockService;

    @BeforeEach
    void setUp() {
        restockRepository.deleteAll();
        saleRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    void saleAndDeleteAdjustStockCorrectly() {
        Product product = createProduct(10);

        SaleForm form = new SaleForm();
        form.setProductId(product.getId());
        form.setQuantity(4);
        form.setSaleDate(LocalDate.now());
        saleService.recordSale(form);

        Product afterSale = productRepository.findById(product.getId()).orElseThrow();
        assertEquals(6, afterSale.getStock());

        Long saleId = saleRepository.findAll().getFirst().getId();
        saleService.deleteSale(saleId);

        Product afterDelete = productRepository.findById(product.getId()).orElseThrow();
        assertEquals(10, afterDelete.getStock());
    }

    @Test
    void restockReceiptAdjustsStockAndPreventsDeletionAfterReceipt() {
        Product product = createProduct(1);

        RestockForm form = new RestockForm();
        form.setProductId(product.getId());
        form.setQuantity(5);
        form.setExpectedDeliveryDate(LocalDate.now().plusDays(3));
        form.setSupplier("Test Supplier");
        RestockOrder order = restockService.createRestock(form);

        Product beforeReceipt = productRepository.findById(product.getId()).orElseThrow();
        assertEquals(1, beforeReceipt.getStock());

        restockService.confirmReceipt(order.getId(), LocalDate.now());

        Product afterReceipt = productRepository.findById(product.getId()).orElseThrow();
        assertEquals(6, afterReceipt.getStock());

        assertThrows(BusinessRuleException.class, () -> restockService.deleteRestock(order.getId()));
    }

    private Product createProduct(int stock) {
        ProductCategory category = categoryRepository.save(new ProductCategory("Test Kategorie"));
        return productRepository.save(new Product(
                "Test Produkt",
                "Beschreibung",
                category,
                new BigDecimal("10.00"),
                new BigDecimal("4.00"),
                stock
        ));
    }
}

