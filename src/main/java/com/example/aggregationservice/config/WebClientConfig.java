package com.example.aggregationservice.config;

import io.netty.channel.ChannelOption;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.time.temporal.ChronoUnit;


@RequiredArgsConstructor
@Configuration
public class WebClientConfig {

	private final BackendServiceProperties backendServiceProperties;

	@Bean
	public WebClient backendWebClient(){
		final HttpClient httpClient = HttpClient.create()
			                              .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, backendServiceProperties.getConnectionTimeoutInMillis())
			                              .responseTimeout(Duration.of(backendServiceProperties.getResponseTimeoutInMillis(), ChronoUnit.MILLIS));

		final ClientHttpConnector clientHttpConnector = new ReactorClientHttpConnector(httpClient);

		return WebClient.builder()
			       .baseUrl(backendServiceProperties.getBaseUrl())
			       .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			       .clientConnector(clientHttpConnector).build();
	}

}
