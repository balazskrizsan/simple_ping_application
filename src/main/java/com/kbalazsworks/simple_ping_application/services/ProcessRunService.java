package com.kbalazsworks.simple_ping_application.services;

import com.kbalazsworks.simple_ping_application.entities.ProcessRunResponse;
import com.kbalazsworks.simple_ping_application.exceptions.ProcessRunException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class ProcessRunService
{
    public ProcessRunResponse run(String... params) throws ProcessRunException
    {
        try
        {
            ProcessBuilder builder = new ProcessBuilder(params);
            builder.redirectErrorStream(true);
            Process process = builder.start();

            String result = new BufferedReader(new InputStreamReader(process.getInputStream()))
                .lines()
                .collect(Collectors.joining(System.lineSeparator()))
                .trim()
                .replaceAll("\r\n", "\n");

            return new ProcessRunResponse(result, process.waitFor());
        }
        catch (IOException e)
        {
            throw new ProcessRunException("IO Exception", e);
        }
        catch (InterruptedException e)
        {
            throw new ProcessRunException("Interrupted Exception", e);
        }
    }
}
