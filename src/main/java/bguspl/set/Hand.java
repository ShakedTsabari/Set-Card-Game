package bguspl.set;

import bguspl.set.ex.Player;

import java.util.Arrays;

public class Hand {

    private final Player player;

    private final int[] cards;


    public Hand(Player player, int[] cards) {
        this.player = player;
        this.cards = cards;
    }
    public Hand(Hand hand) {
        this.player = hand.player;
        this.cards = hand.cards;
    }


        public Player getPlayer() {
        return player;
    }

    public int[] getSlotOfTokens() {
        return cards;
    }

    public int getCard(int slot) {
        return cards[slot];
    }

    public void removeCardFromSet(int slot) {
        boolean flag = true;
        for (int i = 0; i < 3 & flag; i++) {
            if (cards[i] == slot) {
                cards[i] = -1;
                flag = false;
            }
        }
    }

    public void addCardToSet(int slot) {
        boolean flag = true;
        for (int i = 0; i < 3 & flag; i++) {
            if (cards[i] == -1) {
                cards[i] = slot;
                flag = false;
            }
        }
    }

    @Override
    public String toString() {
        return "Hand{" +
                "player=" + player +
                ", cards=" + Arrays.toString(cards) +
                '}';
    }

    public void resetHand() {
        for (int i = 0; i < 3; i++) {
            cards[i] = -1;
        }
    }
}
