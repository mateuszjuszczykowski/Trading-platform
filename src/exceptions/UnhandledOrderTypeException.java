package exceptions;

public class UnhandledOrderTypeException extends Exception{
    UnhandledOrderTypeException(String msg) {
        super(msg);
    }
}
