package com.example.carreracaballos.game;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RaceGame {
    private final UUID id;
    private final int distance;
    private final List<SuitSpanish> horses;
    private final Map<SuitSpanish, Integer> positions;
    private final List<TurnResult> history;
    private final List<SpanishCard> deck;
    private int turn;
    private int penaltyCheckpoint;
    private SuitSpanish winner;

    public RaceGame(UUID id, int distance, List<SuitSpanish> horses) {
        this.id = id;
        this.distance = distance;
        this.horses = List.copyOf(horses);
        this.positions = new EnumMap<>(SuitSpanish.class);
        this.history = new ArrayList<>();
        this.deck = new ArrayList<>(DeckFactory.shuffledSpanishDeck());
        this.turn = 0;
        this.penaltyCheckpoint = 1;

        for (SuitSpanish suit : horses) {
            positions.put(suit, 0);
        }
    }

    public synchronized TurnResult step() {
        if (winner != null) {
            TurnResult last = history.isEmpty() ? null : history.get(history.size() - 1);
            if (last != null) {
                return last;
            }
        }

        if (deck.isEmpty()) {
            deck.addAll(DeckFactory.shuffledSpanishDeck());
        }

        turn += 1;
        SpanishCard card = deck.remove(0);
        boolean moved = false;
        SuitSpanish movedHorse = null;
        int newPosition = -1;
        boolean penaltyTriggered = false;
        SpanishCard penaltyCard = null;
        boolean penaltyApplied = false;
        SuitSpanish penalizedHorse = null;
        int penalizedNewPosition = -1;

        if (positions.containsKey(card.suit())) {
            moved = true;
            movedHorse = card.suit();
            newPosition = positions.get(movedHorse) + 1;
            positions.put(movedHorse, newPosition);
        }

        if (allHorsesReachedCheckpoint() && winner == null) {
            penaltyTriggered = true;
            penaltyCheckpoint += 1;
            penaltyCard = drawCard();
            if (positions.containsKey(penaltyCard.suit())) {
                penaltyApplied = true;
                penalizedHorse = penaltyCard.suit();
                penalizedNewPosition = Math.max(0, positions.get(penalizedHorse) - 1);
                positions.put(penalizedHorse, penalizedNewPosition);
            }
        }

        updateWinnerIfAny();

        TurnResult result = new TurnResult(
                turn,
                card,
                moved,
                movedHorse,
                newPosition,
                penaltyTriggered,
                penaltyCard,
                penaltyApplied,
                penalizedHorse,
                penalizedNewPosition,
                winner
        );
        history.add(result);
        return result;
    }

    private boolean allHorsesReachedCheckpoint() {
        for (SuitSpanish horse : horses) {
            if (positions.get(horse) < penaltyCheckpoint) {
                return false;
            }
        }
        return true;
    }

    private SpanishCard drawCard() {
        if (deck.isEmpty()) {
            deck.addAll(DeckFactory.shuffledSpanishDeck());
        }
        return deck.remove(0);
    }

    private void updateWinnerIfAny() {
        if (winner != null) {
            return;
        }
        for (SuitSpanish horse : horses) {
            if (positions.get(horse) >= distance) {
                winner = horse;
                return;
            }
        }
    }

    public UUID getId() {
        return id;
    }

    public int getDistance() {
        return distance;
    }

    public List<SuitSpanish> getHorses() {
        return horses;
    }

    public Map<SuitSpanish, Integer> getPositions() {
        return Map.copyOf(positions);
    }

    public List<TurnResult> getHistory() {
        return List.copyOf(history);
    }

    public int getTurn() {
        return turn;
    }

    public SuitSpanish getWinner() {
        return winner;
    }
}
