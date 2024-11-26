package unit.com.kbalazsworks.simple_ping_application.repositories;

import com.kbalazsworks.simple_ping_application.repositories.TaskRunRepository;
import org.junit.jupiter.api.Test;

import static com.kbalazsworks.simple_ping_application.enums.RunTypeEnum.ICMP_PING;
import static com.kbalazsworks.simple_ping_application.enums.RunTypeEnum.TCP_PING;
import static com.kbalazsworks.simple_ping_application.enums.RunTypeEnum.TRACEROUTE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class TaskRunRepositoryTest
{
    @Test
    void isRunning_stateCheck_multipleData()
    {
        // Arrange
        TaskRunRepository taskRunRepository = new TaskRunRepository();

        taskRunRepository.setRunning(ICMP_PING, "host1");
        taskRunRepository.setRunning(ICMP_PING, "host2");
        taskRunRepository.setRunning(ICMP_PING, "host4");
        taskRunRepository.setRunning(TCP_PING, "host4");

        // Act
        boolean actual1 = taskRunRepository.isRunning(ICMP_PING, "host1");
        boolean actual2 = taskRunRepository.isRunning(ICMP_PING, "host2");
        boolean actual3 = taskRunRepository.isRunning(ICMP_PING, "host3");
        boolean actual41 = taskRunRepository.isRunning(ICMP_PING, "host4");
        boolean actual42 = taskRunRepository.isRunning(TCP_PING, "host4");
        boolean actual43 = taskRunRepository.isRunning(TRACEROUTE, "host4");

        // Assert
        assertAll(
            () -> assertThat(actual1).isTrue(),
            () -> assertThat(actual2).isTrue(),
            () -> assertThat(actual3).isFalse(),
            () -> assertThat(actual41).isTrue(),
            () -> assertThat(actual42).isTrue(),
            () -> assertThat(actual43).isFalse()
        );
    }

    @Test
    void setRunning_setState_finishRemoveState()
    {
        // Arrange
        TaskRunRepository taskRunRepository = new TaskRunRepository();

        taskRunRepository.setRunning(ICMP_PING, "host1");
        taskRunRepository.setRunning(ICMP_PING, "host2");
        taskRunRepository.setRunning(ICMP_PING, "host4");
        taskRunRepository.setRunning(TCP_PING, "host4");

        taskRunRepository.finish(ICMP_PING, "host2");
        taskRunRepository.finish(ICMP_PING, "host4");

        // Act
        boolean actual1 = taskRunRepository.isRunning(ICMP_PING, "host1");
        boolean actual2 = taskRunRepository.isRunning(ICMP_PING, "host2");
        boolean actual3 = taskRunRepository.isRunning(ICMP_PING, "host3");
        boolean actual41 = taskRunRepository.isRunning(ICMP_PING, "host4");
        boolean actual42 = taskRunRepository.isRunning(TCP_PING, "host4");
        boolean actual43 = taskRunRepository.isRunning(TRACEROUTE, "host4");

        // Assert
        assertAll(
            () -> assertThat(actual1).isTrue(),
            () -> assertThat(actual2).isFalse(),
            () -> assertThat(actual3).isFalse(),
            () -> assertThat(actual41).isFalse(),
            () -> assertThat(actual42).isTrue(),
            () -> assertThat(actual43).isFalse()
        );
    }
}
