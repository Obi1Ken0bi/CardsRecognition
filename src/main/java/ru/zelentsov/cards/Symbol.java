package ru.zelentsov.cards;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
class Symbol {
    private Boolean[][] possiblePixels;
    private String value;


    public Symbol(Boolean[][] possiblePixels) {
        this.possiblePixels = possiblePixels;
    }

}
