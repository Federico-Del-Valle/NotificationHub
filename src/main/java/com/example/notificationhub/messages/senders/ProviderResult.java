package com.example.notificationhub.messages.senders;

public record ProviderResult(boolean success, String response) {
    public static ProviderResult ok(String response) {
        return new ProviderResult(true, response);
    }
    public static ProviderResult fail(String response) {
        return new ProviderResult(false, response);
    }
}
