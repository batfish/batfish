package org.batfish.specifier;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import org.batfish.datamodel.IpSpace;

public class ConstantIpSpaceSpecifier implements IpSpaceSpecifier {
  private final IpSpace _ipSpace;

  public ConstantIpSpaceSpecifier(IpSpace ipSpace) {
    _ipSpace = ipSpace;
  }

  @Override
  public Map<Set<Location>, IpSpace> resolve(Set<Location> locations, SpecifierContext ctxt) {
    return ImmutableMap.of(locations, _ipSpace);
  }
}
