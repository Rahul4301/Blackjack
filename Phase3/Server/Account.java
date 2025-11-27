package Server;
import Enums.AccState;

abstract class Account {
    protected String username, password;
    protected boolean sessionActive;
    protected AccState accountState;
    
}
