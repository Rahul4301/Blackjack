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
}
