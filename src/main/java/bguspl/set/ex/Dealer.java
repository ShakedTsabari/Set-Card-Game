package bguspl.set.ex;

import bguspl.set.Env;
import bguspl.set.Hand;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class manages the dealer's threads and data
 */
public class Dealer implements Runnable {

    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * Game entities.
     */
    private final Table table;
    private final Player[] players;

    /**
     * The list of card ids that are left in the dealer's deck.
     */
    private final List<Integer> deck;

    /**
     * True iff game should be terminated.
     */
    private volatile boolean terminate;

    /**
     * The time when the dealer needs to reshuffle the deck due to turn timeout.
     */
    private long reshuffleTime = Long.MAX_VALUE;


    public Dealer(Env env, Table table, Player[] players) {
        this.env = env;
        this.table = table;
        this.players = players;
        deck = IntStream.range(0, env.config.deckSize).boxed().collect(Collectors.toList());
        this.terminate = false;
    }

    /**
     * The dealer thread starts here (main loop for the dealer thread).
     */
    @Override
    public void run() {
        env.logger.info("thread " + Thread.currentThread().getName() + " starting.");
        for (Player p : players) {
            Thread playerThread = new Thread(p);
            p.setPlayerThread(playerThread);
            p.getPlayerThread().start();
        }
        while (!shouldFinish()) {
            placeCardsOnTable(env.config.tableSize);
            table.hints();
            timerLoop();
            updateTimerDisplay(true);
            removeAllCardsFromTable();
        }
        announceWinners();
        env.logger.info("thread " + Thread.currentThread().getName() + " terminated.");

        for (Player p : players) {
            p.terminateThread();
        }
    }

    /**
     * The inner loop of the dealer thread that runs as long as the countdown did not time out.
     */
    private void timerLoop() {
        this.reshuffleTime = System.currentTimeMillis() + env.config.turnTimeoutMillis;
        while (!terminate && System.currentTimeMillis() < reshuffleTime) {
            sleepUntilWokenOrTimeout();
            updateTimerDisplay(false);
        }
    }

    /**
     * Called when the game should be terminated.
     */
    public void terminate() {
        // TODO implement
        terminate = true;
    }

    /**
     * Check if the game should be terminated or the game end conditions are met.
     *
     * @return true iff the game should be finished.
     */
    private boolean shouldFinish() {
        return terminate || env.util.findSets(deck, 1).size() == 0;
    }

    /**
     * Checks cards should be removed from the table and removes them.
     */
    private void replaceCardsFromTable(Hand hand) {
        // TODO implement
        table.setTableReady(false);
        for (int slot : hand.getSlotOfTokens()) {
            if (slot != -1) {
                synchronized (table.locks[slot]) {
                    this.removeCard(slot);
                }
                deck.remove(table.slotToCard[slot]);
                for (Player p : players)
                    p.updateHand(slot);
            }
        }
        placeCardsOnTable(3);
        table.setTableReady(true);
    }

    public void removeCard(int slot) {
        table.removeCard(slot);
        for (Player player : this.players) {
            player.getHand().removeCardFromSet(slot);
        }
    }


    /**
     * Check if any cards can be removed from the deck and placed on the table.
     */
    public void placeCardsOnTable(int numOfCardsToPlace) {
        // TODO implement
        Random random = new Random();
        int numOfCardToPlace = numOfCardsToPlace;
        if (deck.size() < numOfCardsToPlace)
            numOfCardToPlace = deck.size();

        for (int slot = 0; slot < numOfCardToPlace; slot++) {
            for (int i = 0; i < env.config.tableSize; i++) {
                if (table.slotToCard[i] == null & deck.size() > 0) {
                    Integer card = deck.get(random.nextInt(deck.size()));
                    table.placeCard(card, i);
                    deck.remove(card);
                }
            }
        }
        table.setTableReady(true);
    }

    /**
     * Sleep for a fixed amount of time or until the thread is awakened for some purpose.
     */
    private void sleepUntilWokenOrTimeout() {
        // TODO implement
        Hand hand = null;
        try {
            boolean warning = this.isWarning();//?10:1000;
            if (warning) {
                hand = table.playersWithSet.poll(10, TimeUnit.MILLISECONDS);

            } else
                hand = table.playersWithSet.poll(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
        }
        if (hand != null) {
            Player p = hand.getPlayer();
            if (p.numOfTokens() >= 3 && !p.is_penalty()) {
                int[] set = table.slotsToCardsArray(hand.getSlotOfTokens());//      ^ identify it by bool?
                if (env.util.testSet(set)) {
                    replaceCardsFromTable(hand);
                    p.point();
                    hand.resetHand();
                } else {
                    p.penalty();
                }
            }
            synchronized (p) {
                p.notify();
            }
        }
    }

    private boolean isWarning() {
        return (reshuffleTime - System.currentTimeMillis()) < env.config.turnTimeoutWarningMillis;
    }


    /**
     * Reset and/or update the countdown and the countdown display.
     */
    private void updateTimerDisplay(boolean reset) {
        // TODO implement
        if (reset) {
            reshuffleTime = env.config.turnTimeoutMillis + System.currentTimeMillis();
            env.ui.setCountdown(env.config.turnTimeoutMillis, false);
        } else {
            long time = reshuffleTime - System.currentTimeMillis();
            env.ui.setCountdown(
                    time,
                    time < env.config.turnTimeoutWarningMillis);
        }
        ;
    }

    /**
     * Returns all the cards from the table to the deck.
     */
    public void removeAllCardsFromTable() {
        // TODO implement
        table.setTableReady(false);
        for (int slot = 0; slot < env.config.tableSize; slot++) {
            Integer card = table.slotToCard[slot];
            if (card != null) {
                synchronized (table.locks[slot]) {
                    deck.add(card);
                    this.removeCard(slot);
                }
            }
        }
    }

    /**
     * Check who is/are the winner/s and displays them.
     */
    private void announceWinners() {
        // TODO implement
        int max = 0;
        for (Player p : players) {
            if (p.score() > max)
                max = p.score();
        }
        Queue<Player> winners = new LinkedList<>();
        for (Player p : players) {
            if (p.score() == max)
                winners.add(p);
        }
        int[] winnersToAnnounce = new int[winners.size()];
        for (int i = 0; i < winners.size(); i++) {
            winnersToAnnounce[i] = winners.poll().id;
        }
        env.ui.announceWinner(winnersToAnnounce);
    }
}
