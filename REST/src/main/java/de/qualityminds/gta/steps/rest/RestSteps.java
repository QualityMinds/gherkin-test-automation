package de.qualityminds.gta.steps.rest;

import static net.thucydides.core.steps.StepEventBus.getEventBus;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.restassured.response.ResponseBodyExtractionOptions;
import io.restassured.specification.QueryableRequestSpecification;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.SpecificationQuerier;
import net.serenitybdd.core.rest.RestMethod;
import net.serenitybdd.core.rest.RestQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.qualityminds.gta.steps.StepsBase;
import de.qualityminds.gta.steps.rest.config.RestAssuredProperties;
import de.qualityminds.gta.steps.rest.exceptions.ResponseMappingException;

public class RestSteps extends StepsBase {
	private static final Logger logger = LoggerFactory.getLogger(RestSteps.class);

	private static final ThreadLocal<RequestSpecification> request = new ThreadLocal<>();
	private static final ThreadLocal<Response> lastResponse = new ThreadLocal<>();

	private static final String HIDDEN_VAR = "[HIDDEN]";

	@Autowired
	RestAssuredProperties properties;

	public void basicAuth(String username, String password) {
		request.set(given().auth().preemptive().basic(username, password));
	}

	public void GET(String url) {
		GET(url, true);
	}

	public void GET(String url, boolean logToReport) {
		logger.info("Sending GET to {}", url);
		lastResponse.set(when().get(url));
		if (logToReport) {
			logRequestAndResponseToReportAndConsole(RestMethod.GET);
		}
	}

	public void POST(String url) {
		lastResponse.set(when().post(url));
		logRequestAndResponseToReportAndConsole(RestMethod.POST);
	}

	public void POST(String url, Object jsonBody, String username, String password) {
		lastResponse.set(given().auth().preemptive().basic(username, password).contentType("application/json").body(jsonBody).when().post(url));
		logRequestAndResponseToReportAndConsole(RestMethod.POST);
	}

	public void PUT(String url) {
		lastResponse.set(when().put(url));
		logRequestAndResponseToReportAndConsole(RestMethod.PUT);
	}

	public void PUT(String url, Object jsonBody, String username, String password) {
		lastResponse.set(given().auth().preemptive().basic(username, password).contentType("application/json").body(jsonBody).when().put(url));
		logRequestAndResponseToReportAndConsole(RestMethod.PUT);
	}

	public int getStatus() {
		return lastResponse.get().getStatusCode();
	}

	public <T> T bodyAs(Class<T> cls) throws ResponseMappingException {
		ResponseBodyExtractionOptions body = thenExtractBody();
		try {
			return body.as(cls);
		} catch (Exception cce) {
			throw new ResponseMappingException(cce.getMessage(), cce);
		}

	}

	private ResponseBodyExtractionOptions thenExtractBody() {
		return lastResponse.get().then().extract().body();
	}

	private RequestSpecification given() {

		LogConfig ourLogConfig = LogConfig.logConfig();
		boolean failOnUnknownProperties = true;

		if (properties != null) {
			ourLogConfig = ourLogConfig.blacklistHeaders(properties.getHiddenHeaders());
			failOnUnknownProperties = !properties.getIgnoreUnknownFields();
		}


		RequestSpecification given = RestAssured.given()
				.config(
						RestAssured.config()
								.logConfig(ourLogConfig)
								.objectMapperConfig(getObjectMapper(failOnUnknownProperties))
				)
				.log().all(true);
		request.set(given);
		return given;
	}

	private RequestSpecification when() {
		if (request.get() == null) {
			request.set(given());
		}
		return request.get();
	}

	private void logRequestAndResponseToReportAndConsole(RestMethod method) {

		boolean hideCookiesFromReport = false;
		boolean hideHeadersFromReport = false;
		if (properties != null) {
			hideCookiesFromReport = properties.getHideCookiesFromReport();
			hideHeadersFromReport = properties.getHideHeadersFromReport();
		}

		RequestSpecification getRequest = RestSteps.request.get();
		if (getRequest == null) {
			logger.warn("Trying to log request but no request was created yet.");
			return;
		}


		QueryableRequestSpecification reqSpec = SpecificationQuerier.query(getRequest);

		Set<String> blacklistedHeaders = reqSpec.getConfig().getLogConfig().blacklistedHeaders();
		List<Header> headers = getFilteredHeaders(reqSpec.getHeaders().asList(), blacklistedHeaders);


		RestQuery query = RestQuery.withMethod(method).andPath(reqSpec.getURI())
				.withContent(reqSpec.getBody()).withContentType(reqSpec.getContentType())
				.withRequestHeaders(hideHeadersFromReport ? HIDDEN_VAR : String.valueOf(headers))
				.withRequestCookies(hideCookiesFromReport ? HIDDEN_VAR : String.valueOf(reqSpec.getCookies()));


		Response response = lastResponse.get();
		if (response != null) {
			logger.info("\n--------------------------------------- RESPONSE ---------------------------------------\n");
			response.then().log().headers().log().cookies();

			query = query.withResponse(response.body().prettyPrint())
					.withStatusCode(response.getStatusCode())
					.withResponseHeaders(hideHeadersFromReport ? HIDDEN_VAR : String.valueOf(response.getHeaders()))
					.withResponseCookies(hideCookiesFromReport ? HIDDEN_VAR : String.valueOf(response.getCookies()));
		}


		if (getEventBus().isBaseStepListenerRegistered()) {
			getEventBus().getBaseStepListener().recordRestQuery(query);
		} else {
			logger.warn("Data was not written to Serenity report: StepListener not connected");
		}
	}

	private List<Header> getFilteredHeaders(List<Header> headers, Set<String> blacklistedHeaders) {
		return headers.stream().map(h -> {
			if (blacklistedHeaders.contains(h.getName())) {
				return new Header(h.getName(), HIDDEN_VAR);
			} else {
				return h;
			}
		}).collect(Collectors.toList());
	}

	private static ObjectMapperConfig getObjectMapper(boolean failOnUnknown) {
		return new ObjectMapperConfig().jackson2ObjectMapperFactory((cls, charset) -> {
			ObjectMapper om = new ObjectMapper().findAndRegisterModules();
			om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, failOnUnknown);
			return om;
		});
	}
}
