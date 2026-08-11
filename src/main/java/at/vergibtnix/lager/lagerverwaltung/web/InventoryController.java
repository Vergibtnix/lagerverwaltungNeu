package at.vergibtnix.lager.lagerverwaltung.web;

import at.vergibtnix.lager.lagerverwaltung.model.Product;
import at.vergibtnix.lager.lagerverwaltung.service.BusinessRuleException;
import at.vergibtnix.lager.lagerverwaltung.service.CategoryService;
import at.vergibtnix.lager.lagerverwaltung.service.ProductService;
import at.vergibtnix.lager.lagerverwaltung.service.ReportingService;
import at.vergibtnix.lager.lagerverwaltung.service.RestockService;
import at.vergibtnix.lager.lagerverwaltung.service.SaleService;
import at.vergibtnix.lager.lagerverwaltung.web.form.CategoryForm;
import at.vergibtnix.lager.lagerverwaltung.web.form.ProductFilterForm;
import at.vergibtnix.lager.lagerverwaltung.web.form.ProductForm;
import at.vergibtnix.lager.lagerverwaltung.web.form.RestockForm;
import at.vergibtnix.lager.lagerverwaltung.web.form.SaleForm;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class InventoryController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final SaleService saleService;
    private final RestockService restockService;
    private final ReportingService reportingService;

    public InventoryController(ProductService productService,
                               CategoryService categoryService,
                               SaleService saleService,
                               RestockService restockService,
                               ReportingService reportingService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.saleService = saleService;
        this.restockService = restockService;
        this.reportingService = reportingService;
    }

    @GetMapping("/")
    public String dashboard(@ModelAttribute("filter") ProductFilterForm filter, Model model) {
        populateCommon(model);
        model.addAttribute("products", productService.findAllFiltered(filter));
        return "dashboard";
    }

    @GetMapping("/products/new")
    public String newProduct(Model model) {
        populateCommon(model);
        if (!model.containsAttribute("productForm")) {
            ProductForm form = new ProductForm();
            form.setStock(0);
            model.addAttribute("productForm", form);
        }
        if (!model.containsAttribute("categoryForm")) {
            model.addAttribute("categoryForm", new CategoryForm());
        }
        return "product-form";
    }

    @PostMapping("/categories")
    public String createCategory(@Valid @ModelAttribute("categoryForm") CategoryForm categoryForm,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            populateCommon(model);
            if (!model.containsAttribute("productForm")) {
                ProductForm form = new ProductForm();
                form.setStock(0);
                model.addAttribute("productForm", form);
            }
            return "product-form";
        }
        try {
            categoryService.createCategory(categoryForm);
            redirectAttributes.addFlashAttribute("successMessage", "Kategorie wurde erstellt.");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/products/new";
    }

    @PostMapping("/products")
    public String createProduct(@Valid @ModelAttribute("productForm") ProductForm productForm,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populateCommon(model);
            model.addAttribute("categoryForm", new CategoryForm());
            return "product-form";
        }
        try {
            Product product = productService.createProduct(productForm);
            redirectAttributes.addFlashAttribute("successMessage", "Produkt wurde erstellt.");
            return "redirect:/products/" + product.getId();
        } catch (BusinessRuleException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            populateCommon(model);
            model.addAttribute("categoryForm", new CategoryForm());
            return "product-form";
        }
    }

    @GetMapping("/products/{productId}")
    public String productDetails(@PathVariable Long productId, Model model) {
        Product product = productService.getById(productId);
        populateCommon(model);
        if (!model.containsAttribute("productForm")) {
            model.addAttribute("productForm", mapProduct(product));
        }
        model.addAttribute("product", product);
        model.addAttribute("sales", saleService.findByProductId(productId));
        model.addAttribute("restocks", restockService.findByProductId(productId));
        return "product-details";
    }

    @PostMapping("/products/{productId}")
    public String updateProduct(@PathVariable Long productId,
                                @Valid @ModelAttribute("productForm") ProductForm productForm,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            Product product = productService.getById(productId);
            populateCommon(model);
            model.addAttribute("product", product);
            model.addAttribute("sales", saleService.findByProductId(productId));
            model.addAttribute("restocks", restockService.findByProductId(productId));
            return "product-details";
        }
        try {
            productService.updateProduct(productId, productForm);
            redirectAttributes.addFlashAttribute("successMessage", "Produkt wurde aktualisiert.");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/products/" + productId;
    }

    @GetMapping("/sales/new")
    public String newSale(Model model) {
        populateCommon(model);
        if (!model.containsAttribute("saleForm")) {
            model.addAttribute("saleForm", new SaleForm());
        }
        return "sale-form";
    }

    @PostMapping("/sales")
    public String createSale(@Valid @ModelAttribute("saleForm") SaleForm saleForm,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populateCommon(model);
            return "sale-form";
        }
        try {
            saleService.recordSale(saleForm);
            redirectAttributes.addFlashAttribute("successMessage", "Verkauf wurde erfasst.");
            return "redirect:/transactions";
        } catch (BusinessRuleException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            populateCommon(model);
            return "sale-form";
        }
    }

    @PostMapping("/sales/{saleId}/delete")
    public String deleteSale(@PathVariable Long saleId, RedirectAttributes redirectAttributes) {
        try {
            saleService.deleteSale(saleId);
            redirectAttributes.addFlashAttribute("successMessage", "Verkauf wurde geloescht und Bestand korrigiert.");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/transactions";
    }

    @GetMapping("/restocks/new")
    public String newRestock(Model model) {
        populateCommon(model);
        if (!model.containsAttribute("restockForm")) {
            RestockForm form = new RestockForm();
            form.setExpectedDeliveryDate(LocalDate.now().plusDays(7));
            model.addAttribute("restockForm", form);
        }
        return "restock-form";
    }

    @PostMapping("/restocks")
    public String createRestock(@Valid @ModelAttribute("restockForm") RestockForm restockForm,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populateCommon(model);
            return "restock-form";
        }
        try {
            restockService.createRestock(restockForm);
            redirectAttributes.addFlashAttribute("successMessage", "Nachbestellung wurde erfasst.");
            return "redirect:/transactions";
        } catch (BusinessRuleException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            populateCommon(model);
            return "restock-form";
        }
    }

    @PostMapping("/restocks/{restockId}/receive")
    public String confirmRestock(@PathVariable Long restockId, RedirectAttributes redirectAttributes) {
        try {
            restockService.confirmReceipt(restockId, LocalDate.now());
            redirectAttributes.addFlashAttribute("successMessage", "Wareneingang wurde bestaetigt.");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/transactions";
    }

    @PostMapping("/restocks/{restockId}/delete")
    public String deleteRestock(@PathVariable Long restockId, RedirectAttributes redirectAttributes) {
        try {
            restockService.deleteRestock(restockId);
            redirectAttributes.addFlashAttribute("successMessage", "Nachbestellung wurde geloescht.");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/transactions";
    }

    @GetMapping("/transactions")
    public String transactions(Model model) {
        populateCommon(model);
        model.addAttribute("transactions", reportingService.getTransactionHistory());
        model.addAttribute("profitRows", reportingService.getProfitByProduct());
        return "transactions";
    }

    private void populateCommon(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("allProducts", productService.findAll());
    }

    private ProductForm mapProduct(Product product) {
        ProductForm form = new ProductForm();
        form.setName(product.getName());
        form.setDescription(product.getDescription());
        form.setCategoryId(product.getCategory().getId());
        form.setSalePrice(product.getSalePrice());
        form.setPurchasePrice(product.getPurchasePrice());
        form.setStock(product.getStock());
        return form;
    }
}

