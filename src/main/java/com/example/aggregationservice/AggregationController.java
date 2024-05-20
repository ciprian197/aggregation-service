package com.example.aggregationservice;

import com.example.aggregationservice.client.dto.AggregationRequest;
import com.example.aggregationservice.client.dto.AggregationResponse;
import com.example.aggregationservice.service.AggregationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/aggregation")
@RestController
public class AggregationController {

	private final AggregationService aggregationService;

	@GetMapping
	public Mono<AggregationResponse> getAggregationResult(@ModelAttribute final AggregationRequest request) {
		log.info("Received GET request for aggregation {}", request);
		return aggregationService.getAggregationResponse(request)
			       .doOnSuccess(response -> log.info("Returning response to client {}", response));
	}

}
