package com.example.aggregationservice.service;

import com.example.aggregationservice.service.strategy.ResourceFetchingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class ShipmentsService {

	private final ResourceFetchingStrategy<Long, List<String>> shipmentsFetchingStrategy;

	public Mono<Map<Long, List<String>>> getShipmentsByOrderNumber(final List<Long> shipmentOrderNumbers) {
		if (CollectionUtils.isEmpty(shipmentOrderNumbers)) {
			return Mono.just(Collections.emptyMap());
		}
		log.info("Fetching shipment data by order numbers {}", shipmentOrderNumbers);
		return shipmentsFetchingStrategy.getResources(shipmentOrderNumbers)
			       .doOnSuccess(shipmentsByOrderNumber -> log.debug("Successfully fetched shipment data by order numbers  {}", shipmentsByOrderNumber));

	}

}
