package org.batfish.specifier;

import com.google.common.collect.Multimap;
import java.util.Set;
import org.batfish.datamodel.IpSpace;

/** An abstract specifier of {@link IpSpace}s by some means. */
public interface IpSpaceSpecifier {
  /**
   * Resolve the specifier into concrete {@link IpSpace}s.
   *
   * @param locations The @{link Location}s for which concrete {@link IpSpace}s are needed. Which
   *     concrete {@link IpSpace} is specified may (or may not) vary with {@link Location}.
   * @param ctxt Information about the network that may be used to resolve concrete {@link
   *     IpSpace}s.
   * @return A multimap that assigns {@link IpSpace}s to sets of the input {@link Location}. It is
   * required that the set of values of the multimap matches exactly the input {@link Location}s,
   * and that no {@link Location} is assigned more than one {@link IpSpace}.
   */
  Multimap<IpSpace, Location> resolve(Set<Location> locations, SpecifierContext ctxt);
}
