package com.meetingops;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Context load test to verify the application starts without errors.
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
@SpringBootTest
@ActiveProfiles("test")
class MeetingOpsApplicationTest {

    @Test
    void contextLoads() {
        // TODO: Add more integration tests as the application evolves
    }
}
