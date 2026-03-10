package org.batfish.specifier;

import java.util.Set;

/** An abstract specification of a set of names. */
public interface NameSetSpecifier {
  /** Returns enums that match this specifier. */
  Set<String> resolve(SpecifierContext ctxt);
}
