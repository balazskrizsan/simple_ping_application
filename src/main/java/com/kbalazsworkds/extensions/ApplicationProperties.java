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

            pingServiceHosts = Arrays.stream(properties.getProperty("ping_service.hosts").split(","))
                .map(String::trim)
                .toList();
            pingServiceIcmpDelay = Integer.parseInt(properties.getProperty("ping_service.icmpDelay"));
            pingServiceReportUrl = properties.getProperty("ping_service.reportUrl");
        }
        catch (IOException ex)
        {
            log.error("ApplicationPropertiesService exception occurred", ex);
        }
    }
}
