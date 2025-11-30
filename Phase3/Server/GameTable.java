package Server;

import Enums.BetStatus;
import Enums.GameState;
import Shared.*;
import java.util.ArrayList;
import java.util.List;

public class GameTable {
    private static int count;

    private String tableID;
    Dealer dealer;
    ArrayList<Player> players;
    ArrayList<Bet> bets;
    Shoe shoe;
    GameState state;
    private int currentPlayerIndex; // to keep track of whos turn it is

    public GameTable(Dealer dealer){
        tableID = ("T" + ++count);
        this.dealer = dealer;
        players = new ArrayList<>(7);
        shoe = new Shoe(7); //Shoe / deck is randomized by default in their constructors
        bets = new ArrayList<>();
        state = GameState.IN_PROGRESS;
        currentPlayerIndex = 0;
    }

    public boolean addPlayer(Player player){
        if(players.size() < 7) {
            players.add(player);
            return true;
        }
        return false;
    }

    public void removePlayer(Player person){
        if(players.size() == 0){
            System.err.println("Cannot remove player - Player size is already 0!");
        }
        for(Player player : players){
            if(player.getID().equalsIgnoreCase(person.getID())){
                players.remove(person);
            }
        }
    }

    public void startRound(){
        state = GameState.IN_PROGRESS;
        DealInitialCards();
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

    public void evaluateHands(){
        for(Player player : players){

            //Check for BlackJack
            if(player.getHandValue() == 21){
                if(dealer.getHandValue() != 21){
                    (player.getBet()).settle(BetStatus.BLACKJACK);
                    continue;
                }
            }

            switch(dealer.compareHands(player.getHand())){
                //Dealer hand higher than player
                case MORE:{
                    (player.getBet()).settle(BetStatus.LOSE);
                    break;
                }
                
                //Player hand higher than dealer
                case LESS:{
                    (player.getBet()).settle(BetStatus.WIN);
                    break;
                }

                //Equal hand value
                case EQUAL:{
                    (player.getBet()).settle(BetStatus.PUSH);
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

    public String getCurrentPlayerID(){
        if(players.isEmpty()) return null;
        return players.get(currentPlayerIndex).getID();
    }

    public void resetTable(){
        dealer = null;
        players.clear();
        bets.clear();
        shoe = null;
    }

     public TableSnapshot createSnapshotFor(String requestingPlayerID){
        DealerView dealerView = buildDealerView();
        List<PlayerView> playerViews = new ArrayList<>();
        
        for(Player p : players){
            boolean isYourTurn = p.getID().equalsIgnoreCase(requestingPlayerID);
            playerViews.add(buildPlayerView(p, isYourTurn));
        }

        String currentPlayerID = getCurrentPlayerID();

        return new TableSnapshot(tableID, state, currentPlayerID, dealerView, playerViews);
    }

    //Snapshot helpers

    private DealerView buildDealerView(){
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

    private PlayerView buildPlayerView(Player player, boolean isYourTurn){
        Hand hand = player.getHand();
        List<Card> cards = hand.getCards();
        List<CardView> cardViews = new ArrayList<>();

        for(Card card : cards){
            cardViews.add(new CardView(card.getRank(), card.getSuit(), false)); //Player cards are never hidden
        }

        double betAmount = 0;
        Bet bet = player.getBet();

        if(bet != null){
            betAmount = (double) bet.getAmount();
        }

        int handValue = hand.getValue();
        boolean active = true;

        return new PlayerView(player.getID(), player.getUsername(), betAmount, handValue, active, isYourTurn, cardViews);
    }
}
