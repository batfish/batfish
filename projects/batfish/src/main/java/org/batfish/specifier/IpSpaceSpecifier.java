package org.batfish.specifier;

import com.google.common.collect.Multimap;
import java.util.Set;
import org.batfish.datamodel.IpSpace;

public interface IpSpaceSpecifier {
  Multimap<IpSpace, Location> resolve(Set<Location> locations, SpecifierContext ctxt);
}
