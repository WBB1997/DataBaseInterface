package Adapter;

public interface SQLStateListener {
    abstract public void SQLStart();
    abstract public void SQLError(String SQLState, int errorCode);
    abstract public void SQLEnd();
}
