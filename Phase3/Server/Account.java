package Server;
import java.io.Serializable;
import Enums.AccState;


public abstract class Account implements Serializable {
    protected String username, password;
    protected boolean sessionActive;
    protected AccState accountState;
    
    @Override
    public String toString() {
    	return "Username: " + username + " | Password: " + password;
    }

    public String getUsername(){
        return username;
    }

    public AccState getAccState(){
        return accountState;
    }
    public String getAccountType() {
    if (this instanceof Player) {
        return "PLAYER";
    } else if (this instanceof Dealer) {
        return "DEALER";
    }
    return "UNKNOWN";
}
}
