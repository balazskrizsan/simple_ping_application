package unit.com.kbalazsworks.simple_ping_application.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kbalazsworks.simple_ping_application.entities.PingResult;
import com.kbalazsworks.simple_ping_application.entities.Report;
import com.kbalazsworks.simple_ping_application.extensions.ApplicationProperties;
import com.kbalazsworks.simple_ping_application.extensions.LocalDateTimeAdapter;
import com.kbalazsworks.simple_ping_application.providers.HttpClientProvider;
import com.kbalazsworks.simple_ping_application.repositories.IcmpPingRepository;
import com.kbalazsworks.simple_ping_application.repositories.TcpPingRepository;
import com.kbalazsworks.simple_ping_application.repositories.TracerouteRepository;
import com.kbalazsworks.simple_ping_application.services.ReportService;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.Test;
import unit.com.kbalazsworks.simple_ping_application.helpers.MockCreateHelper;

import java.io.IOException;
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
    @SuppressWarnings("unchecked")
    public void report_PostWith200response_perfect() throws IOException, InterruptedException
    {
        // Arrange
        ApplicationProperties applicationProperties = MockCreateHelper.applicationProperties_default();
        String testedHost = "localhost.balazskrizsan.com";

        TcpPingRepository tcpPingRepository = new TcpPingRepository();
        IcmpPingRepository icmpPingRepository = new IcmpPingRepository();
        TracerouteRepository tracerouteRepository = new TracerouteRepository();

        String expectedLog =
            "Ping report: " + gson.toJson(new Report(testedHost, "icmpPingResult", "tcpPingResult", "tracerouteResult"));
        HttpRequest expectedRequest = HttpRequest.newBuilder()
            .uri(URI.create(applicationProperties.getPingServiceReportUrl()))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(expectedLog))
            .build();

        icmpPingRepository.save(
            testedHost,
            new PingResult(false, LocalDateTime.of(2020, 1, 2, 3, 4, 5), "icmpPingResult")
        );
        tcpPingRepository.save(
            testedHost,
            new PingResult(false, LocalDateTime.of(2020, 1, 2, 3, 4, 5), "tcpPingResult")
        );
        tracerouteRepository.save(
            testedHost,
            new PingResult(false, LocalDateTime.of(2020, 1, 2, 3, 4, 5), "tracerouteResult")
        );

        HttpResponse<String> httpResponseMock = mock(HttpResponse.class);
        when(httpResponseMock.statusCode()).thenReturn(200);

        HttpClient httpClientMock = mock(HttpClient.class);
        when(httpClientMock.send(eq(expectedRequest), any(HttpResponse.BodyHandler.class)))
            .thenReturn(httpResponseMock);

        HttpClientProvider httpClientProviderMock = mock(HttpClientProvider.class);
        when(httpClientProviderMock.createClient()).thenReturn(httpClientMock);

        ReportService reportService = new ReportService(
            httpClientProviderMock,
            applicationProperties,
            tcpPingRepository,
            icmpPingRepository,
            tracerouteRepository
        );

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
    @SuppressWarnings("unchecked")
    public void report_PostWith404response_logsError() throws IOException, InterruptedException
    {
        // Arrange
        ApplicationProperties applicationProperties = MockCreateHelper.applicationProperties_default();
        String testedHost = "localhost.balazskrizsan.com";

        TcpPingRepository tcpPingRepository = new TcpPingRepository();
        IcmpPingRepository icmpPingRepository = new IcmpPingRepository();
        TracerouteRepository tracerouteRepository = new TracerouteRepository();

        String expectedErrorLogStartWith = "Report HTTP error:";
        String expectedWarnLog =
            "Ping report: " + gson.toJson(new Report(testedHost, "icmpPingResult", "tcpPingResult", "tracerouteResult"));
        HttpRequest expectedRequest = HttpRequest.newBuilder()
            .uri(URI.create(applicationProperties.getPingServiceReportUrl()))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(expectedWarnLog))
            .build();

        icmpPingRepository.save(
            testedHost,
            new PingResult(false, LocalDateTime.of(2020, 1, 2, 3, 4, 5), "icmpPingResult")
        );
        tcpPingRepository.save(
            testedHost,
            new PingResult(false, LocalDateTime.of(2020, 1, 2, 3, 4, 5), "tcpPingResult")
        );
        tracerouteRepository.save(
            testedHost,
            new PingResult(false, LocalDateTime.of(2020, 1, 2, 3, 4, 5), "tracerouteResult")
        );

        HttpResponse<String> httpResponseMock = mock(HttpResponse.class);
        when(httpResponseMock.statusCode()).thenReturn(404);

        HttpClient httpClientMock = mock(HttpClient.class);
        when(httpClientMock.send(eq(expectedRequest), any(HttpResponse.BodyHandler.class)))
            .thenReturn(httpResponseMock);

        HttpClientProvider httpClientProviderMock = mock(HttpClientProvider.class);
        when(httpClientProviderMock.createClient()).thenReturn(httpClientMock);

        ReportService reportService = new ReportService(
            httpClientProviderMock,
            applicationProperties,
            tcpPingRepository,
            icmpPingRepository,
            tracerouteRepository
        );

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
