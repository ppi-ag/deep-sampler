/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.model;

import java.io.Serializable;

/**
 * In most cases it will be sufficient to define a fixed Sample as a return value for a stubbed method, but sometimes it
 * is necessary to execute some logic that would compute the return value or that would even change some additional state.
 * This can be done by using an Answer like so:
 *
 * <code>
 *     Sample.of(sampler.echo(anyString())).answer(invocation -&gt; invocation.getParameters().get(0));
 * </code>
 *
 * In essence using Answers gives free control on what a stubbed method should do.
 */
@FunctionalInterface
public interface Answer<E extends Throwable> extends Serializable {

    /**
     * A method that is executed as a replacement of the stubbed method.
     *
     * @param stubMethodInvocation A description of the stubbed method
     * @return a Sample that should be returned by the stubbed method.
     * @throws E By declaring the {@link Throwable} as a generic type, this functional interface can cope with methods that throw any kind of {@link Throwable} and
     * also allows methods that don't declare an {@link Throwable} at all.
     */
    Object call(StubMethodInvocation stubMethodInvocation) throws E;
}
