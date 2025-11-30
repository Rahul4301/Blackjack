package Server;
import Enums.AccState;
import java.io.Serializable;


public abstract class Account implements Serializable {
    protected String username, password;
    protected boolean sessionActive;
    protected AccState accountState;
    
    public String getUsername() {
        return username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public boolean isSessionActive() {
        return sessionActive;
    }
    
    public AccState getAccountState() {
        return accountState;
    }
    
    @Override
    public String toString() {
    	return "Username: " + username + " | Password: " + password;
    }
}
