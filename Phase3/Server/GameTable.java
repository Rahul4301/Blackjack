package Server;

import Enums.BetStatus;
import Enums.GameState;
import Enums.PlayerAction;
import Shared.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameTable {
    private static int count;

    private String tableID;
    private Dealer dealer;
    private ArrayList<Player> players;
    private ArrayList<Bet> bets;
    private Shoe shoe;
    private GameState state;
    private int currentPlayerIndex; // to keep track of whos turn it is

    //Test Implementation
    public ArrayList<String> deck = new ArrayList<>();
    public ArrayList<String> playerHand = new ArrayList<>();
    public ArrayList<String> dealerHand = new ArrayList<>();
    public boolean roundActive = false;

    public synchronized void initDeck() {
        deck.clear();
        playerHand.clear();
        dealerHand.clear();

        String[] ranks = {"2","3","4","5","6","7","8","9","10","J","Q","K","A"};
        String[] suits = {"H","D","C","S"};

        for (String r : ranks) {
            for (String s : suits) {
                deck.add(r + s);
            }
        }

        Collections.shuffle(deck);
        roundActive = true;
    }
    //Test Implementation


    public GameTable(Dealer dealer){
        tableID = ("T" + ++count);
        this.dealer = dealer;
        players = new ArrayList<>(7);
        shoe = new Shoe(7); //Shoe & deck is randomized by default in their constructors
        bets = new ArrayList<>();
        state = GameState.BETTING; //Game flow: BETTING -> DEALING -> IN_PROGRESS -> RESULTS
        currentPlayerIndex = 0;
    }

    public boolean addPlayer(Player player){
        if(players.size() < 7) {
            players.add(player);
            return true;
        }
        return false;
    }

    public synchronized void removePlayer(Player person) {
        if (players.isEmpty()) {
            System.err.println("Cannot remove player - Player size is already 0!");
            return;
        }

        players.removeIf(p -> p.getID().equalsIgnoreCase(person.getID()));
    }


    public boolean startRound(){
        if(state != GameState.BETTING) return false;
        if(players.size() < 1) return false;

        //Check if everyone placed a bet AND there is at least 1 player
        
        for(Player player : players){
            if(player.getBet().getAmount() >= 1.00) continue;
            else System.err.println("Bet must be at least $1");
        }

        DealInitialCards();
        state = GameState.IN_PROGRESS;
        currentPlayerIndex = 0;
        return true;
    }

    public void DealInitialCards(){
        state = GameState.DEALING;

        for(Player p : players){
            for(int i = 0; i < 2; i++){
                p.hit(shoe.dealCard());
            }
        }
        for(int i = 0; i < 2; i++){
            dealer.hit(shoe.dealCard());
        }
    }

    public synchronized boolean handlePlayerAction(String playerUsername, PlayerAction action){
        if (state != GameState.IN_PROGRESS) return false;
        if (players.isEmpty()) return false;

        Player currentPlayer = players.get(currentPlayerIndex);
        if (!currentPlayer.getUsername().equalsIgnoreCase(playerUsername)) return false; // not this player's turn

        switch(action) {
            case HIT:
                currentPlayer.hit(shoe.dealCard());
                if (currentPlayer.getHandValue() > 21){
                    advanceTurn();
                }
                break;

            case STAND:
                advanceTurn();
                break;

            default:
                System.err.println("Not a viable player action!");
                break;
        }

        return true;
    }


    public void evaluateHands(){
        int dealerValue = dealer.getHandValue();
        boolean dealerBust = dealerValue > 21;

        for (Player player : players) {
            Bet bet = player.getBet();
            if (bet == null) {
                continue;
            }

            if (bet.isSettled()) {
                continue;
            }

            int playerValue = player.getHandValue();
            boolean playerBust = playerValue > 21;

            // Check for Blackjack for the player
            if (playerValue == 21 && dealerValue != 21) {
                bet.settle(BetStatus.BLACKJACK);
                continue;
            }

            // Player busts: player always loses, regardless of dealer
            if (playerBust) {
                bet.settle(BetStatus.LOSE);
                continue;
            }

            // Dealer busts and player did not: player wins
            if (dealerBust) {
                bet.settle(BetStatus.WIN);
                continue;
            }

            // At this point both values are <= 21 and neither has blackjack
            switch (dealer.compareHands(player.getHand())) {
                // Dealer hand higher than player
                case MORE: {
                    bet.settle(BetStatus.LOSE);
                    break;
                }

                // Player hand higher than dealer
                case LESS: {
                    bet.settle(BetStatus.WIN);
                    break;
                }

                // Equal hand value
                case EQUAL: {
                    bet.settle(BetStatus.PUSH);
                    break;
                }
            }
        }
    state = GameState.RESULTS;
}


    public String getTableID(){
        return tableID;
    }

    public ArrayList<Player> getPlayers(){
        return players;
    }

    public Dealer getDealer(){
        return dealer;
    }

    public String getCurrentPlayerUsername() {
        if (state != GameState.IN_PROGRESS) {
            return null;
        }
        if (currentPlayerIndex < 0 || currentPlayerIndex >= players.size()) {
            return null;
        }
        return players.get(currentPlayerIndex).getUsername();
    }




    public void resetTable(){
        dealer = null;
        players.clear();
        bets.clear();
        shoe = null;
    }

    public void resetForNextRound(){
        for (Player player : players){
            player.getHand().clearHand();
            player.placeBet(0);
        }

        dealer.getHand().clearHand();

        bets.clear();
        shoe = new Shoe(7);
        currentPlayerIndex = 0;
        state = GameState.BETTING;
    }

    //Send snapshots After: startRound() / dealInitialCards
    //After each successful handlePlayerAction()
    //and After runDealerAndFinishRound()

    public TableSnapshot createSnapshotFor(String requestingPlayerUsername) {
        DealerView dealerView = buildDealerView();
        List<PlayerView> playerViews = new ArrayList<>();

        String currentPlayerUsername = getCurrentPlayerID();

        for (Player p : players) {
            boolean isYou = requestingPlayerUsername != null
                    && p.getUsername().equalsIgnoreCase(requestingPlayerUsername);
            boolean isYourTurn = currentPlayerUsername != null
                    && p.getUsername().equalsIgnoreCase(currentPlayerUsername);

            // This already builds CardView objects internally
            playerViews.add(buildPlayerView(p, isYou, isYourTurn));
        }

        return new TableSnapshot(tableID, state, currentPlayerUsername, dealerView, playerViews);
    }


    private boolean isCurrentTurnFor(String username) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'isCurrentTurnFor'");
}


    public String getCurrentPlayerID() {
        // No "current player" once the betting round is over
        if (state != GameState.IN_PROGRESS) {
            return null;
        }
        if (currentPlayerIndex < 0 || currentPlayerIndex >= players.size()) {
            return null;
        }
        // Use username instead of internal ID
        return players.get(currentPlayerIndex).getUsername();
}


    //handlePlayerAction helpers
    private void advanceTurn() {
        // If the current player is the last one, run dealer and finish round
        if (currentPlayerIndex >= players.size() - 1) {
            runDealerAndFinishRound();
        } else {
            // Otherwise move to next player
            currentPlayerIndex++;
        }
    }


    private void runDealerAndFinishRound(){
        playDealerTurn();
        evaluateHands();
        state = GameState.RESULTS;
    }

    private void playDealerTurn(){
        Hand dealerHand = dealer.getHand();
        int total = dealer.getHandValue();
        
        while(dealer.mustHit(total)){
            dealerHand.addCard(shoe.dealCard());
            total = dealer.getHandValue();
        }
    }

    //Snapshot helpers

    public DealerView buildDealerView(){
        Hand dealerHand = dealer.getHand();
        List<Card> cards = dealerHand.getCards();
        List<CardView> cardViews = new ArrayList<>();

        boolean hasHiddenCard = false;

        for(int i = 0; i < cards.size(); i++){
            Card c = cards.get(i);
            boolean hidden = false;

            if((state == GameState.DEALING || state == GameState.IN_PROGRESS) && i == 1 && cards.size() >= 2){ //second card is hole card (hidden card)
                hidden = true;
                hasHiddenCard = true; //hide card to player, let dealer know it is hiding its card
            }

            cardViews.add(new CardView(c.getRank(), c.getSuit(), hidden));
        }

        return new DealerView(cardViews, hasHiddenCard);
    }

    private PlayerView buildPlayerView(Player player, boolean isYou, boolean isYourTurn) {
        Hand hand = player.getHand();
        List<Card> cards = hand.getCards();
        List<CardView> cardViews = new ArrayList<>();

        for (Card card : cards) {
            // Player cards are never hidden
            cardViews.add(new CardView(card.getRank(), card.getSuit(), false));
        }

        double betAmount = 0;
        Bet bet = player.getBet();
        if (bet != null) {
            betAmount = bet.getAmount();
        }

        int handValue = hand.getValue();
        boolean active = true; // you can refine this later

        return new PlayerView(
                // "playerId" in the view now stores username too
                player.getUsername(),
                player.getUsername(),
                betAmount,
                handValue,
                active,
                isYou,
                isYourTurn,
                cardViews,
                player.getBalance()
        );
    }




}
