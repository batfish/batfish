package org.batfish.specifier;

import javax.annotation.Nullable;

/** An interface for a factory that produces {@link EnumSetSpecifier} */
public interface NamedStructureSpecifierFactory {
  /** The EnumSetSpecifier factory method. Input types vary by factory. */
  EnumSetSpecifier buildNamedStructureSpecifier(@Nullable Object input);
}
