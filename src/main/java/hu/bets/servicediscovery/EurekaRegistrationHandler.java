package hu.bets.servicediscovery;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

class EurekaRegistrationHandler {

    private EurekaClient eurekaClient;

    private static final Logger LOGGER = LoggerFactory.getLogger(EurekaRegistrationHandler.class);
    private static final int RETRY_WAIT_TIME = 5;

    void waitForRegistrationWithEureka(String vipAddress, EurekaClient eurekaClient, int retrySeconds) {
        LOGGER.info("Registering service with name: " + vipAddress);
        InstanceInfo nextServerInfo = null;
        int retries = 5;

        while (nextServerInfo == null) {
            try {
                nextServerInfo = eurekaClient.getNextServerFromEureka(vipAddress, false);
            } catch (Exception e) {
                LOGGER.info("Waiting ... verifying service registration with eureka ...");
                LOGGER.error("", e);
                retries--;
                if (retries == 0) {
                    throw new UnableToRegisterServiceException("Could not register service: " + vipAddress, e);
                }
                try {
                    TimeUnit.SECONDS.sleep(retrySeconds);
                } catch (InterruptedException e1) {
                    // Do nothing here.
                }
            }
        }
    }

    private void register(String name) {
        EurekaFactory eurekaFactory = EurekaFactory.getInstance();
        ApplicationInfoManager appInfoManager = eurekaFactory.getApplicationInfoManager();
        eurekaClient = eurekaFactory.getEurekaClient();

        appInfoManager.setInstanceStatus(InstanceInfo.InstanceStatus.UP);
        LOGGER.info("Registering with eureka server.");
        waitForRegistrationWithEureka(name, eurekaClient, RETRY_WAIT_TIME);
        LOGGER.info("Service is up and running.");
    }

    void blockingRegister(String name) {
        register(name);
    }

    Future<Boolean> nonBlockingRegister(final String name) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Boolean> retVal = executor.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                register(name);
                return true;
            }
        });

        executor.shutdownNow();
        return retVal;
    }

    public void unregister() {
        eurekaClient.shutdown();
    }
}