package unit.com.kbalazsworkds.service;

import com.kbalazsworkds.extensions.ApplicationProperties;
import org.junit.jupiter.api.Test;
import unit.com.kbalazsworkds.helpers.MockCreateHelper;

import static org.junit.jupiter.api.Assertions.fail;

public class BrokenTest
{
    private final ApplicationProperties applicationPropertiesMock =
        MockCreateHelper.applicationProperties_default();

    @Test
    public void failingTest() throws InterruptedException
    {
        fail();
    }
}
