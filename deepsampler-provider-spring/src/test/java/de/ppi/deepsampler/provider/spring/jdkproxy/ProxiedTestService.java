/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.provider.spring.jdkproxy;

public interface ProxiedTestService {

    String echoParameter(final String param);
}
