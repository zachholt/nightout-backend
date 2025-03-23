package com.zachholt.nightout.models.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatResponse {
    private String id;
    private String object;
    private long created;
    private String model;
    private String system_fingerprint;
    private Object choices; // Can be either a single ChatChoices object or an array
    private ChatUsage usage;
    @JsonProperty("provider_specific_fields")
    private Map<String, Object> providerSpecificFields;
    @JsonProperty("stream_options")
    private StreamOptions streamOptions;
    @JsonProperty("service_tier")
    private String serviceTier;

    public ChatResponse() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSystem_fingerprint() {
        return system_fingerprint;
    }

    public void setSystem_fingerprint(String system_fingerprint) {
        this.system_fingerprint = system_fingerprint;
    }

    public Object getChoices() {
        return choices;
    }

    public void setChoices(Object choices) {
        this.choices = choices;
    }

    public ChatUsage getUsage() {
        return usage;
    }

    public void setUsage(ChatUsage usage) {
        this.usage = usage;
    }

    public Map<String, Object> getProviderSpecificFields() {
        return providerSpecificFields;
    }

    public void setProviderSpecificFields(Map<String, Object> providerSpecificFields) {
        this.providerSpecificFields = providerSpecificFields;
    }

    public StreamOptions getStreamOptions() {
        return streamOptions;
    }

    public void setStreamOptions(StreamOptions streamOptions) {
        this.streamOptions = streamOptions;
    }

    public String getServiceTier() {
        return serviceTier;
    }

    public void setServiceTier(String serviceTier) {
        this.serviceTier = serviceTier;
    }

    // Helper methods to extract content
    public String getContent() {
        if (choices instanceof List) {
            List<?> choicesList = (List<?>) choices;
            if (!choicesList.isEmpty()) {
                if (choicesList.get(0) instanceof Map) {
                    Map<?, ?> choice = (Map<?, ?>) choicesList.get(0);
                    
                    // For non-streaming response
                    if (choice.containsKey("message")) {
                        Map<?, ?> message = (Map<?, ?>) choice.get("message");
                        if (message != null && message.containsKey("content")) {
                            return (String) message.get("content");
                        }
                    }
                    
                    // For streaming response
                    if (choice.containsKey("delta")) {
                        Map<?, ?> delta = (Map<?, ?>) choice.get("delta");
                        if (delta != null && delta.containsKey("content")) {
                            return (String) delta.get("content");
                        }
                    }
                }
            }
        } else if (choices instanceof Map) {
            Map<?, ?> choicesMap = (Map<?, ?>) choices;
            
            // For streaming response with delta
            if (choicesMap.containsKey("delta")) {
                Map<?, ?> delta = (Map<?, ?>) choicesMap.get("delta");
                if (delta != null && delta.containsKey("content")) {
                    return (String) delta.get("content");
                }
            }
        }
        return null;
    }

    public static class StreamOptions {
        private boolean include_usage;

        public StreamOptions() {
        }

        public boolean isInclude_usage() {
            return include_usage;
        }

        public void setInclude_usage(boolean include_usage) {
            this.include_usage = include_usage;
        }
    }
}

// ChatChoices and ChatUsage remain the same but are made more flexible with @JsonIgnoreProperties
@JsonIgnoreProperties(ignoreUnknown = true)
class ChatChoices {
    private int index;
    private ChatMessage delta; // For streaming format
    private ChatMessage message; // For non-streaming format
    private String finish_reason;

    public ChatChoices() {
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public ChatMessage getDelta() {
        return delta;
    }

    public void setDelta(ChatMessage delta) {
        this.delta = delta;
    }

    public ChatMessage getMessage() {
        return message;
    }

    public void setMessage(ChatMessage message) {
        this.message = message;
    }

    public String getFinish_reason() {
        return finish_reason;
    }

    public void setFinish_reason(String finish_reason) {
        this.finish_reason = finish_reason;
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class ChatUsage {
    private int prompt_tokens;
    private int completion_tokens;
    private int total_tokens;

    public ChatUsage() {
    }

    public int getPrompt_tokens() {
        return prompt_tokens;
    }

    public void setPrompt_tokens(int prompt_tokens) {
        this.prompt_tokens = prompt_tokens;
    }

    public int getCompletion_tokens() {
        return completion_tokens;
    }

    public void setCompletion_tokens(int completion_tokens) {
        this.completion_tokens = completion_tokens;
    }

    public int getTotal_tokens() {
        return total_tokens;
    }

    public void setTotal_tokens(int total_tokens) {
        this.total_tokens = total_tokens;
    }
} 