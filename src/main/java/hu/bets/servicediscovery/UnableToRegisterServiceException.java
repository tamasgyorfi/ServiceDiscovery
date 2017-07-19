package hu.bets.servicediscovery;

public class UnableToRegisterServiceException extends RuntimeException {

    public UnableToRegisterServiceException(String msg, Exception e) {
        super(msg, e);
    }
}
