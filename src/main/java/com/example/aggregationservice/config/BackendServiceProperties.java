package com.example.aggregationservice.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "aggregation-service.backend")
public class BackendServiceProperties {

	private String baseUrl;
	private String shipmentsApiPath;
	private String trackApiPath;
	private String pricingApiPath;
	private int connectionTimeoutInMillis;
	private int responseTimeoutInMillis;

}
