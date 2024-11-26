package unit.com.kbalazsworks.simple_ping_application.helpers;

import com.kbalazsworks.simple_ping_application.entities.ProcessRunResponse;
import com.kbalazsworks.simple_ping_application.exceptions.ProcessRunException;
import com.kbalazsworks.simple_ping_application.extensions.ApplicationProperties;
import com.kbalazsworks.simple_ping_application.providers.DurationProvider;
import com.kbalazsworks.simple_ping_application.providers.LocalDateTimeProvider;
import com.kbalazsworks.simple_ping_application.services.ProcessRunService;
import com.kbalazsworks.simple_ping_application.services.ReportService;
import lombok.NonNull;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockCreateHelper
{
    public static LocalDateTimeProvider LocalDateTimeProvider_now_default()
    {
        LocalDateTimeProvider mock = mock(LocalDateTimeProvider.class);
        when(mock.now())
            .thenReturn(LocalDateTime.of(2020, 1, 2, 3, 4, 5));

        return mock;
    }

    public static ProcessRunService ProcessRunService_run_ping(
        @NonNull String host,
        @NonNull ProcessRunResponse mockedResponse
    ) throws ProcessRunException
    {
        ProcessRunService mock = mock(ProcessRunService.class);
        when(mock.run("ping", "-n", "5", host)).thenReturn(mockedResponse);

        return mock;
    }

    public static ProcessRunService ProcessRunService_run_tracert(
        @NonNull String host,
        @NonNull ProcessRunResponse mockedResponse
    ) throws ProcessRunException
    {
        ProcessRunService mock = mock(ProcessRunService.class);
        when(mock.run("tracert", host)).thenReturn(mockedResponse);
        when(mock.run("traceroute", host)).thenReturn(mockedResponse);

        return mock;
    }

    public static ReportService ReportService_default()
    {
        return mock(ReportService.class);
    }

    public static ApplicationProperties applicationProperties_default()
    {
        ApplicationProperties ap = mock(ApplicationProperties.class);

        when(ap.getPingServiceTcpDelay()).thenReturn(1000);
        when(ap.getPingServiceTcpTimeout()).thenReturn(1000);
        when(ap.getPingServiceTcpProtocol()).thenReturn("https");
        when(ap.getPingServiceTcpPingPortEndpoint()).thenReturn(":1234/ping");
        when(ap.getPingServiceHosts()).thenReturn(List.of("localhost1", "localhost2", "localhost3"));
        when(ap.getPingServiceIcmpDelay()).thenReturn(1000);
        when(ap.getPingServiceReportUrl()).thenReturn("http://localhost:8080/report");
        when(ap.getPingServiceTracerouteDelay()).thenReturn(1000);

        return ap;
    }

    public static DurationProvider DurationProvider_between_10ms()
    {
        DurationProvider mock = mock(DurationProvider.class);
        when(mock.between(any(), any())).thenReturn(Duration.ofMillis(10));

        return mock;
    }

    public static DurationProvider DurationProvider_between_returnsWith(long returnValue)
    {
        DurationProvider mock = mock(DurationProvider.class);
        when(mock.between(any(), any())).thenReturn(Duration.ofMillis(returnValue));

        return mock;
    }
}
