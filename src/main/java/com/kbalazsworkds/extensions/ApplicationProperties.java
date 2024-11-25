package com.kbalazsworkds.extensions;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.Arrays;
import java.util.List;

@Getter
@Log4j2
public class ApplicationProperties
{
    private final int pingServiceTcpDelay;
    private final int pingServiceTcpTimeout;
    private final String pingServiceTcpProtocol;
    private final String pingServiceTcpPingPortEndpoint;
    private final List<String> pingServiceHosts;
    private final int pingServiceIcmpDelay;
    private final String pingServiceReportUrl;

    public ApplicationProperties()
    {
        pingServiceHosts = Arrays.stream(System.getenv("PING_SERVICE__HOSTS").split(","))
            .map(String::trim)
            .toList();
        pingServiceReportUrl = System.getenv("PING_SERVICE__REPORT_URL");
        pingServiceIcmpDelay = Integer.parseInt(System.getenv("PING_SERVICE__ICMP_DELAY"));
        pingServiceTcpDelay = Integer.parseInt(System.getenv("PING_SERVICE__TCP_DELAY"));
        pingServiceTcpTimeout = Integer.parseInt(System.getenv("PING_SERVICE__TCP_TIMEOUT"));
        pingServiceTcpProtocol = System.getenv("PING_SERVICE__TCP_PROTOCOL");
        pingServiceTcpPingPortEndpoint = System.getenv("PING_SERVICE__TCP_PING_PORT_END_POINT");
        log.info("Application properties loaded");
        log.info(" - env var: pingServiceHosts: {}", pingServiceHosts);
        log.info(" - env var: pingServiceReportUrl: {}", pingServiceReportUrl);
        log.info(" - env var: pingServiceIcmpDelay: {}", pingServiceIcmpDelay);
        log.info(" - env var: pingServiceTcpDelay: {}", pingServiceTcpDelay);
        log.info(" - env var: pingServiceTcpTimeout: {}", pingServiceTcpTimeout);
        log.info(" - env var: pingServiceTcpProtocol: {}", pingServiceTcpProtocol);
        log.info(" - env var: pingServiceTcpPingPortEndpoint: {}", pingServiceTcpPingPortEndpoint);

    }
}
