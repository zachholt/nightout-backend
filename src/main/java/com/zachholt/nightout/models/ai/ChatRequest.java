package com.zachholt.nightout.models.ai;

import java.util.List;

public class ChatRequest {
    private String model;
    private Double temperature;
    private Double top_p;
    private Double frequency_penalty;
    private Double presence_penalty;
    private Integer max_tokens;
    private Integer n;
    private List<String> stop;
    private boolean stream;
    private Long seed;
    private StreamOptions stream_options;
    private List<ChatMessage> messages;

    public ChatRequest() {
    }

    // Builder method for common simple usage
    public static ChatRequest createSimpleRequest(String model, String userMessage, boolean stream) {
        ChatRequest request = new ChatRequest();
        request.setModel(model);
        request.setStream(stream);
        request.setTop_p(0.01);
        request.setMessages(List.of(new ChatMessage("user", userMessage)));
        request.setStop(List.of("\\nUser:", "\\n User:", "User:", "User"));
        
        if (stream) {
            request.setStream_options(new StreamOptions(true));
        }
        
        return request;
    }

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

    public Double getTop_p() {
        return top_p;
    }

    public void setTop_p(Double top_p) {
        this.top_p = top_p;
    }

    public Double getFrequency_penalty() {
        return frequency_penalty;
    }

    public void setFrequency_penalty(Double frequency_penalty) {
        this.frequency_penalty = frequency_penalty;
    }

    public Double getPresence_penalty() {
        return presence_penalty;
    }

    public void setPresence_penalty(Double presence_penalty) {
        this.presence_penalty = presence_penalty;
    }

    public Integer getMax_tokens() {
        return max_tokens;
    }

    public void setMax_tokens(Integer max_tokens) {
        this.max_tokens = max_tokens;
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

    public boolean isStream() {
        return stream;
    }

    public void setStream(boolean stream) {
        this.stream = stream;
    }

    public Long getSeed() {
        return seed;
    }

    public void setSeed(Long seed) {
        this.seed = seed;
    }

    public StreamOptions getStream_options() {
        return stream_options;
    }

    public void setStream_options(StreamOptions stream_options) {
        this.stream_options = stream_options;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public static class StreamOptions {
        private boolean include_usage;

        public StreamOptions() {
        }

        public StreamOptions(boolean include_usage) {
            this.include_usage = include_usage;
        }

        public boolean isInclude_usage() {
            return include_usage;
        }

        public void setInclude_usage(boolean include_usage) {
            this.include_usage = include_usage;
        }
    }
} 