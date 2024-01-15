package bguspl.set.ex;

import bguspl.set.Config;
import bguspl.set.Env;
import bguspl.set.UserInterface;
import bguspl.set.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)


class DealerTest {

    Dealer dealer;
    @Mock
    Util util;
    @Mock
    private UserInterface ui;

    @Mock
    private Logger logger;

    @Mock
    private  List<Integer> deck;

    private Table table;


    private volatile boolean terminate;


    @BeforeEach
    void setUp() {
        Env env = new Env(logger, new Config(logger, (String) null), ui, util);
        table = new Table(env);
        Player player = new Player(env, dealer, table, 0, false);
        Player[] players= new Player[1];
        players[0] = player;
        dealer = new Dealer(env, table, players );
        deck = IntStream.range(0, env.config.deckSize).boxed().collect(Collectors.toList());
        this.terminate = false;
    }

    @Test
    void removeCard(){
        table.placeCard(0,0);
        dealer.removeCard(0);
        assertEquals(null, table.slotToCard[0]);
    }

    @Test
    void placeAllCards(){
        dealer.placeCardsOnTable(12);
        dealer.removeAllCardsFromTable();
        assertTableCards();

    }

    void assertTableCards(){
        for (int i = 0; i < 12; i++) {
            assertEquals(null, table.slotToCard[i]);
        }
    }

    static class MockUserInterface implements UserInterface {
        @Override
        public void dispose() {}
        @Override
        public void placeCard(int card, int slot) {}
        @Override
        public void removeCard(int slot) {}
        @Override
        public void setCountdown(long millies, boolean warn) {}
        @Override
        public void setElapsed(long millies) {}
        @Override
        public void setScore(int player, int score) {}
        @Override
        public void setFreeze(int player, long millies) {}
        @Override
        public void placeToken(int player, int slot) {}
        @Override
        public void removeTokens() {}
        @Override
        public void removeTokens(int slot) {}
        @Override
        public void removeToken(int player, int slot) {}
        @Override
        public void announceWinner(int[] players) {}
    };

    static class MockUtil implements Util {
        @Override
        public int[] cardToFeatures(int card) {
            return new int[0];
        }

        @Override
        public int[][] cardsToFeatures(int[] cards) {
            return new int[0][];
        }

        @Override
        public boolean testSet(int[] cards) {
            return false;
        }

        @Override
        public List<int[]> findSets(List<Integer> deck, int count) {
            return null;
        }

        @Override
        public void spin() {}
    }

    static class MockLogger extends Logger {
        protected MockLogger() {
            super("", null);
        }
    }


}
