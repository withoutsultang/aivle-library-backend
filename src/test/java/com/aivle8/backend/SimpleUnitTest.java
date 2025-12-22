package com.aivle8.backend;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class SimpleUnitTest {

	@Test
	void backendApplicationClassCanBeLoaded() {
		assertDoesNotThrow(() -> {
			Class.forName("com.aivle8.backend.BackendApplication");
		});
	}
}

