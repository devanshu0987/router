package org.codapayments.router.config;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.codapayments.router.enums.RoutingAlgorithmType;
import org.codapayments.router.enums.ServiceInstanceListSupplierType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

// This component binds values from application.properties to object via @ConfigurationProperties
@Component
@Validated
@ConfigurationProperties("routing-config")
public class RoutingConfig {

    @NotNull
    private RoutingAlgorithmType routingAlgorithm;
    @NotNull
    private ServiceInstanceListSupplierType supplierType;
    @NotNull
    @NotEmpty
    private List<URI> instances = new ArrayList<>();
    private Integer timeoutInSeconds;

    public ServiceInstanceListSupplierType getSupplierType() {
        return supplierType;
    }

    public void setSupplierType(ServiceInstanceListSupplierType supplierType) {
        this.supplierType = supplierType;
    }

    public Integer getTimeoutInSeconds() {
        return timeoutInSeconds;
    }

    public void setTimeoutInSeconds(Integer timeoutInMinutes) {
        this.timeoutInSeconds = timeoutInMinutes;
    }

    public RoutingAlgorithmType getRoutingAlgorithm() {
        return routingAlgorithm;
    }

    public void setRoutingAlgorithm(RoutingAlgorithmType routingAlgorithm) {
        this.routingAlgorithm = routingAlgorithm;
    }

    public List<URI> getInstances() {
        return instances;
    }

    public void setInstances(List<URI> instances) {
        this.instances = instances;
    }

    @Override
    public String toString() {
        return "RoutingConfig{" +
                "routingAlgorithm=" + routingAlgorithm +
                ", instances=" + instances +
                '}';
    }
}
