package org.codapayments.router.algorithm;

import org.codapayments.router.algorithm.impl.RoutingAlgorithmFactory;
import org.codapayments.router.config.RoutingConfig;
import org.codapayments.router.instanceListSupplier.ServiceInstanceListSupplierType;
import org.codapayments.router.instanceListSupplier.ServiceInstanceListSupplier;
import org.codapayments.router.instanceListSupplier.impl.ServiceInstanceListSupplierFactory;
import org.codapayments.router.service.CircuitBreakerService;
import org.codapayments.router.service.MetricService;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class RoundRobinAlgorithmTests {

    private RoutingConfig getRoutingConfig(RoutingAlgorithmType type) {
        RoutingConfig config = new RoutingConfig();
        config.setRoutingAlgorithm(type);
        config.setSupplierType(ServiceInstanceListSupplierType.STATIC);
        config.setCooldownTimeoutInSeconds(5);
        try {
            config.setInstances(List.of(
                    new URI("http://localhost:8081"),
                    new URI("http://localhost:8082"),
                    new URI("http://localhost:8083"),
                    new URI("http://localhost:8084"))
            );
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return config;
    }

    @Test
    public void testHappyPath() {
        var config = getRoutingConfig(RoutingAlgorithmType.ROUND_ROBIN);
        RoutingAlgorithm router = RoutingAlgorithmFactory.getInstance(config);
        MetricService metricService = new MetricService(config);
        CircuitBreakerService circuitBreakerService = new CircuitBreakerService(config, metricService);
        ServiceInstanceListSupplier supplier = ServiceInstanceListSupplierFactory.getInstance(config, circuitBreakerService);

        assert config.getInstances().get(0) == router.chooseServiceInstance(supplier);
        assert config.getInstances().get(1) == router.chooseServiceInstance(supplier);
        assert config.getInstances().get(2) == router.chooseServiceInstance(supplier);
        assert config.getInstances().get(3) == router.chooseServiceInstance(supplier);
        assert config.getInstances().get(0) == router.chooseServiceInstance(supplier);
    }

    @Test
    public void testConcurrentRequests() throws ExecutionException, InterruptedException {
        var config = getRoutingConfig(RoutingAlgorithmType.ROUND_ROBIN);
        RoutingAlgorithm router = RoutingAlgorithmFactory.getInstance(config);
        MetricService metricService = new MetricService(config);
        CircuitBreakerService circuitBreakerService = new CircuitBreakerService(config, metricService);
        ServiceInstanceListSupplier supplier = ServiceInstanceListSupplierFactory.getInstance(config, circuitBreakerService);
        ExecutorService executor = (ExecutorService) Executors.newFixedThreadPool(10);

        int numberOfTasks = config.getInstances().size() * 200;
        List<Future<URI>> resultList = null;
        List<Callable<URI>> callables = new ArrayList<>();


        for (int index = 0; index < numberOfTasks; index++) {
            callables.add(new RouterCallable(router, supplier));
        }

        // ACT
        try {
            resultList = executor.invokeAll(callables);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        executor.shutdown();

        // ASSERT
        Map<URI, Integer> resultFrequency = new HashMap<>();
        for (Future<URI> uriFuture : resultList) {
            try {
                var uri = uriFuture.get();
                resultFrequency.put(uri, resultFrequency.getOrDefault(uri, 0) + 1);
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }

        // All frequencies should be same and equal to numberOfTasks / size.
        for (var item : resultFrequency.entrySet()) {
            assert item.getValue() == numberOfTasks / config.getInstances().size();
        }
    }
}
