package at.vergibtnix.lager.lagerverwaltung.service;

import at.vergibtnix.lager.lagerverwaltung.model.Product;
import at.vergibtnix.lager.lagerverwaltung.model.SaleTransaction;
import at.vergibtnix.lager.lagerverwaltung.repository.SaleTransactionRepository;
import at.vergibtnix.lager.lagerverwaltung.web.form.SaleForm;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SaleService {

    private final SaleTransactionRepository saleRepository;
    private final ProductService productService;
    private final CurrentUserService currentUserService;

    public SaleService(SaleTransactionRepository saleRepository, ProductService productService, CurrentUserService currentUserService) {
        this.saleRepository = saleRepository;
        this.productService = productService;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public SaleTransaction recordSale(SaleForm form) {
        Product product = productService.getById(form.getProductId());
        var currentUser = currentUserService.getRequiredCurrentUser();
        BigDecimal unitPrice = form.getUnitPrice() == null ? product.getSalePrice() : form.getUnitPrice();
        BigDecimal costPrice = form.getCostPrice();
        LocalDate saleDate = form.getSaleDate() == null ? LocalDate.now() : form.getSaleDate();
        String customerName = form.getCustomerName() == null || form.getCustomerName().isBlank()
                ? null
                : form.getCustomerName().trim();

        SaleTransaction sale = new SaleTransaction(product, currentUser, form.getQuantity(), customerName, unitPrice, costPrice, saleDate);
        product.setStock(product.getStock() - form.getQuantity());
        return saleRepository.save(sale);
    }

    @Transactional
    public void deleteSale(Long saleId) {
        var currentUser = currentUserService.getRequiredCurrentUser();
        SaleTransaction sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new BusinessRuleException("Verkauf nicht gefunden."));
        if (sale.getOwner() == null || !sale.getOwner().getId().equals(currentUser.getId())) {
            throw new BusinessRuleException("Verkauf gehoert zu einem anderen Benutzer.");
        }
        if (sale.isCanceled()) {
            throw new BusinessRuleException("Verkauf wurde bereits storniert.");
        }
        Product product = sale.getProduct();
        product.setStock(product.getStock() + sale.getQuantity());
        sale.cancel(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<SaleTransaction> findAll() {
        return saleRepository.findByOwnerUsernameIgnoreCaseOrderBySaleDateDescIdDesc(currentUserService.getRequiredCurrentUser().getUsername());
    }

    @Transactional(readOnly = true)
    public List<SaleTransaction> findByProductId(Long productId) {
        return saleRepository.findByProductIdAndOwnerUsernameIgnoreCaseOrderBySaleDateDescIdDesc(
                productId,
                currentUserService.getRequiredCurrentUser().getUsername()
        );
    }
}

