package com.kbalazsworkds.extensions;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@Getter
@Log4j2
public class ApplicationProperties
{
    private int pingServiceTcpDelay;
    private int pingServiceTcpTimeout;
    private String pingServiceTcpProtocol;
    private String pingServiceTcpPingPortEndpoint;
    private List<String> pingServiceHosts;
    private int pingServiceIcmpDelay;
    private String pingServiceReportUrl;

    public ApplicationProperties()
    {
        Properties properties = new Properties();

        try (InputStream input = ApplicationProperties.class.getClassLoader()
            .getResourceAsStream("application.properties")
        )
        {
            if (input == null)
            {
                log.error("Unable to find application.properties");

                return;
            }

            properties.load(input);

            pingServiceTcpDelay = Integer.parseInt(properties.getProperty("pingService.tcpDelay"));
            pingServiceTcpTimeout = Integer.parseInt(properties.getProperty("pingService.tcpTimeout"));
            pingServiceTcpProtocol = properties.getProperty("pingService.tcpProtocol");
            pingServiceTcpPingPortEndpoint = properties.getProperty("pingService.tcpPingPortEndpoint");
            pingServiceHosts = Arrays.stream(properties.getProperty("pingService.hosts").split(","))
                .map(String::trim)
                .toList();
            pingServiceIcmpDelay = Integer.parseInt(properties.getProperty("pingService.icmpDelay"));
            pingServiceReportUrl = properties.getProperty("pingService.reportUrl");
        }
        catch (IOException ex)
        {
            log.error("ApplicationPropertiesService exception occurred", ex);
        }
    }
}
