package exceptions;

public class NotEnoughFundsException extends Exception{
    public NotEnoughFundsException(String msg) {
        super(msg);
    }
}
