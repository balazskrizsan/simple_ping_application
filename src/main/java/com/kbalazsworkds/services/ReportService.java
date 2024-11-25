package com.kbalazsworkds.services;

import com.google.gson.Gson;
import com.kbalazsworkds.entities.Report;
import com.kbalazsworkds.extensions.ApplicationProperties;
import com.kbalazsworkds.providers.HttpClientProvider;
import com.kbalazsworkds.repositories.IcmpPingRepository;
import com.kbalazsworkds.repositories.TcpPingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@RequiredArgsConstructor
@Log4j2
public class ReportService
{
    private final HttpClientProvider httpClientProvider;
    private final ApplicationProperties applicationProperties;
    private final TcpPingRepository tcpPingRepository;
    private final IcmpPingRepository icmpPingRepository;

    private static final Gson gson = new Gson();

    public synchronized void report(String host)
    {
        Report report = new Report(
            host,
            icmpPingRepository.get(host) != null ? icmpPingRepository.get(host).result() : "No provided data yet",
            tcpPingRepository.get(host) != null ? tcpPingRepository.get(host).result() : "No provided data yet"
        );

        log.warn("Ping report: {}", gson.toJson(report));

        try (HttpClient client = httpClientProvider.createClient())
        {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(applicationProperties.getPingServiceReportUrl()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(report)))
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200)
            {
                log.error("Report HTTP error: {}", response);
            }
        }
        catch (IOException e)
        {
            log.error("Report HTTP IO Exception", e);
        }
        catch (InterruptedException e)
        {
            log.error("Report HTTP Interrupted Exception", e);
        }
    }
}
