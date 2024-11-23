package com.kbalazsworkds.services;

import com.kbalazsworkds.extensions.ApplicationProperties;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class HostMonitorService
{
    private final ApplicationProperties applicationProperties = new ApplicationProperties();

    public void startMonitoring()
    {
        log.info("Starting monitoring on hosts: ");
        applicationProperties.getPingServiceHosts().stream().map(String::trim).forEach(log::info);
    }
}
