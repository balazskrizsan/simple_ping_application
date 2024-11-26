package com.kbalazsworks.simple_ping_application.providers;

import java.net.http.HttpClient;

public class HttpClientProvider
{
    public HttpClient createClient()
    {
        return HttpClient.newBuilder().build();
    }
}
