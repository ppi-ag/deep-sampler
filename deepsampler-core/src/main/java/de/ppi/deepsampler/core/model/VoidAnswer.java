/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.model;

/**
 * Sometimes it is necessary to execute some logic that would replace the original method call.
 * In the case of void methods, this can be done like so:
 *
 * <code>
 *     Sample.of(() -&gt; sampler.doSomeThing()).answers(invocation -&gt; doSomethingElse());
 * </code>
 *
 * In essence using Answers gives free control on what a stubbed method should do.
 */
@FunctionalInterface
public interface VoidAnswer<E extends Exception> {

    /**
     * A method that is executed as a replacement of the stubbed method.
     *
     * @param stubMethodInvocation A description of the stubbed method
     * @throws E By declaring the {@link Exception} as a generic type, this functional interface can cope with methods that throw any kind of {@link Exception} an
     * also allows methods that don't declare an {@link Exception} at all.
     */
    void call(StubMethodInvocation stubMethodInvocation) throws E;
}
