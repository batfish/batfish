package org.batfish.specifier;

import java.util.Set;
import org.batfish.datamodel.IpSpace;

/**
 * An {@link IpSpaceSpecifier} that specifies the {@link IpSpace} owned by each {@link Location}.
 */
public final class InferFromLocationIpSpaceSpecifier implements IpSpaceSpecifier {
  public static final InferFromLocationIpSpaceSpecifier INSTANCE =
      new InferFromLocationIpSpaceSpecifier();

  private InferFromLocationIpSpaceSpecifier() {}

  @Override
  public IpSpaceAssignment resolve(Set<Location> locations, SpecifierContext ctxt) {
    IpSpaceAssignment.Builder builder = IpSpaceAssignment.builder();
    locations.forEach(
        location -> builder.assign(location, ctxt.getLocationInfo(location).getSourceIps()));
    return builder.build();
  }
}
