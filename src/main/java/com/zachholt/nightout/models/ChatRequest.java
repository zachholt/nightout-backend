package com.zachholt.nightout.models;

import java.util.List;
import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatRequest {
    private String model;
    private Double temperature;
    private Double topP;
    private Double frequencyPenalty;
    private Double presencePenalty;
    private Integer maxTokens;
    private Integer n;
    private List<String> stop = new ArrayList<>();
    private Boolean stream = false;
    private Integer seed;
    private StreamOptions streamOptions;
    private List<Message> messages = new ArrayList<>();
    
    // Nested class for messages
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        private String role;
        private String content;
        
        public Message() {}
        
        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
        
        public String getRole() {
            return role;
        }
        
        public void setRole(String role) {
            this.role = role;
        }
        
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
        
        @Override
        public String toString() {
            return "Message{" +
                    "role='" + role + '\'' +
                    ", content='" + content + '\'' +
                    '}';
        }
    }
    
    // Nested class for stream options
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StreamOptions {
        private Boolean includeUsage;
        
        public StreamOptions() {}
        
        public StreamOptions(Boolean includeUsage) {
            this.includeUsage = includeUsage;
        }
        
        @JsonProperty("include_usage")
        public Boolean getIncludeUsage() {
            return includeUsage;
        }
        
        @JsonProperty("include_usage")
        public void setIncludeUsage(Boolean includeUsage) {
            this.includeUsage = includeUsage;
        }
        
        @Override
        public String toString() {
            return "StreamOptions{" +
                    "includeUsage=" + includeUsage +
                    '}';
        }
    }
    
    // Getters and Setters
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public Double getTemperature() {
        return temperature;
    }
    
    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }
    
    @JsonProperty("top_p")
    public Double getTopP() {
        return topP;
    }
    
    @JsonProperty("top_p")
    public void setTopP(Double topP) {
        this.topP = topP;
    }
    
    @JsonProperty("frequency_penalty")
    public Double getFrequencyPenalty() {
        return frequencyPenalty;
    }
    
    @JsonProperty("frequency_penalty")
    public void setFrequencyPenalty(Double frequencyPenalty) {
        this.frequencyPenalty = frequencyPenalty;
    }
    
    @JsonProperty("presence_penalty")
    public Double getPresencePenalty() {
        return presencePenalty;
    }
    
    @JsonProperty("presence_penalty")
    public void setPresencePenalty(Double presencePenalty) {
        this.presencePenalty = presencePenalty;
    }
    
    @JsonProperty("max_tokens")
    public Integer getMaxTokens() {
        return maxTokens;
    }
    
    @JsonProperty("max_tokens")
    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }
    
    public Integer getN() {
        return n;
    }
    
    public void setN(Integer n) {
        this.n = n;
    }
    
    public List<String> getStop() {
        return stop;
    }
    
    public void setStop(List<String> stop) {
        this.stop = stop;
    }
    
    public Boolean getStream() {
        return stream;
    }
    
    public void setStream(Boolean stream) {
        this.stream = stream;
    }
    
    public Integer getSeed() {
        return seed;
    }
    
    public void setSeed(Integer seed) {
        this.seed = seed;
    }
    
    @JsonProperty("stream_options")
    public StreamOptions getStreamOptions() {
        return streamOptions;
    }
    
    @JsonProperty("stream_options")
    public void setStreamOptions(StreamOptions streamOptions) {
        this.streamOptions = streamOptions;
    }
    
    public List<Message> getMessages() {
        return messages;
    }
    
    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
    
    @Override
    public String toString() {
        return "ChatRequest{" +
                "model='" + model + '\'' +
                ", temperature=" + temperature +
                ", topP=" + topP +
                ", frequencyPenalty=" + frequencyPenalty +
                ", presencePenalty=" + presencePenalty +
                ", maxTokens=" + maxTokens +
                ", n=" + n +
                ", stop=" + stop +
                ", stream=" + stream +
                ", seed=" + seed +
                ", streamOptions=" + streamOptions +
                ", messages=" + messages +
                '}';
    }
}
