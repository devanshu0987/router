## Functional Requirements
- User sends a request which gets routed to configured instances of another server in an Round Robin Fashion.

## Controller Interface Scope
- All paths are accepted, but the expectation is that it is accepted by the downstream API.
  - `/**` is the matching pattern.
  - For example: `/echo`, `/v2/echo?delay=5`
- Only `POST` is allowed. Body should be a valid `JSON` payload.
  - Invalid payloads will be rejected.

## Introduction
- The application allows routing of API requests in a `ROUND-ROBIN` fashion among the known API service instances.
- It accepts the request, selects a service instance of downstream API and send the request to it.
- It receives the response and sends it back to the user.

## Feature Scope
- Only 2 routing algos are supported: `RANDOM` and `ROUND_ROBIN` routing.
- Static List of service instances.
- No provision to update the list in runtime.

## Design
- The Router assumes a `Static List` of downstream service instances provided to the application via `application.properties` file.
  - No runtime modification of configuration is supported.
- Two type of `RoutingAlgorithmType` is supported.
  - `RANDOM` and `ROUND_ROBIN` routing.
- `RoutingAlgorithm` interface exposes a single method.
  - `public URI chooseServiceInstance(ServiceInstanceListSupplier supplier);`
  - The expectation out of the implementation is that, when provided with a `supplier` which can supply a list of service instances, the algorithm will choose 1 of them using the algo.
- `ServiceInstanceListSupplier` interface acts as place to encapsulate someone who can supply with a list of service instances.
  - There are multiple ways to implement the interface.
  - Presently implemented classes are
    - `StaticServiceInstanceListSupplier` : It returns a static list of service instances.
    - `StaticServiceInstanceWithCoolDownListSupplier` : It returns a filtered list of service instances which are not in cooldown.
      - `CircuitBreakerService` maintains metrics for the instances and can mark an instance to be in cooldown.
      - Cooldown means, the instance will be not be made available to the Routing algorithm.
      - Helps in routing requests to other instances in case of error or high latency.
  - The interface is generic enough that other schemes can be implemented.
- `MetricService` maintains a set of statistics about ErrorCount, SuccessCount and Latency.
  - They can be used to take actions in case of Downstream API failure or slowness.
  - We use a `SLIDING_WINDOW` scheme with specific window size to make sure metrics represent the latest state.
- `CircuitBreakerService` consumes the data from `MetricService`
  - It only provides 1 function `isCircuitClosed` which indicates whether the instance is available or not.
  - *Currently, this is the only corrective measure that is implemented in case of failure but can be extended based on more metrics and business logic.*
  - Other possibilities are: Retry or Fallback request by using different type of supplier.

## Request flow
- User sends a request and is received by the controller.
- Controller offloads the request handling to `RoutingService`
- `RoutingService` asks the `RoutingAlgorithm` to provide `URI` of the downstream service instance by providing a supplier.
- Once we get a URI, we validate it and check with `CircuitBreakerService` whether we can call this API.
- If we get a positive response, we prepare the HttpRequest and send it to the downstream service instance.
- We get the response back and update the metrics based on the response.
  - In case of failure, we update the error metrics and latency.
- And then we return the response to the user.

## How would the service behave if downstream instance goes down/slows down.
- Every request we send to the failed downstream service will fail.
- On the service end, we will update the metrics and increment error counts.
- On subsequent calls, the metrics will keep on accumulating. It will cross the threshold set.
- On the next call, the Supplier will filter out the service instance because the function `isCircuitClosed` will return false.
- In this way, the instance will not get selected for the next `N` seconds.
- Overtime, the Sliding window metrics will reduce as the time passes and once again we will select the instance.
- If the request passes, we update the metrics accordingly and the processing will continue.

## Folder Structure
- `algorithm` : Stores Routing algorithms implementation.
- `config` : Config classes.
- `controller`
- `service` : Stores domain logic in terms of Routing, Metrics accumulation and Error Handling.
- `serviceInstanceListSupplier` : Stores supplier list implementations.
- `statistics` : Classes used by Metric service.

## Entry Point
- `controller` folder.

## Interactions
[Image](Interactions.png)