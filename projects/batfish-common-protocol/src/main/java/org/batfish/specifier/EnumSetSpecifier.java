package org.batfish.specifier;

import java.util.Set;

/** An abstract specification of a set of named structure types. */
public interface EnumSetSpecifier<T> {
  /** Returns named structure types that match this specifier. */
  Set<T> resolve();
}
