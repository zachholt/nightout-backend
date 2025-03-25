package com.zachholt.nightout;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.yaml")
@ActiveProfiles("test")
class NightoutApplicationTests {

	@Test
	void contextLoads() {
		// This test will pass if the Spring context loads successfully
	}

}
