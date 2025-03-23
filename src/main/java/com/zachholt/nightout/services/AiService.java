package com.zachholt.nightout.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AiService {

    private static final Logger logger = LoggerFactory.getLogger(AiService.class);
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    
    @Value("${ai.api.url}")
    private String aiApiUrl;
    
    @Value("${ai.api.token}")
    private String aiApiToken;
    
    public AiService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.webClient = webClientBuilder.baseUrl(aiApiUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .filters(exchangeFilterFunctions -> {
                    exchangeFilterFunctions.add(logRequest());
                    exchangeFilterFunctions.add(logResponse());
                })
                .build();
        
        // Log the token format for debugging
        logger.info("AI API URL: {}", aiApiUrl);
        logger.info("AI API Token format: {} (length: {})", 
                aiApiToken != null ? (aiApiToken.startsWith("Bearer") ? "Bearer..." : "token...") : "null",
                aiApiToken != null ? aiApiToken.length() : 0);
    }
    
    /**
     * Stream chat completion from AI API. Returns the raw streaming response as a Flux of strings.
     * Each string represents a chunk of the stream in SSE format.
     */
    public Flux<String> streamChatCompletion(List<Map<String, Object>> messages) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "claude-3-7");
        requestBody.put("temperature", null);
        requestBody.put("top_p", 0.01);
        requestBody.put("frequency_penalty", null);
        requestBody.put("presence_penalty", null);
        requestBody.put("max_tokens", null);
        requestBody.put("n", null);
        requestBody.put("stream", true);
        requestBody.put("seed", null);
        requestBody.put("stream_options", Map.of("include_usage", true));
        requestBody.put("messages", messages);
        requestBody.put("stop", List.of("\nUser:", "\n User:", "User:", "User"));
        
        try {
            logger.info("Sending streaming request to AI API: {}", objectMapper.writeValueAsString(requestBody));
        } catch (Exception e) {
            logger.error("Error logging request: ", e);
        }
        
        // Return the raw text stream directly without transformation
        return webClient.post()
                .uri("/v2/serve/chat/completions")
                .header("Authorization", aiApiToken)
                .body(BodyInserters.fromValue(requestBody))
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(String.class)
                .onErrorResume(e -> {
                    if (e instanceof WebClientResponseException) {
                        WebClientResponseException wcre = (WebClientResponseException) e;
                        logger.error("API Error Response: {} - {}", 
                                wcre.getStatusCode(), 
                                wcre.getResponseBodyAsString());
                    } else {
                        logger.error("Error calling AI API: ", e);
                    }
                    return Flux.error(e);
                });
    }
    
    public Map<String, Object> chatCompletion(List<Map<String, Object>> messages) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "claude-3-7");
        requestBody.put("temperature", null);
        requestBody.put("top_p", 0.01);
        requestBody.put("frequency_penalty", null);
        requestBody.put("presence_penalty", null);
        requestBody.put("max_tokens", null);
        requestBody.put("n", null);
        requestBody.put("stream", false);
        requestBody.put("seed", null);
        requestBody.put("messages", messages);
        requestBody.put("stop", List.of("\nUser:", "\n User:", "User:", "User"));
        
        try {
            logger.info("Sending request to AI API: {}", objectMapper.writeValueAsString(requestBody));
        } catch (Exception e) {
            logger.error("Error logging request: ", e);
        }
        
        try {
            return webClient.post()
                    .uri("/v2/serve/chat/completions")
                    .header("Authorization", aiApiToken)
                    .body(BodyInserters.fromValue(requestBody))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (WebClientResponseException e) {
            logger.error("API Error Response: {} - {}", 
                    e.getStatusCode(), 
                    e.getResponseBodyAsString());
            throw e;
        }
    }
    
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            logger.info("Request: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers()
                    .forEach((name, values) -> values.forEach(value -> {
                        if ("Authorization".equalsIgnoreCase(name)) {
                            logger.info("{}=<REDACTED>", name);
                        } else {
                            logger.info("{}={}", name, value);
                        }
                    }));
            return Mono.just(clientRequest);
        });
    }
    
    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            logger.info("Response status: {}", clientResponse.statusCode());
            clientResponse.headers().asHttpHeaders()
                    .forEach((name, values) -> values.forEach(value -> logger.info("{}={}", name, value)));
            return Mono.just(clientResponse);
        });
    }
} 