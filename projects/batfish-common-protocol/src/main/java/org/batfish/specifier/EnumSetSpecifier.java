package org.batfish.specifier;

import java.util.Set;

/** An abstract specification of a set of enums of type T. */
public interface EnumSetSpecifier<T> {
  /** Returns enums that match this specifier. */
  Set<T> resolve();
}
