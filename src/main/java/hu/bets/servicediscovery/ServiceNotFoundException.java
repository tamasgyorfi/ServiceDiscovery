package hu.bets.servicediscovery;

public class ServiceNotFoundException extends RuntimeException {
    public ServiceNotFoundException(Exception e) {
        super(e);
    }
}
