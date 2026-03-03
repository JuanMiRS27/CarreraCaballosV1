package com.example.carreracaballos.game;

import java.util.List;

public enum SuitSpanish {
    ORO("Oro"),
    ESPADA("Espada"),
    BASTOS("Bastos"),
    COPA("Copa");

    private final String displayName;

    SuitSpanish(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static List<SuitSpanish> ordered() {
        return List.of(ORO, ESPADA, BASTOS, COPA);
    }
}
