package com.zachholt.nightout.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

/**
 * Configuration for a mock AI service for local testing.
 * This will be activated only in local and test profiles.
 */
@Configuration
@Profile({"local", "test"})
public class MockAiServiceConfig {

    @Bean
    public RouterFunction<ServerResponse> mockAiRoute() {
        return route(POST("/mock-ai/v1/chat/completions"), request -> {
            // Create a mock response similar to the real AI API
            Map<String, Object> message = new HashMap<>();
            message.put("role", "assistant");
            message.put("content", "This is a mock response from the AI API for local testing");

            Map<String, Object> choice = new HashMap<>();
            choice.put("index", 0);
            choice.put("message", message);
            choice.put("finish_reason", "stop");

            Map<String, Object> response = new HashMap<>();
            response.put("id", "mock-response-123");
            response.put("object", "chat.completion");
            response.put("created", System.currentTimeMillis() / 1000);
            response.put("model", "mock-model");
            response.put("choices", List.of(choice));
            response.put("usage", Map.of(
                "prompt_tokens", 10,
                "completion_tokens", 20,
                "total_tokens", 30
            ));

            return ok().bodyValue(response);
        });
    }

    @Bean
    public RouterFunction<ServerResponse> mockAiStreamRoute() {
        return route(POST("/mock-ai/v1/chat/completions"), request -> {
            // Handle streaming requests with a mock SSE stream
            return request.bodyToMono(Map.class)
                .flatMap(body -> {
                    if (body.containsKey("stream") && (boolean) body.get("stream")) {
                        return ok()
                            .contentType(org.springframework.http.MediaType.TEXT_EVENT_STREAM)
                            .bodyValue("data: {\"choices\":[{\"delta\":{\"content\":\"Mock\"}}]}\n\n" +
                                       "data: {\"choices\":[{\"delta\":{\"content\":\" streaming\"}}]}\n\n" +
                                       "data: {\"choices\":[{\"delta\":{\"content\":\" response\"}}]}\n\n" +
                                       "data: {\"choices\":[{\"delta\":{\"content\":\" for\"}}]}\n\n" +
                                       "data: {\"choices\":[{\"delta\":{\"content\":\" testing\"}}]}\n\n" +
                                       "data: [DONE]\n\n");
                    }
                    
                    // Non-streaming requests will be handled by the other route
                    return ServerResponse.badRequest().build();
                });
        });
    }
} 