(function () {
    var STORAGE_KEY = "lap-theme";
    var root = document.documentElement;

    function preferredTheme() {
        if (!window.matchMedia) {
            return "light";
        }
        return window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light";
    }

    function currentTheme() {
        var saved = localStorage.getItem(STORAGE_KEY);
        if (saved === "dark" || saved === "light") {
            return saved;
        }
        return preferredTheme();
    }

    function applyTheme(theme) {
        root.setAttribute("data-bs-theme", theme);
    }

    function updateToggleText(button, theme) {
        button.textContent = theme === "dark" ? "Hell" : "Dunkel";
        button.setAttribute("aria-label", theme === "dark" ? "Auf helles Design umschalten" : "Auf dunkles Design umschalten");
    }

    function initToggle() {
        var button = document.getElementById("themeToggle");
        if (!button) {
            return;
        }

        updateToggleText(button, currentTheme());
        button.addEventListener("click", function () {
            var next = root.getAttribute("data-bs-theme") === "dark" ? "light" : "dark";
            applyTheme(next);
            localStorage.setItem(STORAGE_KEY, next);
            updateToggleText(button, next);
        });
    }

    applyTheme(currentTheme());
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", initToggle);
    } else {
        initToggle();
    }
})();

