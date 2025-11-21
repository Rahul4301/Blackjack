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

        double payout = calculatePayout();
        player.updateBalance(payout);
    }

    public double calculatePayout() {
        switch(outcome) {
            case "BLACKJACK": return amount * 1.5;
            case "WIN": return amount;
            case "PUSH": return 0; 
            case "LOSE": return -amount;
            default: return 0;
        }
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
