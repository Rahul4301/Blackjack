package Server;
import Enums.BetStatus;

public class Bet {
    private double amount;
    private double payoutMultiplier;
    private boolean settled;
    private BetStatus outcome;
    private Player player;

    public Bet(Player player, double amount) {
        this.player = player;
        this.amount = amount;
        this.payoutMultiplier = 1.0;
        this.settled = false;
        this.outcome = BetStatus.PENDING;
        
         if(!validateBet()){
            throw new IllegalArgumentException("Invalid bet amount");

        }
    }

    public boolean validateBet() {
        return player != null && amount > 0 && amount <= player.getBalance();
    }

    public void settle(BetStatus result) {
        this.outcome = result; 
        this.settled = true; 

        double payout = calculatePayout();
        player.updateBalance(payout);
    }

    public double calculatePayout() {
        switch(outcome) {
            case BLACKJACK:
                return amount * 2.5;  // original bet + 1.5x payout
            case WIN:
                return amount * 2;    // return original bet + win
            case PUSH:
                return amount;        // return original bet
            case LOSE:
                return 0;             // lose the bet
            default:
                return 0;
        }
    }

    // getters
    public double getAmount() {
        return amount;
    }

    public boolean isSettled() {
        return settled;
    }

    public BetStatus getOutcome() {
        return outcome;
    }

    public Player getPlayer() {
        return player;
    }


    //Testing purposes
    @Override
    public String toString(){
        return (player.getUsername() + " placed a bet: " + amount);

    }


}
