package com.example.aggregationservice.client.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum TrackingStatus {

	NEW("NEW"),
	IN_TRANSIT("IN TRANSIT"),
	COLLECTING("COLLECTING"),
	COLLECTED("COLLECTED"),
	DELIVERING("DELIVERING"),
	DELIVERED("DELIVERED");

	private static final Map<String, TrackingStatus> trackingStatusByValue =
		Arrays.stream(TrackingStatus.values())
			.collect(Collectors.toMap(TrackingStatus::getValue, Function.identity()));

	private final String value;


	@JsonCreator
	public static TrackingStatus forValue(String value) {
		return trackingStatusByValue.get(value);
	}

	@JsonValue
	public String toValue() {
		return this.getValue();
	}

}
