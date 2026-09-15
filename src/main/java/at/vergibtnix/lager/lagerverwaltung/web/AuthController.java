package at.vergibtnix.lager.lagerverwaltung.web;

import at.vergibtnix.lager.lagerverwaltung.model.AppUser;
import at.vergibtnix.lager.lagerverwaltung.repository.AppUserRepository;
import at.vergibtnix.lager.lagerverwaltung.web.form.RegistrationForm;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        if (!model.containsAttribute("registrationForm")) {
            model.addAttribute("registrationForm", new RegistrationForm());
        }
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registrationForm") RegistrationForm form,
                           BindingResult bindingResult,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (!form.getPassword().equals(form.getPasswordConfirm())) {
            bindingResult.rejectValue("passwordConfirm", "password.mismatch", "Passwort und Bestaetigung stimmen nicht ueberein.");
        }

        if (userRepository.findByUsernameIgnoreCase(form.getUsername()).isPresent()) {
            bindingResult.rejectValue("username", "username.duplicate", "Benutzername ist bereits vergeben.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("registrationForm", form);
            return "register";
        }

        userRepository.save(new AppUser(form.getUsername().trim(), passwordEncoder.encode(form.getPassword())));
        redirectAttributes.addFlashAttribute("successMessage", "Konto wurde erstellt. Bitte anmelden.");
        return "redirect:/login";
    }
}

