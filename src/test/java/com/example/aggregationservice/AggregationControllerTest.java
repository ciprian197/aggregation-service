package com.example.aggregationservice;

import com.example.aggregationservice.client.dto.AggregationRequest;
import com.example.aggregationservice.client.dto.AggregationResponse;
import com.example.aggregationservice.client.dto.TrackingStatus;
import com.example.aggregationservice.config.BackendServiceProperties;
import com.example.aggregationservice.exception.RestExceptionHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.ExactMatchMultiValuePattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.aggregationservice.exception.ErrorCode.BAD_REQUEST;
import static com.github.tomakehurst.wiremock.common.ContentTypes.APPLICATION_JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.test.context.TestConstructor.AutowireMode.ALL;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@TestConstructor(autowireMode = ALL)
@SpringBootTest
@AutoConfigureWebTestClient
class AggregationControllerTest {

	private static WireMockServer wireMockServer;
	private final ObjectMapper objectMapper = new ObjectMapper();

	private final BackendServiceProperties backendServiceProperties;
	private final WebTestClient webTestClient;

	public AggregationControllerTest(final BackendServiceProperties backendServiceProperties, final WebTestClient webTestClient) {
		this.backendServiceProperties = backendServiceProperties;
		this.webTestClient = webTestClient.mutate()
			                     .responseTimeout(Duration.of(15, ChronoUnit.SECONDS)).build();
	}

	@BeforeAll
	public static void setUp() {
		final int testPort = 8090;
		System.setProperty("aggregation-service.backend.base-url", "http://localhost:" + testPort);
		wireMockServer = new WireMockServer(testPort);
		wireMockServer.start();
		configureFor(testPort);
	}

	@AfterAll
	public static void tearDown() {
		wireMockServer.stop();
	}

