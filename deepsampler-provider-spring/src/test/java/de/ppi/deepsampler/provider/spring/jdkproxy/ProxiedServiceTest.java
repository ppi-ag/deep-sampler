package de.ppi.deepsampler.provider.spring.jdkproxy;

import de.ppi.deepsampler.core.api.Sample;
import de.ppi.deepsampler.core.api.Sampler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ProxiedServiceSpringConfig.class)
class ProxiedServiceTest {

    public static final String VALUE_A = "Value A";
    public static final String VALUE_B = "Value B";

    /**
     * {@link ProxiedTestService} is autowired as a jdk-proxy instead of a vanilla class.
     *
     * @see ProxiedServiceSpringConfig#proxiedTestService()
     */
    @Autowired
    private ProxiedTestService proxiedTestService;

    @Test
    void proxiedServiceCanBeStubbed() {
        // 👉 GIVEN
        final ProxiedTestService testServiceSampler = Sampler.prepare(ProxiedTestService.class);
        Sample.of(testServiceSampler.echoParameter(VALUE_B)).is(VALUE_A);

        // 🧪 WHEN
        String actualEcho = proxiedTestService.echoParameter(VALUE_B);

        // 🔬 THEN
        assertEquals(VALUE_A, actualEcho);

        // 🔭 CROSSCHECK
        // 👉 GIVEN
        Sampler.clear();

        // 🧪 WHEN
        actualEcho = proxiedTestService.echoParameter(VALUE_B);

        // 🔬 THEN
        assertEquals(VALUE_B, actualEcho);
    }
}
