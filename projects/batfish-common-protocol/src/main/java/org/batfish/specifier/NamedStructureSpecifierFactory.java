package org.batfish.specifier;

import javax.annotation.Nullable;

/** An interface for a factory that produces {@link NamedStructureSpecifier} */
public interface NamedStructureSpecifierFactory {
  /** The NamedStructureSpecifier factory method. Input types vary by factory. */
  NamedStructureSpecifier buildNamedStructureSpecifier(@Nullable Object input);
}
