package org.deepsampler.core.model;

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
     */
    void call(StubMethodInvocation stubMethodInvocation) throws E;
}
