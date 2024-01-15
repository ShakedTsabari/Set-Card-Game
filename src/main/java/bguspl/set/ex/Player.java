package bguspl.set.ex;

import bguspl.set.Env;
import bguspl.set.Hand;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class manages the players' threads and data
 *
 * @inv id >= 0
 * @inv score >= 0
 */
public class Player implements Runnable {

    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * Game entities.
     */
    private final Table table;

    /**
     * The id of the player (starting from 0).
     */
    public final int id;

    /**
     * The thread representing the current player.
     */
    private Thread playerThread;

    /**
     * The thread of the AI (computer) player (an additional thread used to generate key presses).
     */
    private Thread aiThread;

    /**
     * True iff the player is human (not a computer player).
     */
    private final boolean human;

    /**
     * True iff game should be terminated.
     */
    private volatile boolean terminate;

    /**
     * The current score of the player.
     */
    private int score;

    private BlockingQueue<Integer> actionsQueue;


    private Hand hand;

    private String name;
    private boolean isInPenalty = false;

    private boolean isInPoint = false;

    /**
     * The class constructor.
     *
     * @param env    - the environment object.
     * @param dealer - the dealer object.
     * @param table  - the table object.
     * @param id     - the id of the player.
     * @param human  - true iff the player is a human player (i.e. input is provided manually, via the keyboard).
     */
    public Player(Env env, Dealer dealer, Table table, int id, boolean human) {
        this.env = env;
        this.table = table;
        this.id = id;
        this.human = human;
        this.hand = new Hand(this, new int[]{-1, -1, -1});
        this.actionsQueue = new LinkedBlockingQueue<>(env.config.featureSize);
        this.name = Thread.currentThread().getName();
        this.terminate = false;
    }

    /**
     * The main player thread of each player starts here (main loop for the player thread).
     */
    @Override
    public void run() {
        playerThread = Thread.currentThread();
        env.logger.info("thread " + Thread.currentThread().getName() + " starting.");
        if (!human) createArtificialIntelligence();

        while (!terminate) {
            if (this.isInPenalty)
                penaltyOrPointTimer(env.config.penaltyFreezeMillis);
            else if (this.isInPoint)
                penaltyOrPointTimer(env.config.pointFreezeMillis);

            // TODO implement main player loop
            Integer slot = null;
            try {
                slot = actionsQueue.take();
            } catch (InterruptedException e) {
            }
            if (slot != null & table.tableReady) {
                    if (table.removeToken(id, slot))
                        hand.removeCardFromSet(slot);
                    else if (numOfTokens() < 3) {
                        synchronized (table.locks[slot]) {
                            table.placeToken(id, slot);
                        }
                        hand.addCardToSet(slot);
                        if (numOfTokens() == 3) {
                            try {
                                synchronized (this) {
                                    table.playersWithSet.offer(hand);
                                    this.wait();
                                }
                            } catch (InterruptedException e) {
                            }
                        }
                    }
                }
        }
        if (!human) try {
            aiThread.join();
        } catch (InterruptedException ignored) {
        }
        env.logger.info("thread " + Thread.currentThread().getName() + " terminated.");
    }

    /**
     * Creates an additional thread for an AI (computer) player. The main loop of this thread repeatedly generates
     * key presses. If the queue of key presses is full, the thread waits until it is not full.
     */
    private void createArtificialIntelligence() {
        // note: this is a very, very smart AI (!)
        aiThread = new Thread(() -> {
            env.logger.info("thread " + Thread.currentThread().getName() + " starting.");
            // TODO implement player key press simulator
            Random random = new Random();
            while (!terminate) {
                int real_size = 0;
                for (int i = 0; i < env.config.tableSize; i++) {
                    if (table.slotToCard[i]!=null)
                        real_size++;
                }
                if(real_size==0)
                {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    continue;
                }
                actionsQueue.offer(random.nextInt(real_size));
            }
            env.logger.info("thread " + Thread.currentThread().getName() + " terminated.");
        }, "computer-" + id);
        aiThread.start();

    }


    /**
     * Called when the game should be terminated.
     */
    public void terminate() {
        // TODO implement
        this.terminate = true;
    }

    /**
     * This method is called when a key is pressed.
     *
     * @param slot - the slot corresponding to the key pressed.
     */
    public void keyPressed(int slot) {
        // TODO implement
        if (numOfTokens() == 3) {
            boolean flag = false;
            for (int i = 0; i < 3 & !flag; i++) {
                if (slot == hand.getSlotOfTokens()[i]) {
                    actionsQueue.offer(slot);
                    flag = true;
                }
            }
        } else if (numOfTokens() < 3) {
            actionsQueue.offer(slot);
        }
    }

    /**
     * Award a point to a player and perform other related actions.
     *
     * @post - the player's score is increased by 1.
     * @post - the player's score is updated in the ui.
     */
    public void point() {
        // TODO implement
        this.score++;
        env.ui.setScore(id, score);
        actionsQueue.clear();
        this.isInPoint = true;
        int ignored = table.countCards(); // this part is just for demonstration in the unit tests
    }

    /**
     * Penalize a player and perform other related actions.
     */
    public void penalty() {
        // TODO implement
        actionsQueue.clear();
        this.isInPenalty= true;
    }

    public int score() {
        return score;
    }

    public int numOfTokens() {
        int counter = 0;
        int[] set = hand.getSlotOfTokens();
        for (int i = 0; i < set.length; i++) {
            if (set[i] != -1 && table.tokenCheck(set[i], id))
                counter++;
        }
        return counter;
    }

    public Thread getPlayerThread() {
        return playerThread;
    }

    public void setPlayerThread(Thread playerThread) {
        this.playerThread = playerThread;
    }

    public Hand getHand() {
        return hand;
    }

    public void updateHand(int slot) {
        for (int token : hand.getSlotOfTokens())
            if (token == slot)
                hand.removeCardFromSet(slot);
    }

    public void terminateThread(){
        this.terminate();
    }

    public boolean is_penalty() {
        return this.isInPenalty;
    }


    public void penaltyOrPointTimer (long time){
            try {
                long penaltyOrPointDurationMil = time;
                long penaltyDurationSec = penaltyOrPointDurationMil/1000;
                for (int i = 0; i < penaltyDurationSec; i++) {
                    env.ui.setFreeze(id, penaltyOrPointDurationMil-i * 1000L);
                    Thread.sleep(1000);
                }
                env.ui.setFreeze(id, 0);
                isInPenalty=false;
                isInPoint = false;

            } catch (InterruptedException ignored) {
            }
        }
}
