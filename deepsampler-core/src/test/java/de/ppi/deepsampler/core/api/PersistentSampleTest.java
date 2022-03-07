/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.api;

import de.ppi.deepsampler.core.error.NotASamplerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PersistentSampleTest {

    public static final String PARAMETER_VALUE = "Blubb";

    @BeforeEach
    public void cleanUp() {
        Sampler.clear();
    }

    @Test
    void callOfANonSamplerIsDetectedIBeforeSamplerHasBeenDefined() {
        // GIVEN
        final var notASampler = new TestService();
        // THEN
        Sampler.clear();
        assertThrows(NotASamplerException.class, () -> shouldThrowExceptionAttemptingToSampleANonSampler(notASampler));
    }

    private void shouldThrowExceptionAttemptingToSampleANonSampler(TestService notASampler) {
        PersistentSample.of(notASampler.echoParameter(PARAMETER_VALUE));
    }

    @Test
    void callOfANonSamplerIsDetectedAfterSamplersHasBeenDefined() {
        //GIVEN
        Sampler.clear();
        final var realTestServiceSampler = Sampler.prepare(TestService.class);
        final var notASampler = new TestService();

        //WHEN UNCHANGED
        assertDoesNotThrow(() -> PersistentSample.of(realTestServiceSampler.echoParameter(PARAMETER_VALUE)));

        // THEN
        assertThrows(NotASamplerException.class, () -> shouldThrowExceptionAttemptingToSampleANonSampler(notASampler));
    }

    @Test
    void samplerForVerificationIsChecked() {
        //GIVEN WHEN
        final var testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.forVerification(testServiceSampler);

        // THEN
        assertThrows(NotASamplerException.class, () -> PersistentSample.forVerification("I'm not a Sampler."));
        assertThrows(NullPointerException.class, () -> PersistentSample.forVerification(null));
    }

    public static class TestService {
        public String echoParameter(final String someParameter) {
            return someParameter;
        }
    }

}