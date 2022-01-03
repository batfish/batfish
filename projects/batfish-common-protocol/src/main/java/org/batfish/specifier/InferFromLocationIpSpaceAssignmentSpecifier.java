package org.batfish.specifier;

import java.util.Set;
import org.batfish.datamodel.IpSpace;

/**
 * An {@link IpSpaceAssignmentSpecifier} that specifies the {@link IpSpace} owned by each {@link
 * Location}.
 */
public final class InferFromLocationIpSpaceAssignmentSpecifier
    implements IpSpaceAssignmentSpecifier {
  public static final InferFromLocationIpSpaceAssignmentSpecifier INSTANCE =
      new InferFromLocationIpSpaceAssignmentSpecifier();

  private InferFromLocationIpSpaceAssignmentSpecifier() {}

  @Override
  public IpSpaceAssignment resolve(Set<Location> locations, SpecifierContext ctxt) {
    IpSpaceAssignment.Builder builder = IpSpaceAssignment.builder();
    locations.forEach(
        location -> builder.assign(location, ctxt.getLocationInfo(location).getSourceIps()));
    return builder.build();
  }
}
