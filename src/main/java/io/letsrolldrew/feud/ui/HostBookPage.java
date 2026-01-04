package io.letsrolldrew.feud.ui;

public enum HostBookPage {
    CONTROL,
    SURVEYS,
    SELECTOR,
    FAST_MONEY,
    FAST_MONEY_CONFIG;

    public String token() {
        return name().toLowerCase();
    }

    public static HostBookPage fromToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }

        String uppercaseToken = token.trim().toUpperCase();
        for (HostBookPage page : values()) {
            if (page.name().equals(uppercaseToken)) {
                return page;
            }
        }

        return null;
    }
}
