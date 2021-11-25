package org.batfish.vendor;

import java.io.Serializable;

/**
 * Information to use when parsing a {@link VendorConfiguration}.
 *
 * <p>Reasonable implementations of equals() and hashCode() must be provided by subclasses. They are
 * used to determine when it is safe to reuse parsing.
 */
public interface VendorParsingContext extends Serializable {}
