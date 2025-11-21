public class Bet {
    private double amount;
    private double payoutMultiplier;
    private boolean settled;
    private String outcome;
    private Player player;

    public Bet(Player player, double amount) {
        this.player = player;
        this.amount = amount;
        this.payoutMultiplier = 1.0;
        this.settled = false;
        this.outcome = "PENDING";
    }

    public boolean validateBet() {
        return player != null && amount > 0 && amount <= player.getBalance();

    }

    public void settle(String result) {
        this.outcome = result; 
        this.settled = true; 

        switch(result) {
            case "BLACKJACK":
                this.payoutMultiplier = 2.5;
                break;
            case "WIN":
                this.payoutMultiplier = 2.0;
            break;
            case "PUSH":
                this.payoutMultiplier = 1.0;
            break;
            case "LOSE": 
                this.payoutMultiplier = 0.0;
            break;
            default:
                this.payoutMultiplier = 1.0;
        }
    }

    public double calculatePayout() {
       if (!settled) {
        return 0.0;
       }
       return amount * payoutMultiplier;
    }

    // getters
    public double getAmount() {
        return amount;
    }

    public boolean isSettled() {
        return settled;
    }

    public String getOutcome() {
        return outcome;
    }

    public Player getPlayer() {
        return player;
    }





}
