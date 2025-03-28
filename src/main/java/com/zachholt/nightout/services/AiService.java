package com.zachholt.nightout.services;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zachholt.nightout.models.ChatRequest;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import reactor.netty.http.client.HttpClient;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AiService {

    private static final Logger logger = LoggerFactory.getLogger(AiService.class);
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    
    // GenAI API URL from Postman collection
    private static final String GENAI_API_URL = "https://lisa-rest-2067001295.us-east-1.elb.amazonaws.com";
    private static final String API_TOKEN = "test_token";
    
    public AiService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        
        WebClient tempWebClient;
        try {
            // Create SSL context that ignores certificate validation
            final SslContext sslContext = SslContextBuilder
                .forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();
            
            // Create HTTP client with the insecure SSL context
            final HttpClient httpClient = HttpClient.create()
                .secure(t -> t.sslContext(sslContext));
                
            // Build the WebClient with SSL ignoring configuration
            tempWebClient = webClientBuilder.baseUrl(GENAI_API_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, "*/*")
                .defaultHeader(HttpHeaders.AUTHORIZATION, API_TOKEN)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
        } catch (Exception e) {
            logger.error("Failed to configure SSL context, using default WebClient", e);
            tempWebClient = webClientBuilder.baseUrl(GENAI_API_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, "*/*")
                .defaultHeader(HttpHeaders.AUTHORIZATION, API_TOKEN)
                .build();
        }
        this.webClient = tempWebClient;
        
        logger.info("AI Service initialized with API URL: {}", GENAI_API_URL);
    }
    
    /**
     * Send a chat completion request to the AI API and return the response.
     */
    public Map<String, Object> chatCompletion(ChatRequest chatRequest) {        
        try {
            logger.info("Sending request to AI API: {}", objectMapper.writeValueAsString(chatRequest));
        } catch (Exception e) {
            logger.error("Error logging request: ", e);
        }
        
        try {
            return webClient.post()
                    .uri("/v2/serve/chat/completions")
                    .body(BodyInserters.fromValue(chatRequest))
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
}