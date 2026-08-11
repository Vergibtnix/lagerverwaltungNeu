package at.vergibtnix.lager.lagerverwaltung.config;

import at.vergibtnix.lager.lagerverwaltung.model.Product;
import at.vergibtnix.lager.lagerverwaltung.model.ProductCategory;
import at.vergibtnix.lager.lagerverwaltung.model.RestockOrder;
import at.vergibtnix.lager.lagerverwaltung.model.SaleTransaction;
import at.vergibtnix.lager.lagerverwaltung.repository.ProductCategoryRepository;
import at.vergibtnix.lager.lagerverwaltung.repository.ProductRepository;
import at.vergibtnix.lager.lagerverwaltung.repository.RestockOrderRepository;
import at.vergibtnix.lager.lagerverwaltung.repository.SaleTransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DemoDataInitializer implements CommandLineRunner {

    private final ProductCategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final SaleTransactionRepository saleRepository;
    private final RestockOrderRepository restockRepository;

    public DemoDataInitializer(ProductCategoryRepository categoryRepository,
                               ProductRepository productRepository,
                               SaleTransactionRepository saleRepository,
                               RestockOrderRepository restockRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.saleRepository = saleRepository;
        this.restockRepository = restockRepository;
    }

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) {
            return;
        }

        ProductCategory electronics = categoryRepository.save(new ProductCategory("Elektronik"));
        ProductCategory office = categoryRepository.save(new ProductCategory("Buero"));
        ProductCategory kitchen = categoryRepository.save(new ProductCategory("Kueche"));

        Product mouse = productRepository.save(new Product("Kabellose Maus", "Ergonomische Bluetooth-Maus mit 2.4G USB-Empfaenger.", electronics,
                new BigDecimal("29.90"), new BigDecimal("12.00"), 15));
        Product keyboard = productRepository.save(new Product("Mechanische Tastatur", "RGB Tastatur mit Blue-Switches und deutschem Layout.", electronics,
                new BigDecimal("89.00"), new BigDecimal("44.00"), 8));
        Product notebook = productRepository.save(new Product("Notizbuch A5", "Hartcover Notizbuch mit 192 Seiten, liniert.", office,
                new BigDecimal("6.50"), new BigDecimal("2.20"), 30));
        Product coffee = productRepository.save(new Product("Kaffee Bohnen 1kg", "Arabica Roestung fuer Vollautomaten.", kitchen,
                new BigDecimal("18.90"), new BigDecimal("9.80"), 5));
        Product bottle = productRepository.save(new Product("Trinkflasche 750ml", "Edelstahlflasche, BPA-frei, auslaufsicher.", kitchen,
                new BigDecimal("24.50"), new BigDecimal("11.50"), 3));

        saveSale(mouse, 4, "Kunde A", new BigDecimal("29.90"), LocalDate.now().minusDays(6));
        saveSale(keyboard, 2, "Kunde B", new BigDecimal("85.00"), LocalDate.now().minusDays(5));
        saveSale(notebook, 12, "Kunde C", new BigDecimal("6.50"), LocalDate.now().minusDays(4));
        saveSale(coffee, 9, "Cafe Mokka", new BigDecimal("18.90"), LocalDate.now().minusDays(3));
        saveSale(bottle, 5, "Kunde D", new BigDecimal("24.50"), LocalDate.now().minusDays(2));

        saveOpenRestock(coffee, 20, "Roesterei Nord", LocalDate.now().plusDays(2));
        RestockOrder bottleRestock = saveOpenRestock(bottle, 12, "Hydro GmbH", LocalDate.now().plusDays(1));
        bottleRestock.markReceived(LocalDate.now().minusDays(1));
        bottle.setStock(bottle.getStock() + bottleRestock.getQuantity());

        productRepository.save(coffee);
        productRepository.save(bottle);
        restockRepository.save(bottleRestock);
    }

    private void saveSale(Product product, int quantity, String customer, BigDecimal price, LocalDate date) {
        saleRepository.save(new SaleTransaction(product, quantity, customer, price, date));
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
    }

    private RestockOrder saveOpenRestock(Product product, int quantity, String supplier, LocalDate expectedDate) {
        RestockOrder order = new RestockOrder(product, quantity, expectedDate, supplier, LocalDate.now().minusDays(1));
        return restockRepository.save(order);
    }
}

