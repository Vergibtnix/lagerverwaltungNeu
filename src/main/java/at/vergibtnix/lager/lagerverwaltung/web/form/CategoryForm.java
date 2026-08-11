package at.vergibtnix.lager.lagerverwaltung.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CategoryForm {

    @NotBlank(message = "Kategoriename ist erforderlich.")
    @Size(max = 100, message = "Kategoriename darf maximal 100 Zeichen haben.")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

