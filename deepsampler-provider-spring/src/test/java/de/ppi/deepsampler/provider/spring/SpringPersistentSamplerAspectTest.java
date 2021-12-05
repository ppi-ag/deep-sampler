/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.provider.spring;

import de.ppi.deepsampler.provider.common.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestSpringConfig.class)
public class SpringPersistentSamplerAspectTest extends PersistentSamplerAspectTest {

    @Autowired
    private TestService testService;

    @Override
    public TestService getTestService() {
        return testService;
    }

}