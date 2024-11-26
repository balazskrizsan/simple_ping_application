package unit.com.kbalazsworkds.service;

import com.kbalazsworkds.extensions.ApplicationProperties;
import com.kbalazsworkds.services.HostMonitorService;
import com.kbalazsworkds.services.IcmpPingService;
import com.kbalazsworkds.services.TcpPingService;
import com.kbalazsworkds.services.TracerouteService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import unit.com.kbalazsworkds.helpers.MockCreateHelper;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class HostMonitorServiceTest
{
    private final ApplicationProperties applicationPropertiesMock =
        MockCreateHelper.applicationProperties_default();

    @Test
    public void startMonitoring_allServiceMockAdded_allServicesCalled() throws InterruptedException
    {
        // Arrange
        IcmpPingService icmpPingServiceMock = mock(IcmpPingService.class);
        TcpPingService tcpPingServiceMock = mock(TcpPingService.class);
        TracerouteService tracerouteServiceMock = mock(TracerouteService.class);

        HostMonitorService hostMonitorService = new HostMonitorService(
            applicationPropertiesMock,
            icmpPingServiceMock,
            tcpPingServiceMock,
            tracerouteServiceMock
        );

        // Act
        hostMonitorService.startMonitoring();
        Thread.sleep(3000);

        // Assert
        assertAll(
            () -> verify(icmpPingServiceMock, Mockito.atLeast(3)).ping(any()),
            () -> verify(tracerouteServiceMock, Mockito.atLeast(3)).trace(any()),
            () -> verify(tracerouteServiceMock, Mockito.atLeast(3)).trace(any())
        );
    }
}
