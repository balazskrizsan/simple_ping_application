package com.kbalazsworkds.providers;

import java.net.http.HttpClient;

public class HttpClientProvider
{
    public HttpClient createClient()
    {
        return HttpClient.newBuilder().build();
    }
}
