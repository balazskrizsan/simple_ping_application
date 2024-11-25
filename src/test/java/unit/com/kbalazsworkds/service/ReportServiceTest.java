package unit.com.kbalazsworkds.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kbalazsworkds.entities.PingResult;
import com.kbalazsworkds.entities.Report;
import com.kbalazsworkds.extensions.ApplicationProperties;
import com.kbalazsworkds.extensions.LocalDateTimeAdapter;
import com.kbalazsworkds.providers.HttpClientProvider;
import com.kbalazsworkds.services.IcmpPingService;
import com.kbalazsworkds.services.ReportService;
import lombok.SneakyThrows;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.Test;
import unit.com.kbalazsworkds.helpers.MockCreateHelper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReportServiceTest
{
    private static final Gson gson = new GsonBuilder()
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
        .create();

    @Test
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public void report_PostWith200response_perfect()
    {
        // Arrange
        ApplicationProperties applicationProperties = MockCreateHelper.applicationProperties_default();
        String testedHost = "localhost.balazskrizsan.com";

        String expectedLog = "Ping report: " + gson.toJson(new Report(testedHost, "icmpPingResult"));
        HttpRequest expectedRequest = HttpRequest.newBuilder()
            .uri(URI.create(applicationProperties.getPingServiceReportUrl()))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(expectedLog))
            .build();

        IcmpPingService.LAST_ICMP_RESULTS.put(
            testedHost,
            new PingResult(false, LocalDateTime.of(2020, 1, 2, 3, 4, 5), "icmpPingResult")
        );

        HttpResponse<String> httpResponseMock = mock(HttpResponse.class);
        when(httpResponseMock.statusCode()).thenReturn(200);

        HttpClient httpClientMock = mock(HttpClient.class);
        when(httpClientMock.send(eq(expectedRequest), any(HttpResponse.BodyHandler.class)))
            .thenReturn(httpResponseMock);

        HttpClientProvider httpClientProviderMock = mock(HttpClientProvider.class);
        when(httpClientProviderMock.createClient()).thenReturn(httpClientMock);

        ReportService reportService = new ReportService(httpClientProviderMock, applicationProperties);

        // Act
        try (LogCaptor logCaptor = LogCaptor.forClass(ReportService.class))
        {
            reportService.report(testedHost);

            // Assert
            assertAll(
                () -> assertThat(logCaptor.getWarnLogs()).containsExactly(expectedLog),
                () -> verify(httpResponseMock, only()).statusCode()
            );
        }
    }

    @Test
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public void report_PostWith404response_logsError()
    {
        // Arrange
        ApplicationProperties applicationProperties = MockCreateHelper.applicationProperties_default();
        String testedHost = "localhost.balazskrizsan.com";

        String expectedErrorLogStartWith = "Report HTTP error:";
        String expectedWarnLog = "Ping report: " + gson.toJson(new Report(testedHost, "icmpPingResult"));
        HttpRequest expectedRequest = HttpRequest.newBuilder()
            .uri(URI.create(applicationProperties.getPingServiceReportUrl()))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(expectedWarnLog))
            .build();

        IcmpPingService.LAST_ICMP_RESULTS.put(
            testedHost,
            new PingResult(false, LocalDateTime.of(2020, 1, 2, 3, 4, 5), "icmpPingResult")
        );

        HttpResponse<String> httpResponseMock = mock(HttpResponse.class);
        when(httpResponseMock.statusCode()).thenReturn(404);

        HttpClient httpClientMock = mock(HttpClient.class);
        when(httpClientMock.send(eq(expectedRequest), any(HttpResponse.BodyHandler.class)))
            .thenReturn(httpResponseMock);

        HttpClientProvider httpClientProviderMock = mock(HttpClientProvider.class);
        when(httpClientProviderMock.createClient()).thenReturn(httpClientMock);

        ReportService reportService = new ReportService(httpClientProviderMock, applicationProperties);

        // Act
        try (LogCaptor logCaptor = LogCaptor.forClass(ReportService.class))
        {
            reportService.report(testedHost);

            // Assert
            assertAll(
                () -> assertThat(logCaptor.getWarnLogs()).containsExactly(expectedWarnLog),
                () -> assertThat(logCaptor.getErrorLogs().getFirst()).startsWith(expectedErrorLogStartWith),
                () -> verify(httpResponseMock, only()).statusCode()
            );
        }
    }
}
