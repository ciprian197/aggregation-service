package com.example.aggregationservice.service;

import com.example.aggregationservice.client.dto.AggregationRequest;
import com.example.aggregationservice.client.dto.AggregationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

import java.util.Map;

@RequiredArgsConstructor
@Service
public class AggregationService {

	private final ShipmentsService shipmentsService;
	private final TrackService trackService;
	private final PricingService pricingService;

	public Mono<AggregationResponse> getAggregationResponse(AggregationRequest request) {
		return pricingService.getPricingByCountryCode(request.pricing())
			       .zipWith(shipmentsService.getShipmentsByOrderNumber(request.shipments()))
			       .zipWith(trackService.getTrackingStatusById(request.track()))
			       .map(pricingAndShipmentsWithTracking -> AggregationResponse.builder()
				                                               .pricing(getResponseValue(pricingAndShipmentsWithTracking.getT1().getT1()))
				                                               .shipments(getResponseValue(pricingAndShipmentsWithTracking.getT1().getT2()))
				                                               .track(getResponseValue(pricingAndShipmentsWithTracking.getT2())).build());
	}

	private static <T, R> Map<T, R> getResponseValue(final Map<T, R> responseData) {
		return CollectionUtils.isEmpty(responseData) ? null : responseData;
	}

}
