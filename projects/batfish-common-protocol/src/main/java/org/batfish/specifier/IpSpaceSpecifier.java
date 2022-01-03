package org.batfish.specifier;

import org.batfish.datamodel.IpSpace;

/** An abstract specifier of {@link IpSpace}s. */
public interface IpSpaceSpecifier {
  /** Resolve the specifier into a concrete {@link IpSpace}. */
  IpSpace resolve(SpecifierContext ctxt);
}
