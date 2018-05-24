package org.batfish.specifier;

import java.util.Set;
import org.batfish.datamodel.IpSpace;

/**
 * An {@link IpSpaceSpecifier} that is constant (independent of the input {@link Location}). All
 * input {@link Location}s are assigned the specified {@link IpSpace}.
 */
public class ConstantIpSpaceSpecifier implements IpSpaceSpecifier {
  private final IpSpace _ipSpace;

  public ConstantIpSpaceSpecifier(IpSpace ipSpace) {
    _ipSpace = ipSpace;
  }

  @Override
  public IpSpaceAssignment resolve(Set<Location> locations, SpecifierContext ctxt) {
    return IpSpaceAssignment.builder().assign(locations, _ipSpace).build();
  }
}
