package com.example.aggregationservice.config;

import com.example.aggregationservice.client.BackendClient;
import com.example.aggregationservice.client.dto.TrackingStatus;
import com.example.aggregationservice.service.strategy.DirectResourceFetchingStrategy;
import com.example.aggregationservice.service.strategy.QueuedResourceFetchingStrategy;
import com.example.aggregationservice.service.strategy.ResourceFetchingStrategy;
import com.example.aggregationservice.service.strategy.ScheduledQueuedResourceFetchingStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class StrategyPickerConfig {

	private final BackendClient backendClient;
	private final FetchingStrategy fetchingStrategyName;


	public StrategyPickerConfig(final BackendClient backendClient,
	                            @Value("${aggregation-service.fetching-strategy}") final FetchingStrategy fetchingStrategy) {
		this.fetchingStrategyName = fetchingStrategy;
		this.backendClient = backendClient;
	}

	@Bean
	public ResourceFetchingStrategy<Long, List<String>> getShipmentsFetchingStrategy() {
		return switch (fetchingStrategyName) {
			case DIRECT -> new DirectResourceFetchingStrategy<>(backendClient::getShipmentsByOrderNumber);
			case QUEUED -> new QueuedResourceFetchingStrategy<>(backendClient::getShipmentsByOrderNumber);
			case SCHEDULED_QUEUED -> new ScheduledQueuedResourceFetchingStrategy<>(backendClient::getShipmentsByOrderNumber);
		};
	}

	@Bean
	public ResourceFetchingStrategy<Long, TrackingStatus> getTrackingStatusFetchingStrategy() {
		return switch (fetchingStrategyName) {
			case DIRECT -> new DirectResourceFetchingStrategy<>(backendClient::getTrackingStatusById);
			case QUEUED -> new QueuedResourceFetchingStrategy<>(backendClient::getTrackingStatusById);
			case SCHEDULED_QUEUED -> new ScheduledQueuedResourceFetchingStrategy<>(backendClient::getTrackingStatusById);
		};
	}

	@Bean
	public ResourceFetchingStrategy<String, Float> getPricingFetchingStrategy() {
		return switch (fetchingStrategyName) {
			case DIRECT -> new DirectResourceFetchingStrategy<>(backendClient::getPricingByCountryCode);
			case QUEUED -> new QueuedResourceFetchingStrategy<>(backendClient::getPricingByCountryCode);
			case SCHEDULED_QUEUED -> new ScheduledQueuedResourceFetchingStrategy<>(backendClient::getPricingByCountryCode);
		};
	}

}
