package com.example.aggregationservice.service.strategy;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ScheduledQueuedResourceFetchingStrategy<T,R> extends ResourceFetchingStrategy<T, R> {

	private static final int BUFFER_MAX_SIZE = 5;
	private static final Duration BUFFER_MAX_DURATION = Duration.of(5, ChronoUnit.SECONDS);

	private final QueueOverFlux<T,R> strategyImplementation;

	public ScheduledQueuedResourceFetchingStrategy(final Function<List<T>, Mono<Map<T, R>>> fetchFunction){
		super(fetchFunction);
		final Sinks.Many<T> sink = Sinks.many().multicast().onBackpressureBuffer();
		final Flux<Map<T, R>> queue = sink.asFlux()
			                              .bufferTimeout(BUFFER_MAX_SIZE, BUFFER_MAX_DURATION)
			                              .flatMap(fetchFunction);
		this.strategyImplementation = new QueueOverFlux<>(sink, queue);
	}

	@Override
	public Mono<Map<T, R>> getResources(final List<T> requestData) {
		return strategyImplementation.getResources(requestData);
	}

}
