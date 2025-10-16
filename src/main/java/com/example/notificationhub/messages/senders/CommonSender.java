package com.example.notificationhub.messages.senders;

import com.example.notificationhub.messages.Provider;

public interface CommonSender {
    Provider provider();
    ProviderResult send(String destination, String content);
}
