package com.example.aggregationservice.service.strategy;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class DirectResourceFetchingStrategy<T,R> extends ResourceFetchingStrategy<T, R> {

	public DirectResourceFetchingStrategy(final Function<List<T>, Mono<Map<T, R>>> fetchFunction) {
		super(fetchFunction);
	}

	@Override
	public Mono<Map<T, R>> getResources(final List<T> requestData) {
		return getFetchFunction().apply(requestData);
	}
}
