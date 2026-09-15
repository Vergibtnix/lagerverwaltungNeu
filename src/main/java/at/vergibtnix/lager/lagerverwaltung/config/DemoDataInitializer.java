package at.vergibtnix.lager.lagerverwaltung.config;

import at.vergibtnix.lager.lagerverwaltung.model.AppUser;
import at.vergibtnix.lager.lagerverwaltung.model.Product;
import at.vergibtnix.lager.lagerverwaltung.model.ProductCategory;
import at.vergibtnix.lager.lagerverwaltung.model.RestockOrder;
import at.vergibtnix.lager.lagerverwaltung.model.SaleTransaction;
import at.vergibtnix.lager.lagerverwaltung.repository.AppUserRepository;
import at.vergibtnix.lager.lagerverwaltung.repository.ProductCategoryRepository;
import at.vergibtnix.lager.lagerverwaltung.repository.ProductRepository;
import at.vergibtnix.lager.lagerverwaltung.repository.RestockOrderRepository;
import at.vergibtnix.lager.lagerverwaltung.repository.SaleTransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DemoDataInitializer implements CommandLineRunner {

    private final ProductCategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final SaleTransactionRepository saleRepository;
    private final RestockOrderRepository restockRepository;
    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoDataInitializer(ProductCategoryRepository categoryRepository,
                               ProductRepository productRepository,
                               SaleTransactionRepository saleRepository,
                               RestockOrderRepository restockRepository,
                               AppUserRepository userRepository,
                               PasswordEncoder passwordEncoder) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.saleRepository = saleRepository;
        this.restockRepository = restockRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) {
            return;
        }

        AppUser demoUser = userRepository.findByUsernameIgnoreCase("demo")
                .orElseGet(() -> userRepository.save(new AppUser("demo", passwordEncoder.encode("demo12345"))));

        ProductCategory electronics = categoryRepository.save(new ProductCategory("Elektronik"));
        ProductCategory office = categoryRepository.save(new ProductCategory("Schreibwaren"));
        ProductCategory food = categoryRepository.save(new ProductCategory("Lebensmittel"));
        categoryRepository.save(new ProductCategory("Haushaltswaren"));
        categoryRepository.save(new ProductCategory("Getraenke"));

        Product mouse = productRepository.save(new Product("ART-1001", "Kabellose Maus", "Ergonomische Bluetooth-Maus mit 2.4G USB-Empfaenger.", electronics,
                new BigDecimal("29.90"), new BigDecimal("12.00"), 15));
        Product keyboard = productRepository.save(new Product("ART-1002", "Mechanische Tastatur", "RGB Tastatur mit Blue-Switches und deutschem Layout.", electronics,
                new BigDecimal("89.00"), new BigDecimal("44.00"), 8));
        Product notebook = productRepository.save(new Product("ART-1003", "Notizbuch A5", "Hartcover Notizbuch mit 192 Seiten, liniert.", office,
                new BigDecimal("6.50"), new BigDecimal("2.20"), 30));
        Product coffee = productRepository.save(new Product("ART-1004", "Kaffee Bohnen 1kg", "Arabica Roestung fuer Vollautomaten.", food,
                new BigDecimal("18.90"), new BigDecimal("9.80"), 5));
        Product bottle = productRepository.save(new Product("ART-1005", "Trinkflasche 750ml", "Edelstahlflasche, BPA-frei, auslaufsicher.", food,
                new BigDecimal("24.50"), new BigDecimal("11.50"), 3));

        saveSale(demoUser, mouse, 4, "Kunde A", new BigDecimal("29.90"), new BigDecimal("12.00"), LocalDate.now().minusDays(6));
        saveSale(demoUser, keyboard, 2, "Kunde B", new BigDecimal("85.00"), new BigDecimal("44.00"), LocalDate.now().minusDays(5));
        saveSale(demoUser, notebook, 12, "Kunde C", new BigDecimal("6.50"), new BigDecimal("2.20"), LocalDate.now().minusDays(4));
        saveSale(demoUser, coffee, 9, "Cafe Mokka", new BigDecimal("18.90"), new BigDecimal("9.80"), LocalDate.now().minusDays(3));
        saveSale(demoUser, bottle, 5, "Kunde D", new BigDecimal("24.50"), new BigDecimal("11.50"), LocalDate.now().minusDays(2));

        saveOpenRestock(demoUser, coffee, 20, new BigDecimal("10.20"), "Roesterei Nord", LocalDate.now().plusDays(2));
        RestockOrder bottleRestock = saveOpenRestock(demoUser, bottle, 12, new BigDecimal("11.20"), "Hydro GmbH", LocalDate.now().plusDays(1));
        bottleRestock.markReceived(LocalDate.now().minusDays(1));
        bottle.setStock(bottle.getStock() + bottleRestock.getQuantity());

        productRepository.save(coffee);
        productRepository.save(bottle);
        restockRepository.save(bottleRestock);
    }

    private void saveSale(AppUser owner,
                          Product product,
                          int quantity,
                          String customer,
                          BigDecimal price,
                          BigDecimal costPrice,
                          LocalDate date) {
        saleRepository.save(new SaleTransaction(product, owner, quantity, customer, price, costPrice, date));
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
    }

    private RestockOrder saveOpenRestock(AppUser owner,
                                         Product product,
                                         int quantity,
                                         BigDecimal purchasePrice,
                                         String supplier,
                                         LocalDate expectedDate) {
        RestockOrder order = new RestockOrder(product, owner, quantity, expectedDate, supplier, LocalDate.now().minusDays(1), purchasePrice);
        return restockRepository.save(order);
    }
}

