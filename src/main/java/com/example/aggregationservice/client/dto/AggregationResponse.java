package com.example.aggregationservice.client.dto;

import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public record AggregationResponse(Map<String, Float> pricing, Map<Long, TrackingStatus> track,
                                  Map<Long, List<String>> shipments) {

}
