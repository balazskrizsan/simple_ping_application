package unit.com.kbalazsworkds.helpers;

import com.kbalazsworkds.entities.ProcessRunResponse;
import com.kbalazsworkds.extensions.ApplicationProperties;
import com.kbalazsworkds.providers.LocalDateTimeProvider;
import com.kbalazsworkds.services.ProcessRunService;
import com.kbalazsworkds.services.ReportService;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.time.LocalDateTime;
import java.util.List;

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

    @SneakyThrows
    public static ProcessRunService ProcessRunService_run_ping(
        @NonNull String host,
        @NonNull ProcessRunResponse mockedResponse
    )
    {
        ProcessRunService mock = mock(ProcessRunService.class);
        when(mock.run("ping", "-n", "5", host)).thenReturn(mockedResponse);

        return mock;
    }

    public static ReportService ReportService_default()
    {
        return mock(ReportService.class);
    }

    public static ApplicationProperties applicationProperties_default()
    {
        ApplicationProperties ap = mock(ApplicationProperties.class);

        when(ap.getPingServiceIcmpDelay()).thenReturn(1000);
        when(ap.getPingServiceHosts()).thenReturn(List.of("localhost1", "localhost2", "localhost3"));
        when(ap.getPingServiceReportUrl()).thenReturn("http://localhost:8080/report");

        return ap;
    }
}
