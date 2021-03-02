package de.ppi.deepsampler.core.api;

/**
 * Type of the scope used by deepsampler. The scope defines how samples are bound respectively how long they will live in the JVM.
 */
public enum ScopeType {

    /**
     * Samples are bound to a thread. So every Sample will live and die with the thread they are created in.
     *
     * @see de.ppi.deepsampler.core.model.ThreadScope
     */
    THREAD,

    /**
     * Samples are bound to the jvm. So every sample will live as long the jvm is active.
     *
     * @see de.ppi.deepsampler.core.model.SingletonScope
     */
    SINGLETON

}

