package Server;
import Enums.BetStatus;

public class Bet {
    private double amount;
    private boolean settled;
    private BetStatus outcome;
    private Player player;
    boolean doubled;       // new field, package-private so GameTable can touch it


    public Bet(Player player, double amount) {
        this.player = player;
        this.amount = amount;
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
        double base;

        switch(outcome) {
            case BLACKJACK: base = amount * 1.5; break;
            case WIN:       base = amount;       break;
            case PUSH:      base = 0;            break; 
            case LOSE:      base = -amount;      break;
            default:        base = 0;            break;
        }

        if (doubled) {
            base *= 2;
        }

        return base;
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
