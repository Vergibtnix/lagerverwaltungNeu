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

    public SaleService(SaleTransactionRepository saleRepository, ProductService productService) {
        this.saleRepository = saleRepository;
        this.productService = productService;
    }

    @Transactional
    public SaleTransaction recordSale(SaleForm form) {
        Product product = productService.getById(form.getProductId());
        BigDecimal unitPrice = form.getUnitPrice() == null ? product.getSalePrice() : form.getUnitPrice();
        LocalDate saleDate = form.getSaleDate() == null ? LocalDate.now() : form.getSaleDate();
        String customerName = form.getCustomerName() == null || form.getCustomerName().isBlank()
                ? null
                : form.getCustomerName().trim();

        SaleTransaction sale = new SaleTransaction(product, form.getQuantity(), customerName, unitPrice, saleDate);
        product.setStock(product.getStock() - form.getQuantity());
        return saleRepository.save(sale);
    }

    @Transactional
    public void deleteSale(Long saleId) {
        SaleTransaction sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new BusinessRuleException("Verkauf nicht gefunden."));
        Product product = sale.getProduct();
        product.setStock(product.getStock() + sale.getQuantity());
        saleRepository.delete(sale);
    }

    @Transactional(readOnly = true)
    public List<SaleTransaction> findAll() {
        return saleRepository.findAllByOrderBySaleDateDescIdDesc();
    }

    @Transactional(readOnly = true)
    public List<SaleTransaction> findByProductId(Long productId) {
        return saleRepository.findByProductIdOrderBySaleDateDescIdDesc(productId);
    }
}

