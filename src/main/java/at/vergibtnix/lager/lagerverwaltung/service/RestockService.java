package at.vergibtnix.lager.lagerverwaltung.service;

import at.vergibtnix.lager.lagerverwaltung.model.Product;
import at.vergibtnix.lager.lagerverwaltung.model.RestockOrder;
import at.vergibtnix.lager.lagerverwaltung.repository.RestockOrderRepository;
import at.vergibtnix.lager.lagerverwaltung.web.form.RestockForm;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RestockService {

    private final RestockOrderRepository restockRepository;
    private final ProductService productService;

    public RestockService(RestockOrderRepository restockRepository, ProductService productService) {
        this.restockRepository = restockRepository;
        this.productService = productService;
    }

    @Transactional
    public RestockOrder createRestock(RestockForm form) {
        Product product = productService.getById(form.getProductId());
        RestockOrder order = new RestockOrder(
                product,
                form.getQuantity(),
                form.getExpectedDeliveryDate(),
                form.getSupplier().trim(),
                LocalDate.now()
        );
        return restockRepository.save(order);
    }

    @Transactional
    public void confirmReceipt(Long restockId, LocalDate receivedDate) {
        RestockOrder order = restockRepository.findById(restockId)
                .orElseThrow(() -> new BusinessRuleException("Nachbestellung nicht gefunden."));
        if (order.isReceived()) {
            throw new BusinessRuleException("Wareneingang wurde bereits bestaetigt.");
        }
        Product product = order.getProduct();
        product.setStock(product.getStock() + order.getQuantity());
        order.markReceived(receivedDate == null ? LocalDate.now() : receivedDate);
    }

    @Transactional
    public void deleteRestock(Long restockId) {
        RestockOrder order = restockRepository.findById(restockId)
                .orElseThrow(() -> new BusinessRuleException("Nachbestellung nicht gefunden."));
        if (order.isReceived()) {
            throw new BusinessRuleException("Bestaetigte Nachbestellungen koennen nicht geloescht werden.");
        }
        restockRepository.delete(order);
    }

    @Transactional(readOnly = true)
    public List<RestockOrder> findAll() {
        return restockRepository.findAllByOrderByOrderedDateDescIdDesc();
    }

    @Transactional(readOnly = true)
    public List<RestockOrder> findByProductId(Long productId) {
        return restockRepository.findByProductIdOrderByOrderedDateDescIdDesc(productId);
    }
}