	@Test
	public void getAggregationResult_EmptyRequest_ResponseWithNullFields() {
		// Given
		// When
		final AggregationResponse response = webTestClient.get().uri("/aggregation")
			                                     .exchange()
			                                     // Then
			                                     .expectStatus().isOk()
			                                     .expectBody(AggregationResponse.class)
			                                     .returnResult().getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.pricing()).isNull();
		assertThat(response.track()).isNull();
		assertThat(response.shipments()).isNull();
	}

	@Test
	public void getAggregationResult_RequestContainsOnlyPricingData_ResponseContainsPricing() throws JsonProcessingException {
		// Given
		final AggregationRequest aggregationRequest = AggregationRequest.builder()
			                                              .pricing(List.of("NL", "RO")).build();

		final Map<String, Float> pricingBackendResult = Map.of("NL", 1.234f, "RO", 2.4354f);

		mockBackendRequest(backendServiceProperties.getPricingApiPath(), aggregationRequest.pricing(), pricingBackendResult);

		// When
		final AggregationResponse response = webTestClient.get().uri(uriBuilder -> uriBuilder.path("/aggregation")
			                                                                           .queryParam("pricing", aggregationRequest.pricing()).build())
			                                     .exchange()
			                                     // Then
			                                     .expectStatus().isOk()
			                                     .expectBody(AggregationResponse.class)
			                                     .returnResult().getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.pricing()).isEqualTo(pricingBackendResult);
		assertThat(response.track()).isNull();
		assertThat(response.shipments()).isNull();
	}

	@Test
	public void getAggregationResult_AllDataRequested_ResponseContainsCompleteData() throws JsonProcessingException {
		// Given
		final AggregationRequest aggregationRequest = AggregationRequest.builder()
			                                              .pricing(List.of("NL", "RO"))
			                                              .shipments(List.of(123456786L, 987654326L))
			                                              .track(List.of(123456239L, 987654521L)).build();

		final Map<String, Float> pricingBackendResult = Map.of("NL", 1.234f, "RO", 2.4354f);
		final Map<Long, List<String>> shipmentsBackendResult = Map.of(123456786L, List.of("box", "pallet"), 987654326L, List.of("element"));
		final Map<Long, TrackingStatus> trackingBackendResult = Map.of(123456239L, TrackingStatus.COLLECTED, 987654521L, TrackingStatus.COLLECTING);

		mockBackendRequest(backendServiceProperties.getPricingApiPath(), aggregationRequest.pricing(), pricingBackendResult);
		mockBackendRequest(backendServiceProperties.getShipmentsApiPath(), aggregationRequest.shipments(), shipmentsBackendResult);
		mockBackendRequest(backendServiceProperties.getTrackApiPath(), aggregationRequest.track(), trackingBackendResult);

		// When
		final AggregationResponse response = webTestClient.get().uri(uriBuilder -> uriBuilder.path("/aggregation")
			                                                                           .queryParam("pricing", aggregationRequest.pricing())
			                                                                           .queryParam("shipments", aggregationRequest.shipments())
			                                                                           .queryParam("track", aggregationRequest.track()).build())
			                                     .exchange()
			                                     // Then
			                                     .expectStatus().isOk()
			                                     .expectBody(AggregationResponse.class)
			                                     .returnResult().getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.pricing()).isEqualTo(pricingBackendResult);
		assertThat(response.shipments()).isEqualTo(shipmentsBackendResult);
		assertThat(response.track()).isEqualTo(trackingBackendResult);
	}

	@Test
	public void getAggregationResult_AllDataRequestedButShipmentsDoesNotRespond_ResponseContainsDataWithShipmentsAsNull() throws JsonProcessingException {
		// Given
		final AggregationRequest aggregationRequest = AggregationRequest.builder()
			                                              .pricing(List.of("NL", "RO"))
			                                              .shipments(List.of(123456787L, 987654327L))
			                                              .track(List.of(123456239L, 987654521L)).build();

		final Map<String, Float> pricingBackendResult = Map.of("NL", 1.234f, "RO", 2.4354f);
		final Map<Long, List<String>> shipmentsBackendResult = new HashMap<>();
		shipmentsBackendResult.put(123456787L, null);
		shipmentsBackendResult.put(987654327L, null);
		final Map<Long, TrackingStatus> trackingBackendResult = Map.of(123456239L, TrackingStatus.COLLECTED, 987654521L, TrackingStatus.COLLECTING);

		mockBackendRequest(backendServiceProperties.getPricingApiPath(), aggregationRequest.pricing(), pricingBackendResult);
		mockBackendRequest(backendServiceProperties.getTrackApiPath(), aggregationRequest.track(), trackingBackendResult);

		// When
		final AggregationResponse response = webTestClient.get().uri(uriBuilder -> uriBuilder.path("/aggregation")
			                                                                           .queryParam("pricing", aggregationRequest.pricing())
			                                                                           .queryParam("shipments", aggregationRequest.shipments())
			                                                                           .queryParam("track", aggregationRequest.track()).build())
			                                     .exchange()
			                                     // Then
			                                     .expectStatus().isOk()
			                                     .expectBody(AggregationResponse.class)
			                                     .returnResult().getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.pricing()).isEqualTo(pricingBackendResult);
		assertThat(response.shipments()).isEqualTo(shipmentsBackendResult);
		assertThat(response.track()).isEqualTo(trackingBackendResult);
	}

	@Test
	public void getAggregationResult_InvalidRequest_ErrorResponse() {
		// Given
		final List<String> trackIds = List.of("abfs", "sdaw");

		// When
		final RestExceptionHandler.ErrorResponse response = webTestClient.get().uri(uriBuilder -> uriBuilder.path("/aggregation")
			                                                                           .queryParam("track", trackIds).build())
			                                     .exchange()
			                                     // Then
			                                     .expectStatus().is4xxClientError()
			                                     .expectBody(RestExceptionHandler.ErrorResponse.class)
			                                     .returnResult().getResponseBody();
		assertThat(response.getCode()).isEqualTo(BAD_REQUEST.getCode());
	}

	private <T, R> void mockBackendRequest(final String path, final List<T> requestIds, final Map<T, R> mockedBackendResult) throws JsonProcessingException {
		final List<StringValuePattern> valuePatterns = requestIds.stream()
			                                               .map(T::toString)
			                                               .map(WireMock::equalTo)
			                                               .collect(Collectors.toList());

		stubFor(get(urlPathEqualTo(path))
			        .withQueryParam("q", new ExactMatchMultiValuePattern(valuePatterns))
			        .willReturn(aResponse()
				                    .withHeader(CONTENT_TYPE, APPLICATION_JSON)
				                    .withStatus(200)
				                    .withBody(objectMapper.writeValueAsString(mockedBackendResult))));
	}

}
