package com.example.aggregationservice.client.dto;


import lombok.Builder;

import java.util.List;

@Builder
public record AggregationRequest(List<String> pricing, List<Long> track, List<Long> shipments) {
}
