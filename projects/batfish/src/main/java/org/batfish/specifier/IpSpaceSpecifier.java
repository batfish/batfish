package org.batfish.specifier;

import java.util.Map;
import java.util.Set;
import org.batfish.datamodel.IpSpace;

public interface IpSpaceSpecifier {
  Map<Set<Location>, IpSpace> resolve(Set<Location> locations, SpecifierContext ctxt);
}
