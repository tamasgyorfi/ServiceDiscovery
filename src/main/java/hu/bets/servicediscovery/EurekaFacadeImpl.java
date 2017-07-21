package hu.bets.servicediscovery;

import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.DynamicPropertyFactory;

import java.util.Properties;
import java.util.concurrent.Future;

public class EurekaFacadeImpl implements EurekaFacade {

    private ConcurrentCompositeConfiguration configuration = new ConcurrentCompositeConfiguration();

    private static EurekaRegistrationHandler registrationHandler;
    private static EurekaServiceResolver serviceResolver;

    public EurekaFacadeImpl(String eurekaUrl) {
        configuration.addProperty("eureka.region", "default");
        configuration.addProperty("eureka.preferSameZone", "true");
        configuration.addProperty("eureka.shouldUseDns", "false");
        configuration.addProperty("eureka.serviceUrl.default", eurekaUrl);
    }

    public EurekaFacadeImpl(Properties props) {
        System.getProperties().putAll(props);
    }

    public void registerBlockingly(String serviceName) {

        setInstanceProperties(serviceName);
        getRegistrationHandler().blockingRegister(serviceName);
    }

    public Future<Boolean> registerNonBlockingly(String serviceName) {

        setInstanceProperties(serviceName);
        return getRegistrationHandler().nonBlockingRegister(serviceName);
    }

    public String resolveEndpoint(String name) {
        return getServiceResolver().getServiceEndpoint(name);
    }

    private void setInstanceProperties(String name) {
        configuration.addProperty("eureka.vipAddress", name);
        configuration.addProperty("eureka.name", name);

        configuration.addProperty("eureka.homePageUrl", "https://" + name + ".herokuapp.com");
        configuration.addProperty("eureka.hostname", "https://" + name + ".herokuapp.com");
        DynamicPropertyFactory.initWithConfigurationSource(configuration);
    }

    @Override
    public void unregister() {
        getRegistrationHandler().unregister();
    }

    private synchronized EurekaServiceResolver getServiceResolver() {
        if (serviceResolver == null) {
            serviceResolver = new EurekaServiceResolver();
        }

        return serviceResolver;
    }

    private synchronized EurekaRegistrationHandler getRegistrationHandler() {
        if (registrationHandler == null) {
            registrationHandler = new EurekaRegistrationHandler();
        }

        return registrationHandler;
    }
}
