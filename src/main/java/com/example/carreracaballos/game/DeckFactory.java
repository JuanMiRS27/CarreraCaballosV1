package com.example.carreracaballos.game;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class DeckFactory {

    private static final List<Integer> SPANISH_VALUES = List.of(1, 2, 3, 4, 5, 6, 7, 10, 11, 12);
    private static final Random RANDOM = new SecureRandom();

    private DeckFactory() {
    }

    public static List<SpanishCard> shuffledSpanishDeck() {
        List<SpanishCard> cards = new ArrayList<>();
        for (SuitSpanish suit : SuitSpanish.ordered()) {
            for (Integer value : SPANISH_VALUES) {
                cards.add(new SpanishCard(suit, value));
            }
        }
        Collections.shuffle(cards, RANDOM);
        return cards;
    }
}
