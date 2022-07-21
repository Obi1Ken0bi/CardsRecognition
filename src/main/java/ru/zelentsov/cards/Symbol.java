package ru.zelentsov.cards;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
class Symbol {
    private Boolean[][] pixels;
    private String value;

    public Symbol(Boolean[][] pixels) {
        this.pixels = pixels;
    }

}
