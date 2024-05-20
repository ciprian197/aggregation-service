package com.example.aggregationservice.client;

import com.example.aggregationservice.client.dto.TrackingStatus;
import com.example.aggregationservice.config.BackendServiceProperties;
import com.example.aggregationservice.exception.IntegrationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class BackendClient {

	private static final String QUERY_PARAMETER_NAME = "q";

	private final WebClient backendWebClient;
	private final BackendServiceProperties backendServiceProperties;

	public Mono<Map<Long, List<String>>> getShipmentsByOrderNumber(final List<Long> orderNumbers) {
		log.info("Performing backend call for shipment data for orderNumbers={}", orderNumbers);

		final String path = backendServiceProperties.getShipmentsApiPath();
		return performBackendCall(path, orderNumbers, new ParameterizedTypeReference<Map<Long, List<String>>>() {
		})
			       .onErrorResume(exception -> {
				       log.error("Encountered exception while fetching shipments data from backend client: ", exception);
				       return Mono.fromCallable(() -> orderNumbers.stream()
					                                      .collect(HashMap::new, (map, element) -> map.put(element, null), HashMap::putAll));
			       });
	}

	public Mono<Map<Long, TrackingStatus>> getTrackingStatusById(final List<Long> trackingIds) {
		log.debug("Performing backend call for tracking status data for ids={}", trackingIds);

		final String path = backendServiceProperties.getTrackApiPath();
		return performBackendCall(path, trackingIds, new ParameterizedTypeReference<Map<Long, TrackingStatus>>() {
		})
			       .onErrorResume(exception -> {
				       log.error("Encountered exception while fetching tracking data from backend client: ", exception);
				       return Mono.fromCallable(() -> trackingIds.stream()
					                                      .collect(HashMap::new, (map, element) -> map.put(element, null), HashMap::putAll));
			       });
	}

	public Mono<Map<String, Float>> getPricingByCountryCode(final List<String> countryCodes) {
		log.debug("Performing backend call for pricing data for countryCodes={}", countryCodes);

		final String path = backendServiceProperties.getPricingApiPath();
		return performBackendCall(path, countryCodes, new ParameterizedTypeReference<Map<String, Float>>() {
		})
			       .onErrorResume(exception -> {
				       log.error("Encountered exception while fetching pricing data from backend client: ", exception);
				       return Mono.fromCallable(() -> countryCodes.stream()
					                                      .collect(HashMap::new, (map, element) -> map.put(element, null), HashMap::putAll));
			       });
	}

	public <T, R> Mono<R> performBackendCall(final String path, final List<T> ids,
	                                         final ParameterizedTypeReference<R> responseTypeReference) {
		log.debug("Performing GET backend call for path {} with ids {}", path, ids);

		return backendWebClient.get()
			       .uri(uriBuilder -> uriBuilder.path(path)
				                          .queryParam(QUERY_PARAMETER_NAME, ids).build())
			       .exchangeToMono(response -> {
				       log.info("Received status code {} from backend client for path {} and request {}", response.statusCode(), path, ids);
				       if (response.statusCode().is2xxSuccessful()) {
					       return response.bodyToMono(responseTypeReference);
				       } else {
					       return handleErrorCode(path, response);
				       }
			       })
			       .doOnSuccess(response -> log.info("Successfully retrieved data {} from backend service path {} for request {}", response, path, ids));
	}

	private <T> Mono<T> handleErrorCode(final String path, final ClientResponse response) {
		return response.bodyToMono(String.class)
			       .flatMap(body -> Mono.error(new IntegrationException(String.format("Error encountered while connecting to the backend service for path %s : %s", path, body))));
	}

}
