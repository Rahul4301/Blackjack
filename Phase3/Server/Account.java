package Server;

import Enums.AccState;
import java.io.Serializable;

public abstract class Account implements Serializable, IUser {
    protected String username, password;
    protected boolean sessionActive;
    protected AccState accountState;
    
    @Override
    public String toString() {
        return "Username: " + username + " | Password: " + password;
    }

    @Override
    public String getUsername(){
        return username;
    }

    @Override
    public AccState getAccState(){
        return accountState;
    }

    @Override
    public boolean isSessionActive() {
        return sessionActive;
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