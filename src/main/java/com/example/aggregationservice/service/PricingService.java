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
import java.util.Locale;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class PricingService {

	private final ResourceFetchingStrategy<String, Float> pricingFetchingStrategy;

	public Mono<Map<String, Float>> getPricingByCountryCode(final List<String> countryCodes) {
		if (CollectionUtils.isEmpty(countryCodes)) {
			return Mono.just(Collections.emptyMap());
		}
		log.info("Fetching pricing data by country codes {}", countryCodes);
		return pricingFetchingStrategy.getResources(countryCodes)
			       .doOnSuccess(pricingByCountryCode -> log.debug("Successfully fetched pricing data by countryCodes  {}", pricingByCountryCode));
	}

}
