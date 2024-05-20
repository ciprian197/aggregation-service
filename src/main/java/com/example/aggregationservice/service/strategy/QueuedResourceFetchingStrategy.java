package com.example.aggregationservice.service.strategy;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class QueuedResourceFetchingStrategy<T,R> extends ResourceFetchingStrategy<T, R> {

	private static final int BUFFER_MAX_SIZE = 5;

	private final QueueOverFlux<T,R> strategyImplementation;

	public QueuedResourceFetchingStrategy(final Function<List<T>, Mono<Map<T, R>>> fetchFunction){
		super(fetchFunction);
		final Sinks.Many<T> sink = Sinks.many().multicast().onBackpressureBuffer();
		final Flux<Map<T, R>> queue = sink.asFlux()
			                              .buffer(BUFFER_MAX_SIZE)
			                              .flatMap(fetchFunction);
		this.strategyImplementation = new QueueOverFlux<>(sink, queue);
	}

	@Override
	public Mono<Map<T, R>> getResources(final List<T> requestData) {
		return strategyImplementation.getResources(requestData);
	}

}
