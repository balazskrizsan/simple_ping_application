package com.kbalazsworkds;

import com.kbalazsworkds.services.HostMonitorService;

public class Main
{
    public static void main(String[] args)
    {
        HostMonitorService monitor = new HostMonitorService();
        monitor.startMonitoring();
    }
}
