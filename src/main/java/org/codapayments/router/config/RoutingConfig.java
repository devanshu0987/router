package org.codapayments.router.config;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.codapayments.router.algorithm.RoutingAlgorithmType;
import org.codapayments.router.instanceListSupplier.ServiceInstanceListSupplierType;
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

    private Integer cooldownTimeoutInSeconds = 0;
    // To what extent the error count should go to warrant a cooldown.
    private Integer errorCountForCooldown = Integer.MAX_VALUE;
    // To what extent the latency should go to warrant a cooldown.
    private Integer latencyForCooldownInSeconds = Integer.MAX_VALUE;
    private Integer metricsWindowSizeInSeconds = Integer.MAX_VALUE;

    public Integer getErrorCountForCooldown() {
        return errorCountForCooldown;
    }


    public void setErrorCountForCooldown(Integer errorCountForCooldown) {
        this.errorCountForCooldown = errorCountForCooldown;
    }

    public Integer getLatencyForCooldownInSeconds() {
        return latencyForCooldownInSeconds;
    }

    public void setLatencyForCooldownInSeconds(Integer latencyForCooldownInSeconds) {
        this.latencyForCooldownInSeconds = latencyForCooldownInSeconds;
    }

    public Integer getMetricsWindowSizeInSeconds() {
        return metricsWindowSizeInSeconds;
    }

    public void setMetricsWindowSizeInSeconds(Integer metricsWindowSizeInSeconds) {
        this.metricsWindowSizeInSeconds = metricsWindowSizeInSeconds;
    }

    public Integer getCooldownTimeoutInSeconds() {
        return cooldownTimeoutInSeconds;
    }

    public void setCooldownTimeoutInSeconds(Integer cooldownTimeoutInSeconds) {
        this.cooldownTimeoutInSeconds = cooldownTimeoutInSeconds;
    }

    public ServiceInstanceListSupplierType getSupplierType() {
        return supplierType;
    }

    public void setSupplierType(ServiceInstanceListSupplierType supplierType) {
        this.supplierType = supplierType;
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
        return "RoutingConfig{" + "routingAlgorithm=" + routingAlgorithm + ", instances=" + instances + '}';
    }
}
