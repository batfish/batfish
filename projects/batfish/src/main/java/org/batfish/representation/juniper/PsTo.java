package org.batfish.representation.juniper;

import java.io.Serializable;

/**
 * Represents a policy-statement "to" line in a {@link PsTerm}.
 *
 * <p>Unlike {@link PsFrom}, "to" conditions require export context (target protocol, RIB, etc.) and
 * cannot be independently converted to VI boolean expressions. Conversion must be handled at the
 * policy level in {@link JuniperConfiguration}.
 */
public abstract class PsTo implements Serializable {}
