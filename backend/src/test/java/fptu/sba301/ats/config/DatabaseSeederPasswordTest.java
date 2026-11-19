package fptu.sba301.ats.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DatabaseSeederPasswordTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "short-pass"})
    void refusesToSeedWithoutAStrongBootstrapPassword(String value) {
        // Previously a missing value silently fell back to the public "Password123!".
        assertThrows(IllegalStateException.class, () -> DatabaseSeeder.requireBootstrapPassword(value));
    }

    @Test
    void acceptsAConfiguredPassword() {
        assertEquals("a-long-enough-password", DatabaseSeeder.requireBootstrapPassword("a-long-enough-password"));
    }
}
