package com.example.aggregationservice.service.strategy;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class ResourceFetchingStrategy<T,R> {

	private final Function<List<T>, Mono<Map<T,R>>> fetchFunction;

	public ResourceFetchingStrategy(final Function<List<T>, Mono<Map<T, R>>> fetchFunction) {
		this.fetchFunction = fetchFunction;
	}

	public abstract Mono<Map<T,R>> getResources(final List<T> requestData);

	protected Function<List<T>, Mono<Map<T, R>>> getFetchFunction() {
		return fetchFunction;
	}

}
