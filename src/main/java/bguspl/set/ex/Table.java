package bguspl.set.ex;

import bguspl.set.Env;
import bguspl.set.Hand;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * This class contains the data that is visible to the player.
 *
 * @inv slotToCard[x] == y iff cardToSlot[y] == x
 */
public class Table {

    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * Mapping between a slot and the card placed in it (null if none).
     */
    protected final Object[] locks;
    protected final Integer[] slotToCard; // card per slot (if any)

    /**
     * Mapping between a card and the slot it is in (null if none).
     */
    protected final Integer[] cardToSlot; // slot per card (if any)


    protected LinkedList<Integer>[] tokensTable;


    LinkedBlockingQueue<Hand> playersWithSet;


    volatile boolean tableReady;


    /**
     * Constructor for testing.
     *
     * @param env        - the game environment objects.
     * @param slotToCard - mapping between a slot and the card placed in it (null if none).
     * @param cardToSlot - mapping between a card and the slot it is in (null if none).
     */
    public Table(Env env, Integer[] slotToCard, Integer[] cardToSlot) {

        this.env = env;
        this.slotToCard = slotToCard;
        this.cardToSlot = cardToSlot;
        this.playersWithSet = new LinkedBlockingQueue<>(env.config.players);
        this.tableReady = false;

        locks = new Object[slotToCard.length];
        for (int i = 0; i < slotToCard.length; i++) {
            locks[i]=new Object();
        }
        this.tokensTable = new LinkedList[env.config.tableSize];
        for (int i = 0; i < env.config.tableSize; i++) {
            tokensTable[i] = new LinkedList<>();
        }
    }

    /**
     * Constructor for actual usage.
     *
     * @param env - the game environment objects.
     */
    public Table(Env env) {

        this(env, new Integer[env.config.tableSize], new Integer[env.config.deckSize]);
        this.playersWithSet = new LinkedBlockingQueue<>(env.config.players);
        this.tokensTable = new LinkedList[env.config.tableSize];
        for (int i = 0; i < env.config.tableSize; i++) {
            tokensTable[i] = new LinkedList<>();
        }
    }

    /**
     * This method prints all possible legal sets of cards that are currently on the table.
     */
    public void hints() {
        List<Integer> deck = Arrays.stream(slotToCard).filter(Objects::nonNull).collect(Collectors.toList());
        env.util.findSets(deck, Integer.MAX_VALUE).forEach(set -> {
            StringBuilder sb = new StringBuilder().append("Hint: Set found: ");
            List<Integer> slots = Arrays.stream(set).mapToObj(card -> cardToSlot[card]).sorted().collect(Collectors.toList());
            int[][] features = env.util.cardsToFeatures(set);
        });
    }

    /**
     * Count the number of cards currently on the table.
     *
     * @return - the number of cards on the table.
     */
    public int countCards() {
        int cards = 0;
        for (Integer card : slotToCard)
            if (card != null)
                ++cards;
        return cards;
    }

    /**
     * Places a card on the table in a grid slot.
     *
     * @param card - the card id to place in the slot.
     * @param slot - the slot in which the card should be placed.
     * @post - the card placed is on the table, in the assigned slot.
     */
    public void placeCard(int card, int slot) {
        try {
            Thread.sleep(env.config.tableDelayMillis);
        } catch (InterruptedException ignored) {
        }

        cardToSlot[card] = slot;
        slotToCard[slot] = card;
        env.ui.placeCard(card, slot);
        try {
            Thread.sleep(env.config.tableDelayMillis);
        } catch (InterruptedException e) {
        }
        // TODO implement

    }

    /**
     * Removes a card from a grid slot on the table.
     *
     * @param slot - the slot from which to remove the card.
     */
    public void removeCard(int slot) {
        try {
            Thread.sleep(env.config.tableDelayMillis);
        } catch (InterruptedException ignored) {
        }

        // TODO implement
            Integer card = slotToCard[slot];

            cardToSlot[card] = null;
            slotToCard[slot] = null;
            env.ui.removeCard(slot);
            removeAllTokensInSlot(slot);
    }

    /**
     * Places a player token on a grid slot.
     *
     * @param player - the player the token belongs to.
     * @param slot   - the slot on which to place the token.
     */
    public void placeToken(int player, int slot) {
        // TODO implement
        if (slotToCard[slot] != null) {
            tokensTable[slot].offer(player);
            env.ui.placeToken(player, slot);
        }
    }

    /**
     * Removes a token of a player from a grid slot.
     *
     * @param player - the player the token belongs to.
     * @param slot   - the slot from which to remove the token.
     * @return - true iff a token was successfully removed.
     */
    public boolean removeToken(int player, int slot) {
        // TODO implement
        LinkedList<Integer> tokensInSlot = tokensTable[slot];
        if (tokensInSlot.removeFirstOccurrence(player)) {
            env.ui.removeToken(player, slot);
            return true;
        }
        return false;
    }

    public void removeAllTokensInSlot(int slot) {
        LinkedList<Integer> tokensInTheSlot = tokensTable[slot];
        tokensInTheSlot.clear();
        env.ui.removeTokens(slot);
    }

    public int[] slotsToCardsArray(int[] slots) {
        int[] cardss = new int[3];
        for (int slot = 0; slot < 3; slot++)
            cardss[slot] = slotToCard[slots[slot]];
        return cardss;
    }

    public boolean tokenCheck(int slot, int id) {
        LinkedList<Integer> tokensInTheSlot = tokensTable[slot];
        return (tokensInTheSlot.contains(id));
    }

    public void setTableReady(boolean tableReady) {
        this.tableReady = tableReady;
    }
}
