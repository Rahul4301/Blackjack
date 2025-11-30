package Server;

import Enums.BetStatus;
import Enums.GameState;
import java.util.ArrayList;

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

    public void resetTable(){
        dealer = null;
        players.clear();
        bets.clear();
        shoe = null;
    }

}
