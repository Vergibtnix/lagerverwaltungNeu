package at.vergibtnix.lager.lagerverwaltung.service;

import at.vergibtnix.lager.lagerverwaltung.model.Product;
import at.vergibtnix.lager.lagerverwaltung.model.RestockOrder;
import at.vergibtnix.lager.lagerverwaltung.repository.RestockOrderRepository;
import at.vergibtnix.lager.lagerverwaltung.web.form.RestockForm;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RestockService {

    private final RestockOrderRepository restockRepository;
    private final ProductService productService;
    private final CurrentUserService currentUserService;

    public RestockService(RestockOrderRepository restockRepository,
                          ProductService productService,
                          CurrentUserService currentUserService) {
        this.restockRepository = restockRepository;
        this.productService = productService;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public RestockOrder createRestock(RestockForm form) {
        Product product = productService.getById(form.getProductId());
        var currentUser = currentUserService.getRequiredCurrentUser();
        RestockOrder order = new RestockOrder(
                product,
                currentUser,
                form.getQuantity(),
                form.getExpectedDeliveryDate(),
                form.getSupplier().trim(),
                LocalDate.now(),
                form.getPurchasePrice()
        );
        return restockRepository.save(order);
    }

    @Transactional
    public void confirmReceipt(Long restockId, LocalDate receivedDate) {
        var currentUser = currentUserService.getRequiredCurrentUser();
        RestockOrder order = restockRepository.findById(restockId)
                .orElseThrow(() -> new BusinessRuleException("Nachbestellung nicht gefunden."));
        if (order.getOwner() == null || !order.getOwner().getId().equals(currentUser.getId())) {
            throw new BusinessRuleException("Nachbestellung gehoert zu einem anderen Benutzer.");
        }
        if (order.isCanceled()) {
            throw new BusinessRuleException("Stornierte Nachbestellungen koennen nicht eingebucht werden.");
        }
        if (order.isReceived()) {
            throw new BusinessRuleException("Wareneingang wurde bereits bestaetigt.");
        }
        Product product = order.getProduct();
        // Beim Wareneingang wird der Einkaufspreis als gewichteter Durchschnitt fortgeschrieben.
        product.setPurchasePrice(calculateWeightedAveragePurchasePrice(product, order));
        product.setStock(product.getStock() + order.getQuantity());
        order.markReceived(receivedDate == null ? LocalDate.now() : receivedDate);
    }

    @Transactional
    public void deleteRestock(Long restockId) {
        var currentUser = currentUserService.getRequiredCurrentUser();
        RestockOrder order = restockRepository.findById(restockId)
                .orElseThrow(() -> new BusinessRuleException("Nachbestellung nicht gefunden."));
        if (order.getOwner() == null || !order.getOwner().getId().equals(currentUser.getId())) {
            throw new BusinessRuleException("Nachbestellung gehoert zu einem anderen Benutzer.");
        }
        if (order.isReceived()) {
            throw new BusinessRuleException("Bestaetigte Nachbestellungen koennen nicht storniert werden.");
        }
        if (order.isCanceled()) {
            throw new BusinessRuleException("Nachbestellung wurde bereits storniert.");
        }
        order.cancel(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<RestockOrder> findAll() {
        return restockRepository.findByOwnerUsernameIgnoreCaseOrderByOrderedDateDescIdDesc(currentUserService.getRequiredCurrentUser().getUsername());
    }

    @Transactional(readOnly = true)
    public List<RestockOrder> findByProductId(Long productId) {
        return restockRepository.findByProductIdAndOwnerUsernameIgnoreCaseOrderByOrderedDateDescIdDesc(
                productId,
                currentUserService.getRequiredCurrentUser().getUsername()
        );
    }

    private BigDecimal calculateWeightedAveragePurchasePrice(Product product, RestockOrder order) {
        // Negativer Bestand wird fuer die Durchschnittskalkulation wie 0 behandelt, um Kosten nicht zu verfaelschen.
        int currentStock = Math.max(product.getStock(), 0);
        int incomingStock = order.getQuantity();
        BigDecimal currentCost = product.getPurchasePrice();
        BigDecimal incomingCost = order.getUnitPurchasePrice();

        if (currentStock == 0) {
            return incomingCost;
        }

        BigDecimal currentTotal = currentCost.multiply(BigDecimal.valueOf(currentStock));
        BigDecimal incomingTotal = incomingCost.multiply(BigDecimal.valueOf(incomingStock));
        BigDecimal totalQuantity = BigDecimal.valueOf((long) currentStock + incomingStock);
        // Durchschnitt = (Bestandswert alt + Zugangswert neu) / Gesamtmenge.
        return currentTotal.add(incomingTotal).divide(totalQuantity, 2, RoundingMode.HALF_UP);
    }
}

