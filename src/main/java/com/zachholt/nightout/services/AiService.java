package com.zachholt.nightout.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zachholt.nightout.controllers.ChatRequest;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.util.ArrayList;
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
    
    // GenAI API URL from Postman collection
    private static final String GENAI_API_URL = "https://lisa-rest-2067001295.us-east-1.elb.amazonaws.com";
    private static final String API_TOKEN = "test_token";
    private static final String DEFAULT_MODEL = "mistral-vllm";
    
    private static final String SYSTEM_PROMPT = 
        "You are the NightOut AI assistant, specialized in recommending places for users to enjoy their nights and days out. " +
        "Your primary focus is to suggest specific venues, restaurants, bars, and entertainment options based on the user's location context. " +
        "When making recommendations: " +
        "1. Always use location context when available to provide relevant nearby places. " +
        "2. Include specific details about each place (type of venue, atmosphere, specialties, typical crowd). " +
        "3. Suggest timely options (breakfast spots in morning, nightlife in evening). " +
        "4. Consider weather conditions if mentioned by the user. " +
        "5. Offer varied options across price ranges. " +
        "6. Suggest complementary venues for a complete experience (dinner followed by nearby entertainment). " +
        "When users ask about specific locations, provide helpful information about what to expect. " +
        "When users ask 'Where's everyone at' or about crowd levels: " +
        "1. If user density data is available, tell them which locations are currently busiest. " +
        "2. Recommend popular places that are likely to have good crowds based on the day and time. " +
        "3. Suggest both busy venues for those looking for crowds and quieter alternatives for those seeking less crowded options. " +
        "Keep responses friendly, enthusiastic, and concise while giving specific, actionable recommendations.";
    
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
     * Stream chat completion from AI API. Returns the raw streaming response as a Flux of strings.
     * Each string represents a chunk of the stream in SSE format.
     */
    public Flux<String> streamChatCompletion(List<Map<String, Object>> messages) {
        Map<String, Object> requestBody = buildRequestBody(messages, true);
        
        try {
            logger.info("Sending streaming request to AI API: {}", objectMapper.writeValueAsString(requestBody));
        } catch (Exception e) {
            logger.error("Error logging request: ", e);
        }
        
        // Return the raw text stream directly without transformation
        return webClient.post()
                .uri("/v2/serve/chat/completions")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                .bodyToFlux(String.class)
                .doOnError(e -> {
                    if (e instanceof WebClientResponseException) {
                        WebClientResponseException wcre = (WebClientResponseException) e;
                        logger.error("API Error Response: {} - {}", 
                                wcre.getStatusCode(), 
                                wcre.getResponseBodyAsString());
                    } else {
                        logger.error("Error calling AI API: ", e);
                    }
                })
                .onErrorResume(e -> {
                    return Flux.error(e);
                });
    }
    
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
                    .bodyToMono(String.class)
                    .map(response -> {
                        try {
                            // Convert string response to Map
                            return objectMapper.readValue(response, Map.class);
                        } catch (Exception e) {
                            logger.error("Error parsing AI API response: ", e);
                            // Return a simple map with error information
                            Map<String, Object> errorMap = new HashMap<>();
                            Map<String, Object> message = new HashMap<>();
                            message.put("content", "I'm sorry, I encountered an error processing your request.");
                            Map<String, Object> choice = new HashMap<>();
                            choice.put("message", message);
                            List<Map<String, Object>> choices = new ArrayList<>();
                            choices.add(choice);
                            errorMap.put("choices", choices);
                            return errorMap;
                        }
                    })
                    .onErrorResume(e -> {
                        logger.error("Error calling AI API: ", e);
                        // Return a fallback response on error
                        Map<String, Object> errorMap = new HashMap<>();
                        Map<String, Object> message = new HashMap<>();
                        message.put("content", "I'm sorry, I encountered an error connecting to the AI service.");
                        Map<String, Object> choice = new HashMap<>();
                        choice.put("message", message);
                        List<Map<String, Object>> choices = new ArrayList<>();
                        choices.add(choice);
                        errorMap.put("choices", choices);
                        return Mono.just(errorMap);
                    })
                    .block();
        } catch (Exception e) {
            logger.error("Unexpected error in chat completion: ", e);
            // Return a fallback response
            Map<String, Object> errorMap = new HashMap<>();
            Map<String, Object> message = new HashMap<>();
            message.put("content", "I'm sorry, an unexpected error occurred.");
            Map<String, Object> choice = new HashMap<>();
            choice.put("message", message);
            List<Map<String, Object>> choices = new ArrayList<>();
            choices.add(choice);
            errorMap.put("choices", choices);
            return errorMap;
        }
    }
    
    /**
     * Build the request body according to the GenAI Postman collection format
     */
    private Map<String, Object> buildRequestBody(List<Map<String, Object>> userMessages, boolean isStreaming) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", DEFAULT_MODEL);
        requestBody.put("temperature", null);
        requestBody.put("top_p", 0.01);
        requestBody.put("frequency_penalty", null);
        requestBody.put("presence_penalty", null);
        requestBody.put("max_tokens", null);
        requestBody.put("n", null);
        requestBody.put("stop", List.of("\nUser:", "\n User:", "User:", "User"));
        requestBody.put("stream", isStreaming);
        requestBody.put("seed", null);
        
        if (isStreaming) {
            requestBody.put("stream_options", Map.of("include_usage", true));
        }
        
        // Format messages for the AI API in exact Postman format
        List<Map<String, Object>> formattedMessages = new ArrayList<>();
        
        // Add system message first
        Map<String, Object> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", SYSTEM_PROMPT);
        formattedMessages.add(systemMessage);
        
        // Add all user and assistant messages
        formattedMessages.addAll(formatMessages(userMessages));
        
        requestBody.put("messages", formattedMessages);
        
        return requestBody;
    }
    
    /**
     * Helper method to format messages for the AI API
     */
    private List<Map<String, Object>> formatMessages(List<Map<String, Object>> originalMessages) {
        List<Map<String, Object>> formattedMessages = new ArrayList<>();
        
        for (Map<String, Object> message : originalMessages) {
            String role = (String) message.get("role");
            
            // Handle different message formats
            Object contentObj = message.get("content");
            String textContent = "";
            
            if (contentObj instanceof List) {
                // Handle structured content format (with type and text)
                List<Map<String, Object>> contentList = (List<Map<String, Object>>) contentObj;
                if (contentList != null && !contentList.isEmpty()) {
                    Map<String, Object> content = contentList.get(0);
                    textContent = (String) content.get("text");
                }
            } else if (contentObj instanceof String) {
                // Handle simple string content
                textContent = (String) contentObj;
            }
            
            // Add formatted message
            Map<String, Object> formattedMessage = new HashMap<>();
            formattedMessage.put("role", role);
            formattedMessage.put("content", textContent);
            formattedMessages.add(formattedMessage);
        }
        
        return formattedMessages;
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