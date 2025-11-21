package Server;
import enums.AccState;

abstract class Account {
    protected String username, password;
    protected boolean sessionActive;
    protected AccState accState;

    public boolean login(){
        return true;
    }

    public void logout(){
        return;
    }
}
