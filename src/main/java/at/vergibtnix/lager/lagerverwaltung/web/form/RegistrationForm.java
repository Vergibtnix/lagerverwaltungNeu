package at.vergibtnix.lager.lagerverwaltung.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegistrationForm {

    @NotBlank(message = "Benutzername ist erforderlich.")
    @Size(min = 3, max = 100, message = "Benutzername muss zwischen 3 und 100 Zeichen lang sein.")
    private String username;

    @NotBlank(message = "Passwort ist erforderlich.")
    @Size(min = 8, max = 120, message = "Passwort muss zwischen 8 und 120 Zeichen lang sein.")
    private String password;

    @NotBlank(message = "Passwort-Bestaetigung ist erforderlich.")
    private String passwordConfirm;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordConfirm() {
        return passwordConfirm;
    }

    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }
}

