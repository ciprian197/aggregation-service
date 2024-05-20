package com.example.aggregationservice.service.strategy;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.util.function.Tuple2;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class QueueOverFlux<T, R> {
	private final Set<T> elementsPending;
	private final Sinks.Many<T> sink;
	private final Flux<Map<T, R>> queue;
	public QueueOverFlux(final Sinks.Many<T> sink,
	                     final Flux<Map<T, R>> queue) {
		elementsPending = Collections.synchronizedSet(new HashSet<>());
		this.sink = sink;
		this.queue = queue.map(results -> {
				results.keySet().forEach(elementsPending::remove);
				return results;
			})
			             .publish()
			             .autoConnect();
	}
	public Mono<Map<T, R>> getResources(final List<T> requestData) {
		return Mono.just(requestData)
			       .map(data -> data.stream()
				                    .filter(e -> !elementsPending.contains(e))
				                    .map(addElementToSink())
				                    .collect(Collectors.toList()))
			       .zipWith(getResultFromResponseQueue(requestData))
			       .map(Tuple2::getT2);
	}
	private Mono<HashMap<T, R>> getResultFromResponseQueue(final List<T> requestData) {
		final Set<T> uniqueRequestData = Set.copyOf(requestData);
		final Set<T> alreadyFetched = new HashSet<>();

		return queue.takeUntil(e -> allRequestedDataIsInTheResponse(e, uniqueRequestData, alreadyFetched))
			       .flatMap(map -> Flux.fromIterable(map.entrySet()))
			       .filter(entry -> uniqueRequestData.contains(entry.getKey()))
			       .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()));
	}
	private static <T, R> boolean allRequestedDataIsInTheResponse(final Map<T, R> response, final Set<T> uniqueRequestData,
	                                                              final Set<T> existingData) {
		log.debug("Checking response data for {} against request data {} and existing data {}", response, uniqueRequestData, existingData);
		response.keySet().forEach(p -> {
			if (uniqueRequestData.contains(p)) {
				existingData.add(p);
			}
		});
		return uniqueRequestData.size() == existingData.size();
	}
	private Function<T, Sinks.EmitResult> addElementToSink() {
		return requestItem -> {
			log.debug("Adding data to sink {}", requestItem);
			elementsPending.add(requestItem);
			return sink.tryEmitNext(requestItem);
		};
	}
}
