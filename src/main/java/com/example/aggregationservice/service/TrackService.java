package com.example.aggregationservice.service;

import com.example.aggregationservice.client.dto.TrackingStatus;
import com.example.aggregationservice.service.strategy.ResourceFetchingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class TrackService {

	private final ResourceFetchingStrategy<Long, TrackingStatus> trackingStatusFetchingStrategy;

	public Mono<Map<Long, TrackingStatus>> getTrackingStatusById(final List<Long> trackIds) {
		if (CollectionUtils.isEmpty(trackIds)) {
			return Mono.just(Collections.emptyMap());
		}
		log.info("Fetching tracking status data by tracking ids {}", trackIds);

		return trackingStatusFetchingStrategy.getResources(trackIds)
			       .doOnSuccess(shipmentsByOrderNumber -> log.debug("Successfully fetched track data by tracking ids  {}", shipmentsByOrderNumber));
	}

}
